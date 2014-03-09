package com.vividsolutions.jts.polytriangulate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

public class EarClipperM {
    private final GeometryFactory gf;
    private final Polygon inputPolygon;
    private PreparedGeometry inputPrepGeom;
    private Geometry triangulation;
    /**
     * The shell coordinates are maintain in CW order. This means that for
     * convex interior angles, the vertices forming the angle are in CW
     * orientation.
     */
    private PolygonShellM polyShell;
    private List<Coordinate> polyShellCoords;
    private List<PolygonTriangle> triList;
    private boolean isImprove = true;

    /**
     * Constructor
     * 
     * @param inputPolygon the input polygon
     */
    public EarClipperM(Polygon inputPolygon) {
        gf = new GeometryFactory();
        this.inputPolygon = inputPolygon;
    }

    public void setImprove(boolean isImproved) {
        this.isImprove = isImproved;
    }

    /**
     * Get the result triangular polygons.
     * 
     * @return triangles as a GeometryCollection
     */
    public Geometry getResult() {
        if (triangulation == null) {
            triangulation = triangulate();
        }
        return triangulation;
    }

    /**
     * Performs the ear-clipping triangulation
     * 
     * @return GeometryCollection of triangular polygons
     */
    private Geometry triangulate() {
        triList = new ArrayList<PolygonTriangle>();
        createShell();
        computeEars();
        // improve triangulation if required
        if (isImprove) {
            TriangleImprover improver = new TriangleImprover(polyShellCoords);
            improver.improve(triList);
        }
        return createResult();
    }

    private void computeEars() {
        boolean finished = false;
        boolean foundEar = false;
        // int k0 = 0;
        // int k1 = 1;
        // int k2 = 2;
        int firstK = 0;
        // polyShell.nextCorner(false);
        int cornerCount = 0;
        do {
            foundEar = false;
            // find next convex corner (which is the next candidate ear)
            Coordinate[] cornerCandidate = polyShell
                    .getCornerCandidateVertices();
            while (CGAlgorithms.computeOrientation(cornerCandidate[0],
                    cornerCandidate[1], cornerCandidate[2]) != CGAlgorithms.CLOCKWISE) {
                // delete the "corner" if three points are in the same line
                if (inLine(cornerCandidate[0], cornerCandidate[1],
                        cornerCandidate[2])) {
                    polyShell.remove();
                    if (polyShell.size() < 3) {
                        return;
                    }
                }
                polyShell.nextCorner(true);
                cornerCandidate = polyShell.getCornerCandidateVertices();
            }
            cornerCount++;
            if (cornerCount > 2 * polyShell.size()) {
                throw new IllegalStateException(
                        "Unable to find a convex corner which is a valid ear");
            }
            boolean b = polyShell.isValidEarFast();
            // boolean a = isValidEarSlow();
            /*
             * if (a != b) { System.out.println(b + " " +
             * polyShell.toGeometry()); System.out.println("---" +
             * polyShell.getCornerCandidateVertices()[0] + " " +
             * polyShell.getCornerCandidateVertices()[1] + " " +
             * polyShell.getCornerCandidateVertices()[2]); }
             */
            if (b) {
                foundEar = true;
                int[] earIndex = polyShell.getCornerCandidateIndex();
                PolygonTriangle ear = new PolygonTriangle(earIndex[0],
                        earIndex[1], earIndex[2]);
                triList.add(ear);
                polyShell.remove();
                if (polyShell.size() < 3) {
                    return;
                }
                // polyShell.nextCorner(false);
                cornerCount = 0;
            }
            else {
                polyShell.nextCorner(true);
            }
            cornerCandidate = polyShell.getCornerCandidateVertices();
        } while (!finished);
    }

    /**
     * Check if the inputs are in the same line
     * 
     * @param a
     * @param b
     * @param c
     * @return
     */
    protected boolean inLine(Coordinate a, Coordinate b, Coordinate c) {
        double abX = b.x - a.x;
        double abY = b.y - a.y;
        double acX = c.x - a.x;
        double acY = c.y - a.y;
        // coordinate a and c are the same
        if (acX == 0 && acY == 0)
            return true;
        // a, b are the same
        if (abX == 0 && abY == 0)
            return true;
        if (abX == 0 && acX == 0)
            return true;
        if (abX * acX == 0)
            return false;
        if (abY / abX == acY / acX)
            return true;
        return false;
    }

