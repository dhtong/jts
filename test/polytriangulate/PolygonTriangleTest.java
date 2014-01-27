package com.vividsolutions.jts.polytriangulate;

import junit.framework.TestCase;

public class PolygonTriangleTest extends TestCase {

    public PolygonTriangleTest(String name) {
        super(name);
    }

    public void testBasicConstruct() {
        PolygonTriangle triangle = new PolygonTriangle(0, 0, 0);
        int[] v = { 10, 10, 13 };
        triangle.setVertices(v[0], v[1], v[2]);
        checkVertices(v, triangle.getVertices());
    }

    public void testGetDeepCopy() {
        int[] v = { 1, 2, 3 };
        PolygonTriangle triangle = new PolygonTriangle(v[0], v[1], v[2]);
        int[] vReturned = triangle.getVertices();
        // modify returned vertices.
        for (int i = 0; i < 3; i++) {
            vReturned[i] = v[i] + 1;
        }
        checkVertices(v, triangle.getVertices());
    }

    public void testShared() {
        PolygonTriangle tri1 = new PolygonTriangle(1, 1, 2);
        PolygonTriangle tri2 = new PolygonTriangle(1, 2, 3);
        int[] common1 = { 1, 1, 2 };
        checkVertices(common1, tri1.getSharedVertices(tri2));
        int[] common2 = { 1, 2 };
        checkVertices(common2, tri2.getSharedVertices(tri1));
    }

    protected void checkVertices(int[] a, int[] b) {
        if (a.length != b.length) {
            fail("Vertices in different dimensions.");
        }
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
    }
}
