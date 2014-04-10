package com.vividsolutions.jts.polytriangulate.tri;

import java.util.HashSet;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.triangulate.quadedge.Vertex;

public class TriN {
    private Coordinate p0;
    private Coordinate p1;
    private Coordinate p2;
    /**
     * triN is the neighbour triangle across the edge pN - pNN
     * pNN is the next vertex CW from pN
     */
    // private TriN tri0;
    // private TriN tri1;
    // private TriN tri2;
    private TriN[] neighbors;

    public TriN(Coordinate p0, Coordinate p1, Coordinate p2) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        neighbors = new TriN[3];
    }

    public void setNeighbours(TriN tri0, TriN tri1, TriN tri2) {
        // this.tri0 = tri0;
        // this.tri1 = tri1;
        // this.tri2 = tri2;
        neighbors[0] = tri0;
        neighbors[1] = tri1;
        neighbors[2] = tri2;
    }

    public void setCoordinate(Coordinate p0, Coordinate p1, Coordinate p2) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
    }

    public boolean addNeighbour(TriN tri) {
        for (int i = 0; i < 3; i++) {
            if (neighbors[i] == null) {
                neighbors[i] = tri;
                return true;
            }
        }
        return false;
    }

    /**
     * Swap triOld with triNew
     * @param triOld
     * @param triNew
     */
    public void updateNeighbor(TriN triOld, TriN triNew) {
        for (int i = 0; i < 3; i++) {
            if (neighbors[i] != null && triOld.equals(neighbors[i])) {
                neighbors[i] = triNew;
                return;
            }
        }
    }

    /**
     * Spits a triangle by a point located inside the triangle.
     * Returns a new triangle whose 0'th vertex is p
     * @param p
     *            the point to insert
     * @return a new triangle whose 0'th vertex is p
     */
    public TriN split(Coordinate p) {
        TriN tt0 = new TriN(p, p0, p1);
        TriN tt1 = new TriN(p, p1, p2);
        TriN tt2 = new TriN(p, p2, p0);
        tt0.setNeighbours(tt2, neighbors[0], tt1);
        tt1.setNeighbours(tt0, neighbors[1], tt2);
        tt2.setNeighbours(tt1, neighbors[2], tt0);
        return tt0;
    }

    /**
     * flip with new edge from opp0 to opp1
     * @param a neighbor of the current Tri
     * @param adj0 coordinate on the adjacent edge
     * @param adj1
     * @param opp0 the other coordinate in current Tri
     * @param opp1 the other coordinate in Tri a
     */
    public void flip(TriN a, Coordinate adj0, Coordinate adj1, Coordinate opp0,
            Coordinate opp1) {
        // Order: 0: opp0-adj0 edge, 1: opp0-adj1 edge, 2: opp1-adj0 edge, 3:
        // opp1-adj1 edge
        TriN[] surrounding = getSurroundingTris(a, adj0, adj1, opp0, opp1);
        this.setCoordinate(adj0, opp0, opp1);
        a.setCoordinate(adj1, opp0, opp1);
        this.setNeighbours(a, surrounding[0], surrounding[2]);
        if (surrounding[2] != null) {
            surrounding[2].updateNeighbor(a, this);
        }
        a.setNeighbours(this, surrounding[1], surrounding[3]);
        if (surrounding[1] != null) {
            surrounding[1].updateNeighbor(this, a);
        }
    }

    public void flip(TriN a) {
        HashSet<Coordinate> aCoords = new HashSet<Coordinate>();
        aCoords.add(a.getCoordinate(0));
        aCoords.add(a.getCoordinate(1));
        aCoords.add(a.getCoordinate(2));
        Coordinate[] adj = new Coordinate[2];
        int curr = 0;
        Coordinate opp0 = null;
        for (int i = 0; i < 3; i++) {
            Coordinate tmp = getCoordinate(i);
            if (aCoords.contains(tmp)) {
                adj[curr] = tmp;
                curr++;
                aCoords.remove(tmp);
            } else {
                opp0 = tmp;
            }
        }
        flip(a, adj[0], adj[1], opp0, (Coordinate) aCoords.toArray()[0]);
    }

    /**
     * Get ordered surrounding Tri's of the current Tri and TriN a
     * @param a
     * @param adj0
     * @param adj1
     * @param opp0
     * @param opp1
     * @return
     */
    private TriN[] getSurroundingTris(TriN a, Coordinate adj0, Coordinate adj1,
            Coordinate opp0, Coordinate opp1) {
        TriN[] surrounding = new TriN[4];
        for (int i = 0; i < 3; i++) {
            TriN curr = neighbor(i);
            if (curr != null) {
                if (curr.hasCoordinate(opp0) && curr.hasCoordinate(adj0)) {
                    surrounding[0] = curr;
                }
                if (curr.hasCoordinate(opp0) && curr.hasCoordinate(adj1)) {
                    surrounding[1] = curr;
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            TriN curr = a.neighbor(i);
            if (curr != null) {
                if (curr.hasCoordinate(opp1) && curr.hasCoordinate(adj0)) {
                    surrounding[2] = curr;
                }
                if (curr.hasCoordinate(opp1) && curr.hasCoordinate(adj1)) {
                    surrounding[3] = curr;
                }
            }
        }
        return surrounding;
    }

    /**
     * Get a specific neighbor Tri of a, which has coord0 and coord1
     * @param a
     * @param coord0
     * @param coord1
     * @return return null if such neighbor is not found.
     */
    public static TriN getNeighborTri(TriN a, Coordinate coord0,
            Coordinate coord1) {
        for (int i = 0; i < 3; i++) {
            TriN curr = a.neighbor(i);
            if (curr.hasCoordinate(coord0) && curr.hasCoordinate(coord1)) {
                return curr;
            }
        }
        return null;
    }

    public Coordinate[] getSharedCoordinates(TriN neighbor) {
        Coordinate[] shared = new Coordinate[2];
        int sharedIndex = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (getCoordinate(i).equals(neighbor.getCoordinate(j))) {
                    shared[sharedIndex] = getCoordinate(i);
                    sharedIndex++;
                }
            }
        }
        return shared;
    }

    public boolean hasCoordinate(Coordinate v) {
        if (p0.equals(v) || p1.equals(v) || p2.equals(v)) {
            return true;
        }
        return false;
    }

    public boolean isRightOf(int iedge, Vertex v) {
        Coordinate e0 = getCoordinate(iedge);
        Coordinate e1 = getCoordinate(next(iedge));
        return CGAlgorithms.COUNTERCLOCKWISE == CGAlgorithms.orientationIndex(
                e0, e1, v.getCoordinate());
    }

    public Coordinate getCoordinate(int i) {
        if (i == 0) {
            return p0;
        }
        if (i == 1) {
            return p1;
        }
        return p2;
    }

    public TriN neighbor(int i) {
        return neighbors[i];
    }

    public TriN sym(int i) {
        return neighbor(i);
    }

    public static int next(int i) {
        switch (i) {
        case 0:
            return 1;
        case 1:
            return 2;
        case 2:
            return 0;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Tri{" + p0 + ", " + p1 + ", " + p2 + "}";
    }
}