    /**
     * This is MB's original logic.
     * 
     * It is quite expensive to compute. Could be possibly replaced with
     * checking whether any other vertices lie inside the ear - which could be
     * optimized with a spatial index on the vertices.
     * 
     * @param k0
     * @param k1
     * @param k2
     * @return
     */
    private boolean isValidEarSlow() {
        // if (!isValidEdge(k0, k2))
        // return false;
        Coordinate[] cornerCandidate = polyShell.getCornerCandidateVertices();
        LineString ls = gf.createLineString(new Coordinate[] {
                cornerCandidate[0], cornerCandidate[1] });
        if (!inputPrepGeom.covers(ls))
            return false;
        Polygon earPoly = gf.createPolygon(
                gf.createLinearRing(new Coordinate[] { cornerCandidate[0],
                        cornerCandidate[1], cornerCandidate[2],
                        cornerCandidate[0] }), null);
        if (inputPrepGeom.covers(earPoly))
            return true;
        return false;
    }

    private Geometry createResult() {
        Geometry[] geoms = new Geometry[triList.size()];
        for (int i = 0; i < triList.size(); i++) {
            geoms[i] = createPolygon(triList.get(i));
        }
        return gf.createGeometryCollection(geoms);
    }

    /**
     * Creates a Polygon from a PolygonTriangle object
     * 
     * @param t the triangle
     * @return a new Polygon object
     */
    private Polygon createPolygon(final PolygonTriangle t) {
        final int[] vertices = t.getVertices();
        return gf.createPolygon(
                gf.createLinearRing(new Coordinate[] {
                        polyShellCoords.get(vertices[0]),
                        polyShellCoords.get(vertices[1]),
                        polyShellCoords.get(vertices[2]),
                        polyShellCoords.get(vertices[0]) }), null);
    }

    /**
     * Transforms the input polygon into a single, possible self-intersecting
     * shell by connecting holes to the exterior ring, The holes are added from
     * the lowest upwards. As the resulting shell develops, a hole might be
     * added to what was originally another hole.
     */
    private void createShell() {
        // defensively copy the input polygon
        Polygon poly = (Polygon) inputPolygon.clone();
        poly.normalize();
        PreparedGeometryFactory pgFact = new PreparedGeometryFactory();
        inputPrepGeom = pgFact.create(poly);
        polyShellCoords = new ArrayList<Coordinate>();
        Coordinate[] coords = poly.getExteriorRing().getCoordinates();
        polyShellCoords.addAll(Arrays.asList(coords));
        if (poly.getNumInteriorRing() > 0) {
            HoleJoiner holeJoiner = new HoleJoiner(inputPrepGeom);
            holeJoiner.joinHoles(polyShellCoords);
        }
        polyShell = new PolygonShellM(polyShellCoords);
    }
}

class PolygonShellM {
    /**
     * The shell coordinates are maintain in CW order. This means that for
     * convex interior angles, the vertices forming the angle are in CW
     * orientation.
     */
    private List<Coordinate> shellCoords;
    private int[] shellCoordAvailable;
    private int size;
    // index for current candidate corner
    public int[] cornerCandidate;
    // first available coordinate index
    private int firstAvailable;

    public PolygonShellM(List<Coordinate> shellCoords) {
        this.shellCoords = shellCoords;
        size = shellCoords.size() - 1;
        shellCoordAvailable = new int[size];
        for (int i = 0; i < size; i++) {
            shellCoordAvailable[i] = i + 1;
        }
        shellCoordAvailable[size - 1] = 0;
        cornerCandidate = new int[3];
        cornerCandidate[0] = 0;
        cornerCandidate[1] = 1;
        cornerCandidate[2] = 2;
        firstAvailable = 0;
    }

    public int size() {
        return size;
    }

