package com.vividsolutions.jts.polytriangulate;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
     * @param inputPolygon
     *            the input polygon
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
            // if (isValidEar(k0, k1, k2)) {
            // if (isValidEarFast(iEar[0], iEar[1], iEar[2])) {
            if (isValidEarSlow(iEar[0], iEar[1], iEar[2])) {
                foundEar = true;
                // System.out.println(earPoly);
                PolygonTriangle ear = new PolygonTriangle(iEar[0], iEar[1],
                        iEar[2]);
                triList.add(ear);
                polyShell.remove(iEar[1]);
                if (polyShell.size() < 3) {
                    return;
                }
                polyShell.nextCorner(iEar[0], iEar);
                cornerCount = 0;
            } else {
                polyShell.nextCorner(iEar[0] + 1, iEar);
            }
        } while (!finished);
    }
    /**
     * Check if the inputs are in the same line
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

    private boolean isValidEarFast(int k0, int k1, int k2) {
        Coordinate[] triRing = new Coordinate[] { polyShell.getCoordinate(k0),
                polyShell.getCoordinate(k1), polyShell.getCoordinate(k2),
                polyShell.getCoordinate(k0) };
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
     * @param t
     *            the triangle
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
    private boolean[] shellCoordAvailable;
    private int size;

    public PolygonShellSlow(List<Coordinate> shellCoords) {
        this.shellCoords = shellCoords;
        shellCoordAvailable = new boolean[shellCoords.size() - 1];
        Arrays.fill(shellCoordAvailable, true);
        size = shellCoordAvailable.length;
    }

    public int size() {
        return size;
    }

    public void remove(int i) {
        shellCoordAvailable[i] = false;
        size--;
    }

    public Coordinate getCoordinate(int i) {
        return shellCoords.get(i);
    }

    public void nextCorner(int i, int[] iVert) {
        if (!shellCoordAvailable[i % shellCoordAvailable.length])
            i = nextIndex(i);
        iVert[0] = i;
        iVert[1] = nextIndex(iVert[0]);
        iVert[2] = nextIndex(iVert[1]);
    }

    /**
     * Get the index of the next available shell coordinate starting from the
     * given candidate position.
     * 
     * @param pos
     *            candidate position
     * 
     * @return index of the next available shell coordinate
     */
    private int nextIndex(int pos) {
        int posNext = (pos + 1) % shellCoordAvailable.length;
        while (!shellCoordAvailable[posNext]) {
            posNext = (posNext + 1) % shellCoordAvailable.length;
        }
        return posNext;
    }

    public Polygon toGeometry() {
        GeometryFactory fact = new GeometryFactory();
        CoordinateList coordList = new CoordinateList();
        for (int i = 0; i < shellCoords.size(); i++) {
            if (i < shellCoordAvailable.length && shellCoordAvailable[i])
                coordList.add(getCoordinate(i), false);
        }
        coordList.closeRing();
        return fact.createPolygon(
                fact.createLinearRing(coordList.toCoordinateArray()), null);
    }
}

class PolygonShell {
    /**
     * The shell coordinates are maintain in CW order. This means that for
     * convex interior angles, the vertices forming the angle are in CW
     * orientation.
     */
    private List<Coordinate> shellCoords;
    private boolean[] shellCoordAvailable;
    private int size;

    public PolygonShell(List<Coordinate> shellCoords) {
        this.shellCoords = shellCoords;
        shellCoordAvailable = new boolean[shellCoords.size() - 1];
        Arrays.fill(shellCoordAvailable, true);
        size = shellCoordAvailable.length;
    }

    public int size() {
        return size;
    }

    public void remove(int i) {
        shellCoordAvailable[i] = false;
        size--;
    }

    public Coordinate getCoordinate(int i) {
        return shellCoords.get(i);
    }

    public void nextCorner(int i, int[] iVert) {
        if (!shellCoordAvailable[i % shellCoordAvailable.length])
            i = nextIndex(i);
        iVert[0] = i;
        iVert[1] = nextIndex(iVert[0]);
        iVert[2] = nextIndex(iVert[1]);
    }

    /**
     * Get the index of the next available shell coordinate starting from the
     * given candidate position.
     * 
     * @param pos
     *            candidate position
     * 
     * @return index of the next available shell coordinate
     */
    private int nextIndex(int pos) {
        int posNext = (pos + 1) % shellCoordAvailable.length;
        while (!shellCoordAvailable[posNext]) {
            posNext = (posNext + 1) % shellCoordAvailable.length;
        }
        return posNext;
    }
}

class HoleJoiner {
    private static final double EPS = 1.0E-4;
    private final GeometryFactory gf;
    private PreparedGeometry inputPrepGeom;
    private List<Coordinate> shellCoords;

    public HoleJoiner(PreparedGeometry inputPrepGeom) {
        this.inputPrepGeom = inputPrepGeom;
        gf = inputPrepGeom.getGeometry().getFactory();
    }

    public void joinHoles(List<Coordinate> shellCoords) {
        this.shellCoords = shellCoords;
        List<Geometry> orderedHoles = getOrderedHoles((Polygon) inputPrepGeom
                .getGeometry());
        for (int i = 0; i < orderedHoles.size(); i++) {
            joinHoleToShell(orderedHoles.get(i));
        }
    }

