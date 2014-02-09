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

public class HoleJoiner {
    private static final double EPS = 1.0E-4;
    private final GeometryFactory gf;
    private PreparedGeometry inputPrepGeom;
    private List<Coordinate> shellCoords;
    // orderedCoords a copy of shellCoords for sort purpose
    private List<Coordinate> orderedCoords;

    public HoleJoiner(PreparedGeometry inputPrepGeom) {
        this.inputPrepGeom = inputPrepGeom;
        gf = inputPrepGeom.getGeometry().getFactory();
        orderedCoords = new ArrayList<Coordinate>();
    }

    /**
     * @param shellCoords
     *            Shell Coordinates of the polygon.
     */
    public void joinHoles(List<Coordinate> shellCoords) {
        this.shellCoords = shellCoords;
        orderedCoords.addAll(shellCoords);
        List<Geometry> orderedHoles = getOrderedHoles((Polygon) inputPrepGeom
                .getGeometry());
        for (int i = 0; i < orderedHoles.size(); i++) {
            joinHoleToShell(orderedHoles.get(i));
        }
    }

    /**
     * Join the current hole to the polygon
     * 
     * @param hole
     */
    private void joinHoleToShell(Geometry hole) {
        final Coordinate[] holeCoords = hole.getCoordinates();
        final int holeVertexIndex = getLeftMostVertex(hole);
        final Coordinate holeCoord = holeCoords[holeVertexIndex];
        Coordinate shellCoord = getLeftShellVertex(holeCoord);
        int shellVertexIndex = getIndexInShellCoords(shellCoord);
        doJoinHole(shellVertexIndex, holeCoords, holeVertexIndex);
    }

    /**
     * Find the index of the coordinate in ShellCoords ArrayList
     * 
     * @param coord
     * @return
     */
    private int getIndexInShellCoords(Coordinate coord) {
        for (int i = 0; i < shellCoords.size(); i++) {
            if (shellCoords.get(i).equals2D(coord, EPS)) {
                return i;
            }
        }
        throw new IllegalStateException("Request vertex is not in sheelcoords");
    }

    /**
     * Find the shell coordinate which is 1. closest to the left most vertex of
     * the hole 2. on the left of the hole 3. lingstring is covered 4. in
     * ShellCoords list
     * 
     * @param holeCoord
     * @return
     */
    private Coordinate getLeftShellVertex(Coordinate holeCoord) {
        Collections.sort(orderedCoords);
        double holeX = holeCoord.x;
        int prevBiggest = 0;
        // Advanced approach needed here.
        while (orderedCoords.get(prevBiggest).x <= holeX) {
            ++prevBiggest;
        }
        do {
            --prevBiggest;
        } while (!joinAble(holeCoord, orderedCoords.get(prevBiggest))
                && prevBiggest >= 0);
        if (prevBiggest < 0)
            throw new IllegalStateException(
                    "Failed to find vertex on shell to join");
        return orderedCoords.get(prevBiggest);
    }

    /**
     * Determine if a linestring between two coordinates is covered
     * 
     * @param holeCoord
     * @param shellCoord
     * @return
     */
    private boolean joinAble(Coordinate holeCoord, Coordinate shellCoord) {
        LineString join = gf.createLineString(new Coordinate[] { holeCoord,
                shellCoord });
        if (inputPrepGeom.covers(join)) {
            return true;
        }
        return false;
    }

    /**
     * Add holeCoords to proper position. update ShellCoords and OrderedCoords
     * 
     * @param shellVertexIndex
     * @param holeCoords
     * @param holeVertexIndex
     */
    private void doJoinHole(int shellVertexIndex, Coordinate[] holeCoords,
            int holeVertexIndex) {
        List<Coordinate> newCoords = new ArrayList<Coordinate>();
        newCoords.add(new Coordinate(shellCoords.get(shellVertexIndex)));
        final int N = holeCoords.length - 1;
        int i = holeVertexIndex;
        do {
            newCoords.add(new Coordinate(holeCoords[i]));
            i = (i + 1) % N;
        } while (i != holeVertexIndex);
        newCoords.add(new Coordinate(holeCoords[holeVertexIndex]));
        shellCoords.addAll(shellVertexIndex, newCoords);
        orderedCoords.addAll(newCoords);
    }

    /**
     * Ordered the holes by left most vertex's x value.
     * 
     * @param poly
     *            polygon that contains all the holes.
     * @return list of ordered hole geometry
     */
    private List<Geometry> getOrderedHoles(final Polygon poly) {
        List<Geometry> holes = new ArrayList<Geometry>();
        if (poly.getNumInteriorRing() > 0) {
            for (int i = 0; i < poly.getNumInteriorRing(); i++) {
                holes.add(poly.getInteriorRingN(i));
            }
            Collections.sort(holes, new EnvelopeComparator());
        }
        return holes;
    }

    /**
     * Get index of the leftmost vertex in hole, if same x, vertex with smaller
     * y value will be returned
     * 
     * @param geom
     *            hole
     * @return index of the left most vertex
     */
    private int getLeftMostVertex(Geometry geom) {
        Coordinate[] coords = geom.getCoordinates();
        double minX = geom.getEnvelopeInternal().getMinX();
        for (int i = 0; i < coords.length; i++) {
            if (Math.abs(coords[i].x - minX) < EPS) {
                return i;
            }
        }
        throw new IllegalStateException("Failed to find left most vertex");
    }

    private static class EnvelopeComparator implements Comparator<Geometry> {
        public int compare(Geometry o1, Geometry o2) {
            Envelope e1 = o1.getEnvelopeInternal();
            Envelope e2 = o2.getEnvelopeInternal();
            if (e1.getMinX() < e2.getMinX())
                return -1;
            if (e1.getMinX() > e2.getMinX())
                return 1;
            if (e1.getMinY() < e2.getMinY())
                return -1;
            if (e1.getMinY() < e2.getMinY())
                return 1;
            return 0;
        }
    }
}