    /**
     * Check if the current corner candidate is valid without using cover()
     * 
     * @return
     */
    public boolean isValidEarFast() {
        Coordinate[] cornerCandidateV = getCornerCandidateVertices();
        double angle = Angle.angleBetweenOriented(cornerCandidateV[0],
                cornerCandidateV[1], cornerCandidateV[2]);
        Coordinate[] triRing = new Coordinate[] { cornerCandidateV[0],
                cornerCandidateV[1], cornerCandidateV[2], cornerCandidateV[0] };
        int currIndex = nextIndex(firstAvailable);
        int prevIndex = firstAvailable;
        Coordinate prevV = shellCoords.get(prevIndex);
        for (int i = 0; i < size; i++) {
            Coordinate v = shellCoords.get(currIndex);
            // when corner[1] occurs, cannot simply skip. It might occur
            // multiple times and is connected with a hole
            if (v.equals2D(triRing[1])) {
                Coordinate nextTmp = shellCoords.get(nextIndex(currIndex));
                double aOut = Angle.angleBetweenOriented(cornerCandidateV[0],
                        cornerCandidateV[1], nextTmp);
                double aIn = Angle.angleBetweenOriented(cornerCandidateV[0],
                        cornerCandidateV[1], prevV);
                // TODO: add cases where a line attaching to corner[1]
                // if(aOut == aIn){
                // remove(prevIndex, currIndex);
                // }
                if (aOut > 0 && aOut < angle)
                    return false;
                if (aIn > 0 && aIn < angle)
                    return false;
                if (aOut == 0 && aIn == angle)
                    return false;
                prevV = v;
                prevIndex = currIndex;
                currIndex = nextIndex(currIndex);
                continue;
            }
            prevV = v;
            prevIndex = currIndex;
            currIndex = nextIndex(currIndex);
            if (v.equals2D(triRing[0]) || v.equals2D(triRing[2])) {
                continue;
            }
            // not valid if vertex is contained in tri
            if (CGAlgorithms.isPointInRing(v, triRing)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Remove corner[1] and update the candidate corner.
     */
    public void remove() {
        if (firstAvailable == cornerCandidate[1]) {
            firstAvailable = shellCoordAvailable[cornerCandidate[1]];
        }
        shellCoordAvailable[cornerCandidate[0]] = shellCoordAvailable[cornerCandidate[1]];
        shellCoordAvailable[cornerCandidate[1]] = -1;
        size--;
        nextCorner(false);
    }

    /**
     * Remove curr
     * @param pre
     * @param curr
     */
    private void remove(int pre, int curr) {
        if (firstAvailable == curr) {
            firstAvailable = shellCoordAvailable[curr];
        }
        shellCoordAvailable[pre] = shellCoordAvailable[curr];
        shellCoordAvailable[curr] = -1;
        size--;
    }

    public Coordinate[] getCornerCandidateVertices() {
        Coordinate[] coord = new Coordinate[] {
                shellCoords.get(cornerCandidate[0]),
                shellCoords.get(cornerCandidate[1]),
                shellCoords.get(cornerCandidate[2]) };
        return coord;
    }

    public int[] getCornerCandidateIndex() {
        return cornerCandidate;
    }

    /**
     * Set to next corner candidate.
     * @param moveFirst if corner[0] should be moved to next available
     *            coordinates.
     */
    public void nextCorner(boolean moveFirst) {
        if (size < 3)
            return;
        if (moveFirst) {
            cornerCandidate[0] = nextIndex(cornerCandidate[0]);
        }
        cornerCandidate[1] = nextIndex(cornerCandidate[0]);
        cornerCandidate[2] = nextIndex(cornerCandidate[1]);
    }

    /**
     * Get the index of the next available shell coordinate starting from the
     * given candidate position.
     * 
     * @param pos candidate position
     * 
     * @return index of the next available shell coordinate
     */
    private int nextIndex(int pos) {
        return shellCoordAvailable[pos];
    }

    public Polygon toGeometry() {
        GeometryFactory fact = new GeometryFactory();
        CoordinateList coordList = new CoordinateList();
        int availIndex = firstAvailable;
        for (int i = 0; i < size; i++) {
            Coordinate v = shellCoords.get(availIndex);
            availIndex = nextIndex(availIndex);
            // if (i < shellCoordAvailable.length && shellCoordAvailable.get(i))
            coordList.add(v, true);
        }
        coordList.closeRing();
        return fact.createPolygon(
                fact.createLinearRing(coordList.toCoordinateArray()), null);
    }
}