    /**
     * Join a given hole to the current shell. The hole coordinates are inserted
     * into the list of shell coordinates.
     * 
     * @param hole
     *            the hole to join
     */
    private void joinHoleToShell(Geometry hole) {
        double minD2 = Double.MAX_VALUE;
        int shellVertexIndex = -1;
        final int Ns = shellCoords.size() - 1;
        final int holeVertexIndex = getLowestVertex(hole);
        final Coordinate[] holeCoords = hole.getCoordinates();
        final Coordinate ch = holeCoords[holeVertexIndex];
        List<IndexedDouble> distanceList = new ArrayList<IndexedDouble>();
        /*
         * Note: it's important to scan the shell vertices in reverse so that if
         * a hole ends up being joined to what was originally another hole, the
         * previous hole's coordinates appear in the shell before the new hole's
         * coordinates (otherwise the triangulation algorithm tends to get
         * stuck).
         */
        for (int i = Ns - 1; i >= 0; i--) {
            Coordinate cs = shellCoords.get(i);
            double d2 = (ch.x - cs.x) * (ch.x - cs.x) + (ch.y - cs.y)
                    * (ch.y - cs.y);
            if (d2 < minD2) {
                minD2 = d2;
                shellVertexIndex = i;
            }
            distanceList.add(new IndexedDouble(i, d2));
        }
        /*
         * Try a quick join: if the closest shell vertex is reachable without
         * crossing any holes.
         */
        LineString join = gf.createLineString(new Coordinate[] { ch,
                shellCoords.get(shellVertexIndex) });
        if (inputPrepGeom.covers(join)) {
            doJoinHole(shellVertexIndex, holeCoords, holeVertexIndex);
            return;
        }
        /*
         * Quick join didn't work. Sort the shell coords on distance to the hole
         * vertex and choose the closest reachable one.
         */
        Collections.sort(distanceList, new IndexedDoubleComparator());
        for (int i = 1; i < distanceList.size(); i++) {
            join = gf.createLineString(new Coordinate[] { ch,
                    shellCoords.get(distanceList.get(i).index) });
            if (inputPrepGeom.covers(join)) {
                shellVertexIndex = distanceList.get(i).index;
                doJoinHole(shellVertexIndex, holeCoords, holeVertexIndex);
                return;
            }
        }
        throw new IllegalStateException("Failed to join hole to shell");
    }

    /**
     * Helper method for joinHoleToShell. Insert the hole coordinates into the
     * shell coordinate list.
     * 
     * @param shellCoords
     *            list of current shell coordinates
     * @param shellVertexIndex
     *            insertion point in the shell coordinate list
     * @param holeCoords
     *            array of hole coordinates
     * @param holeVertexIndex
     *            attachment point of hole
     */
    private void doJoinHole(int shellVertexIndex, Coordinate[] holeCoords,
            int holeVertexIndex) {
        List<Coordinate> newCoords = new ArrayList<Coordinate>();
        List<Integer> newRingIndices = new ArrayList<Integer>();
        newCoords.add(new Coordinate(shellCoords.get(shellVertexIndex)));
        final int N = holeCoords.length - 1;
        int i = holeVertexIndex;
        do {
            newCoords.add(new Coordinate(holeCoords[i]));
            i = (i + 1) % N;
        } while (i != holeVertexIndex);
        newCoords.add(new Coordinate(holeCoords[holeVertexIndex]));
        shellCoords.addAll(shellVertexIndex, newCoords);
    }

    /**
     * Returns a list of holes in the input polygon (if any) ordered by y
     * coordinate with ties broken using x coordinate.
     * 
     * @param poly
     *            input polygon
     * @return a list of Geometry objects representing the ordered holes (may be
     *         empty)
     */
    private List<Geometry> getOrderedHoles(final Polygon poly) {
        List<Geometry> holes = new ArrayList<Geometry>();
        List<IndexedEnvelope> bounds = new ArrayList<IndexedEnvelope>();
        if (poly.getNumInteriorRing() > 0) {
            for (int i = 0; i < poly.getNumInteriorRing(); i++) {
                bounds.add(new IndexedEnvelope(i, poly.getInteriorRingN(i)
                        .getEnvelopeInternal()));
            }
            Collections.sort(bounds, new IndexedEnvelopeComparator());
            for (int i = 0; i < bounds.size(); i++) {
                holes.add(poly.getInteriorRingN(bounds.get(i).index));
            }
        }
        return holes;
    }

    /**
     * Return the index of the lowest vertex
     * 
     * @param geom
     *            input geometry
     * @return index of the first vertex found at lowest point of the geometry
     */
    private int getLowestVertex(Geometry geom) {
        Coordinate[] coords = geom.getCoordinates();
        double minY = geom.getEnvelopeInternal().getMinY();
        for (int i = 0; i < coords.length; i++) {
            if (Math.abs(coords[i].y - minY) < EPS) {
                return i;
            }
        }
        throw new IllegalStateException("Failed to find lowest vertex");
    }

    private static class IndexedEnvelope {
        int index;
        Envelope envelope;

        public IndexedEnvelope(int i, Envelope env) {
            index = i;
            envelope = env;
        }
    }

    private static class IndexedEnvelopeComparator implements
            Comparator<IndexedEnvelope> {
        public int compare(IndexedEnvelope o1, IndexedEnvelope o2) {
            double delta = o1.envelope.getMinY() - o2.envelope.getMinY();
            if (Math.abs(delta) < EPS) {
                delta = o1.envelope.getMinX() - o2.envelope.getMinX();
                if (Math.abs(delta) < EPS) {
                    return 0;
                }
            }
            return (delta > 0 ? 1 : -1);
        }
    }

    private static class IndexedDouble {
        int index;
        double value;

        public IndexedDouble(int i, double v) {
            index = i;
            value = v;
        }
    }

    private static class IndexedDoubleComparator implements
            Comparator<IndexedDouble> {
        public int compare(IndexedDouble o1, IndexedDouble o2) {
            double delta = o1.value - o2.value;
            if (Math.abs(delta) < EPS) {
                return 0;
            }
            return (delta > 0 ? 1 : -1);
        }
    }
}
