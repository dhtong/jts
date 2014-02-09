package com.vividsolutions.jts.polytriangulate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;

class HoleJoinerO {
    private static final double EPS = 1.0E-4;
    private final GeometryFactory gf;
    private PreparedGeometry inputPrepGeom;
    private List<Coordinate> shellCoords;

    public HoleJoinerO(PreparedGeometry inputPrepGeom) {
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