package com.vividsolutions.jts.polytriangulate.tri;

import java.util.HashMap;

import com.vividsolutions.jts.geom.Coordinate;

public class Triangulation {
    private HashMap<TriEdge, TriN> triMap;
    public Triangulation(){
        triMap = new HashMap<TriEdge, TriN>();
    }
    public TriN find(TriEdge a) {
        return triMap.get(a);
    }
    
    /**
     * Add triangle represented by coords to TriMap and update its neighbors
     * @param coords
     * @return
     */
    public TriN add(Coordinate[] coords) {
        TriN tri = new TriN(coords[0], coords[1], coords[2]);
        TriEdge a = new TriEdge(coords[0], coords[1]);
        TriEdge b = new TriEdge(coords[1], coords[2]);
        TriEdge c = new TriEdge(coords[0], coords[2]);
        TriEdge[] edges = { a, b, c };
        // get neighbors
        TriN[] neighbors = { find(a), find(b), find(c) };
        tri.setNeighbours(neighbors[0], neighbors[1], neighbors[2]);
        for (int i = 0; i < 3; i++) {
            if (neighbors[i] != null) {
                neighbors[i].addNeighbour(tri);
            } else {
                // System.out.println(i);
                triMap.put(edges[i], tri);
            }
        }
        return tri;
    }
}
