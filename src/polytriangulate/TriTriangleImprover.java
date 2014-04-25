package com.vividsolutions.jts.polytriangulate;

import java.util.List;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.polytriangulate.tri.TriN;
import com.vividsolutions.jts.triangulate.quadedge.TrianglePredicate;

public class TriTriangleImprover {
    private static int MAX_IMPROVE_SCAN = 20;

    public TriTriangleImprover() {
    }

    public void improve(List<TriN> triList) {
        for (int i = 0; i < MAX_IMPROVE_SCAN; i++) {
            int improveCount = doImprovementScan(triList);
            System.out.println("improve #" + i + " - count = " + improveCount);
            if (improveCount == 0) {
                return;
            }
        }
    }

    /**
     * Attempts to improve the triangulation by examining pairs of triangles
     * with a common edge, forming a quadrilateral, and testing if swapping the
     * diagonal of this quadrilateral would produce two new triangles with
     * larger minimum interior angles.
     * @return the number of improvement flips that were made
     */
    private int doImprovementScan(List<TriN> triList) {
        int improveCount = 0;
        for (int i = 0; i < triList.size() - 1; i++) {
            TriN ear0 = triList.get(i);
            for (int j = 0; j < 3; j++) {
                TriN ear1 = ear0.neighbor(j);
                if (improve(ear0, ear1)) {
                    improveCount++;
                }
            }
        }
        return improveCount;
    }

    private boolean improve(TriN t0, TriN t1) {
        if (t0 == null || t1 == null) {
            return false;
        }
        return flip(t0, t1);
    }

    private boolean flip(TriN tri0, TriN tri1) {
        Coordinate[] adjacentVertices = tri0.getSharedCoordinates(tri1);
        Coordinate adj0 = adjacentVertices[0];
        Coordinate adj1 = adjacentVertices[1];
        Coordinate opp0 = oppositeCoord(tri0, adjacentVertices);
        Coordinate opp1 = oppositeCoord(tri1, adjacentVertices);
        /*
         * The candidate new edge is from opp0 to opp1. First check if this is
         * inside the quadrilateral, which is the case iff the quadrilateral is
         * convex
         */
        if (!isQuadConvex(opp0, adj0, adj1, opp1)) {
            return false;
        }
        /**
         * The candidate edge is inside the quadrilateral. Check to see if the
         * flipping criteria is met. The flipping criteria is to flip iff the
         * two triangles are not Delaunay (i.e. one of the opposite vertices is
         * in the circumcircle of the other triangle).
         */
        boolean doFlip = false;
        if (!isDelaunay(opp0, adj0, adj1, opp1)) {
            doFlip = true;
        }
        if (doFlip) {
            tri0.flip(tri1, adj0, adj1, opp0, opp1);
            return true;
        }
        return false;
    }

    /**
     * Checks if the quadrilateral formed by the two triangles is convex.
     * @param opp0
     * @param adj0
     * @param adj1
     * @param opp1
     * @return
     */
    private boolean isQuadConvex(Coordinate opp0, Coordinate adj0,
            Coordinate adj1, Coordinate opp1) {
        int dir0 = CGAlgorithms.orientationIndex(opp0, opp1, adj0);
        int dir1 = CGAlgorithms.orientationIndex(opp0, opp1, adj1);
        boolean isQuadConvex = dir0 == -dir1;
        if (!isQuadConvex) {
            return false;
        }
        return true;
    }

    private Coordinate oppositeCoord(TriN tri, Coordinate[] sharedVertices) {
        for (int i = 0; i < 3; i++) {
            Coordinate curr = tri.getCoordinate(i);
            if (!curr.equals(sharedVertices[0])
                    && !curr.equals(sharedVertices[1])) {
                return curr;
            }
        }
        return null;
    }

    private boolean isDelaunay(Coordinate c0, Coordinate adj0, Coordinate adj1,
            Coordinate c1) {
        return !(isInCircle(c0, adj0, adj1, c1) || isInCircle(c1, adj1, adj0,
                c0));
    }

    private boolean isInCircle(Coordinate a, Coordinate b, Coordinate c,
            Coordinate p) {
        if (isCCW(a, b, c)) {
            return TrianglePredicate.isInCircleRobust(a, b, c, p);
        }
        return TrianglePredicate.isInCircleRobust(a, c, b, p);
    }

    public final boolean isCCW(Coordinate a, Coordinate b, Coordinate c) {
        return CGAlgorithms.computeOrientation(a, b, c) == CGAlgorithms.COUNTERCLOCKWISE;
    }
}