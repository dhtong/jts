package com.vividsolutions.jts.polytriangulate;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class PolygonTriangulatorTest extends TestCase {
    protected WKTReader reader = new WKTReader();

    PolygonTriangulatorTest(String name) {
        super(name);
    }

   
    public void testGetEarClipperResultMultiPoly() throws ParseException {
        String original = "GEOMETRYCOLLECTION (POLYGON ((0 0, 4 0, 3 1, 1 2, 0 0)), "
                + "POLYGON ((5 0, 5 -1, 10 -2, 13 5, 5 0)))";
        System.out.println("dd");
        Geometry geo = reader.read(original);
        System.out.println(geo);
        PolygonTriangulator runner = new PolygonTriangulator(geo);
        runner.getEarClipperResult(false);
        
    }

    public void testBreakDownCollection() {
        //fail("Not yet implemented");
    }
}
