package com.vividsolutions.jts.polytriangulate.tri;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Coordinate;

public class TriNTest extends TestCase {
    public TriNTest(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    public void testFlip() {
        Coordinate coord1 = new Coordinate(7, 9);
        Coordinate coord2 = new Coordinate(0, 4);
        Coordinate coord3 = new Coordinate(5, 0);
        Coordinate coord4 = new Coordinate(0, 0);
        Coordinate coord5 = new Coordinate(2, -6);
        TriN top = new TriN(coord1, coord2, coord3);
        TriN mid = new TriN(coord4, coord2, coord3);
        TriN botm = new TriN(coord5, coord3, coord4);
        top.setNeighbours(null, null, mid);
        mid.setNeighbours(top, null, botm);
        botm.setNeighbours(null, mid, null);
        mid.flip(top);
        System.out.println("top: " + top.toString());
        for (int i = 0; i < 3; i++) {
            System.out.println(top.neighbor(i));
        }
        System.out.println("bottom: " + botm.toString());
        for (int i = 0; i < 3; i++) {
            System.out.println(botm.neighbor(i));
        }
    }
}
