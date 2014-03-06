package com.vividsolutions.jts.polytriangulate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Triangle;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

public class EarClipper {
    private final GeometryFactory gf;
    private final Polygon inputPolygon;
    private PreparedGeometry inputPrepGeom;
    private Geometry triangulation;
    /**
     * The shell coordinates are maintain in CW order. This means that for
     * convex interior angles, the vertices forming the angle are in CW
     * orientation.
     */
    private PolygonShellSlow polyShell;
    private List<Coordinate> polyShellCoords;
    private List<PolygonTriangle> triList;
    private boolean isImprove = true;

    /**
     * Constructor
     * 
     * @param inputPolygon the input polygon
     */
    public EarClipper(Polygon inputPolygon) {
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
        int numCorners = polyShell.size() - 1;
        boolean finished = false;
        boolean foundEar = false;
        int[] iEar = new int[3];
        // int k0 = 0;
        // int k1 = 1;
        // int k2 = 2;
        int firstK = 0;
        polyShell.nextCorner(0, iEar);
        int cornerCount = 0;
        do {
            foundEar = false;
            // find next convex corner (which is the next candidate ear)
            while (CGAlgorithms.computeOrientation(
                    polyShell.getCoordinate(iEar[0]),
                    polyShell.getCoordinate(iEar[1]),
                    polyShell.getCoordinate(iEar[2])) != CGAlgorithms.CLOCKWISE) {
                // delete the "corner" if three points are in the same line
                if (inLine(polyShell.getCoordinate(iEar[0]),
                        polyShell.getCoordinate(iEar[1]),
                        polyShell.getCoordinate(iEar[2]))) {
                    polyShell.remove(iEar[1]);
                    if (polyShell.size() < 3) {
                        return;
                    }
                }
                polyShell.nextCorner(iEar[0] + 1, iEar);
            }
            cornerCount++;
            if (cornerCount > 2 * polyShell.size()) {
                throw new IllegalStateException(
                        "Unable to find a convex corner which is a valid ear");
            }
            // if (isValidEarFast(iEar[0], iEar[1], iEar[2])) {
            if (isValidEarSlow(iEar[0], iEar[1], iEar[2])) {
                foundEar = true;
                PolygonTriangle ear = new PolygonTriangle(iEar[0], iEar[1],
                        iEar[2]);
                triList.add(ear);
                polyShell.remove(iEar[1]);
                if (polyShell.size() < 3) {
                    return;
                }
                polyShell.nextCorner(iEar[0], iEar);
                cornerCount = 0;
            }
            else {
                polyShell.nextCorner(iEar[0] + 1, iEar);
            }
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
    private boolean isValidEarSlow(int k0, int k1, int k2) {
        // if (!isValidEdge(k0, k2))
        // return false;
        LineString ls = gf.createLineString(new Coordinate[] {
                polyShell.getCoordinate(k0), polyShell.getCoordinate(k2) });
        if (!inputPrepGeom.covers(ls))
            return false;
        Polygon earPoly = gf.createPolygon(
                gf.createLinearRing(new Coordinate[] {
                        polyShell.getCoordinate(k0),
                        polyShell.getCoordinate(k1),
                        polyShell.getCoordinate(k2),
                        polyShell.getCoordinate(k0) }), null);
        if (inputPrepGeom.covers(earPoly))
            return true;
        return false;
    }

    /**
     * This method has problems with holes.
     * @param k0
     * @param k1
     * @param k2
     * @return
     */
    private boolean isValidEarFast(int k0, int k1, int k2) {
        Coordinate[] triRing = new Coordinate[] { polyShell.getCoordinate(k0),
                polyShell.getCoordinate(k1), polyShell.getCoordinate(k2),
                polyShell.getCoordinate(k0) };
        Coordinate centroid = Triangle.centroid(polyShell.getCoordinate(k0),
                polyShell.getCoordinate(k1), polyShell.getCoordinate(k2));
        Point centroPoint = gf.createPoint(centroid);
        if(!centroPoint.within(inputPolygon)){
            return false;
        }
        //TODO: This would not work. Some vertices could have been removed.
        int n = polyShellCoords.size();
        for (int i = 0; i < n; i++) {
            Coordinate v = polyShellCoords.get(i);
            // skip if vertex is a triangle vertex
            if (v.equals2D(triRing[0]))
                continue;
            if (v.equals2D(triRing[1]))
                continue;
            if (v.equals2D(triRing[2]))
                continue;
            // not valid if vertex is contained in tri
            if (CGAlgorithms.isPointInRing(v, triRing))
                return false;
        }
        return true;
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
        polyShell = new PolygonShellSlow(polyShellCoords);
    }
}

class PolygonShellSlow {
    /**
     * The shell coordinates are maintain in CW order. This means that for
     * convex interior angles, the vertices forming the angle are in CW
     * orientation.
     */
    private List<Coordinate> shellCoords;
    //private boolean[] shellCoordAvailable;
    private int size;
    private BitSet shellCoordAvailable;

    public PolygonShellSlow(List<Coordinate> shellCoords) {
        this.shellCoords = shellCoords;
        /*shellCoordAvailable = new boolean[shellCoords.size() - 1];
        Arrays.fill(shellCoordAvailable, true);*/
        size = shellCoords.size();
        shellCoordAvailable = new BitSet(size);
        shellCoordAvailable.set(0, size);        
    }

    public int size() {
        return size;
    }

    public void remove(int i) {
        //shellCoordAvailable[i] = false;
        shellCoordAvailable.set(i, false);
        size--;
    }

    public Coordinate getCoordinate(int i) {
        return shellCoords.get(i);
    }

    public void nextCorner(int i, int[] iVert) {
        i = i % shellCoordAvailable.size();
        if (!shellCoordAvailable.get(i))
            i = nextIndex(i);
        iVert[0] = i;
        iVert[1] = nextIndex(iVert[0]);
        iVert[2] = nextIndex(iVert[1]);
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
        /*int posNext = (pos + 1) % shellCoordAvailable.length;
        while (!shellCoordAvailable[posNext]) {
            posNext = (posNext + 1) % shellCoordAvailable.length;
        }
        return posNext;*/
        int next = (pos + 1) % shellCoordAvailable.size();
        next = shellCoordAvailable.nextSetBit(next);
        if(next == -1)
            next = shellCoordAvailable.nextSetBit(0);        
        return next;
    }

    public Polygon toGeometry() {
        GeometryFactory fact = new GeometryFactory();
        CoordinateList coordList = new CoordinateList();
        for (int i = 0; i < shellCoords.size(); i++) {
            if (i < shellCoordAvailable.size() && shellCoordAvailable.get(i))
                coordList.add(getCoordinate(i), false);
        }
        coordList.closeRing();
        return fact.createPolygon(
                fact.createLinearRing(coordList.toCoordinateArray()), null);
    }
}

