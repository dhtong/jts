package com.vividsolutions.jts.polytriangulate;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import junit.framework.TestCase;

public class PolygonTriangulatorTest extends TestCase {
    protected WKTReader reader = new WKTReader();
    static final double COMPARISON_TOLERANCE = 1.0e-7;

    public PolygonTriangulatorTest(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }
    
    public void testGetEarClipperResultMultiPoly() throws ParseException {
        String original = "GEOMETRYCOLLECTION (POLYGON ((0 0, 4 0, 3 1, 1 2, 0 0)), "
                + "POLYGON ((5 0, 5 -1, 10 -2, 13 5, 5 0)))";
        unionEqualsOrigin(original);
    }
    
    public void testGetEarClipperResultSinglePoly() throws ParseException {
        String original = "POLYGON ((0 0, 4 0, 3 1, 1 2, 0 0))";
        unionEqualsOrigin(original);
    }
    
    /**
     * Check if there is overlap among earclipped triangles. Then union them
     * back to one polygon and compare with the original.
     * 
     * @param result
     *            EarClipped polygon
     * @param original
     *            Before applying EarClipper
     */
    protected void unionEqualsOrigin(Geometry result, Geometry original) {
        int size = result.getNumGeometries();
        Geometry union = result.getGeometryN(0);
        for (int i = 1; i < size; i++) {
            Geometry current = result.getGeometryN(i);
            if (!union.overlaps(current)) {
                union = union.union(current);
            }
        }
        original.normalize();
        union.normalize();
        assertTrue(original.equalsExact(union, COMPARISON_TOLERANCE));
    }
    
    protected void unionEqualsOrigin(String original) throws ParseException {
        Geometry geo = reader.read(original);
        PolygonTriangulator runner = new PolygonTriangulator(geo);
        GeometryCollection col = runner.getEarClipperResult(false);
        unionEqualsOrigin(col, geo.union());
    }
    
    
}
