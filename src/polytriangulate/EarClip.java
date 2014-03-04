package com.vividsolutions.jts.polytriangulate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

public class EarClip {
    private final GeometryFactory gf;
    private final Polygon inputPolygon;
    private PreparedGeometry inputPrepGeom;
    private Geometry triangulation;
    /**
     * The shell coordinates are maintain in CW order. This means that for
     * convex interior angles, the vertices forming the angle are in CW
     * orientation.
     */
    private PolygonShell poShell;
    private List<Coordinate> polyShellCoords;
    private List<PolyTriangle> triList;
    private boolean isImprove = true;

    /**
     * Constructor
     * 
     * @param inputPolygon the input polygon
     */
    public EarClip(Polygon inputPolygon) {
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
        triList = new ArrayList<PolyTriangle>();
        createShell();
        computeEars();
        // improve triangulation if required
        if (isImprove) {
            // TODO: implement improver
            /*
             * TriangleImprover improver = new
             * TriangleImprover(polyShellCoords); improver.improve(triList);
             */
        }
        return createResult();
    }

    private void computeEars() {
        boolean finished = false;
        boolean foundEar = false;
        poShell.nextCorner();
        int cornerCount = 0;
        do {
            foundEar = false;
            // find next convex corner (which is the next candidate ear)
            Coordinate[] currCorner = poShell.getCorner();
            while (CGAlgorithms.computeOrientation(currCorner[0],
                    currCorner[1], currCorner[2]) != CGAlgorithms.CLOCKWISE) {
                // delete the "corner" if three points are in the same line
                if (inLine(currCorner[0], currCorner[1], currCorner[2])) {
                    poShell.remove();
                    if (poShell.size() < 3) {
                        return;
                    }
                }
                currCorner = poShell.nextCorner();
            }
            cornerCount++;
            if (cornerCount > 2 * poShell.size()) {
                throw new IllegalStateException(
                        "Unable to find a convex corner which is a valid ear");
            }
            // if (isValidEarFast(iEar[0], iEar[1], iEar[2])) {
            if (isValidEarSlow()) {
                foundEar = true;
                PolyTriangle ear = new PolyTriangle(currCorner[0],
                        currCorner[1], currCorner[2]);
                triList.add(ear);
                poShell.remove();
                if (poShell.size() < 3) {
                    return;
                }
                cornerCount = 0;
            }
            else {
                currCorner = poShell.nextCorner();
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
    private boolean isValidEarSlow() {
        Coordinate[] corner = poShell.getCorner();
        LineString ls = gf.createLineString(new Coordinate[] { corner[0],
                corner[2] });
        if (!inputPrepGeom.covers(ls))
            return false;
        Polygon earPoly = gf.createPolygon(
                gf.createLinearRing(new Coordinate[] { corner[0], corner[1],
                        corner[2], corner[0] }), null);
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
    private Polygon createPolygon(final PolyTriangle t) {
        final Coordinate[] vertices = t.getVertices();
        return gf.createPolygon(
                gf.createLinearRing(new Coordinate[] { vertices[0],
                        vertices[1], vertices[2], vertices[0] }), null);
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
            //this function does not modify polyShellCoords
            holeJoiner.joinHoles(polyShellCoords);
        }
        poShell = new PolygonShell(polyShellCoords);
    }
}

class PolygonShell {
    /**
     * The shell coordinates are maintain in CW order. This means that for
     * convex interior angles, the vertices forming the angle are in CW
     * orientation.
     */
    private List<Coordinate> shellCoordAvailable;
    private int currIndex;

    public PolygonShell(List<Coordinate> shellCoords) {
        shellCoordAvailable = shellCoords;
        currIndex = -1;
    }

    public int size() {
        return shellCoordAvailable.size();
    }

    /**
     * Always remove the middle one
     * @param i
     */
    public void remove() {
        shellCoordAvailable.remove((currIndex + 1) % size());
    }

    public Coordinate[] getCorner() {
        return new Coordinate[] { shellCoordAvailable.get(currIndex % size()),
                shellCoordAvailable.get((currIndex + 1) % size()),
                shellCoordAvailable.get((currIndex + 2) % size()) };
    }

    public Coordinate[] nextCorner() {
        currIndex++;
        return getCorner();
    }
}