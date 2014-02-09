package com.vividsolutions.jts.polytriangulate;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

public class EarClipperTest extends TestCase {
    protected WKTReader reader = new WKTReader();
    protected WKTWriter writer = new WKTWriter();
    static final double COMPARISON_TOLERANCE = 1.0e-7;
    GeometryFactory fact = new GeometryFactory();

    public EarClipperTest(String name) {
        super(name);
    }

    public void testTriangle() throws ParseException {
        String triangleStr = "POLYGON ((10 20, 10 10, 20 20, 10 20))";
        String expected = "GEOMETRYCOLLECTION (POLYGON ((10 10, 20 20, 10 20, 10 10)))";
        runCompare(triangleStr, expected);
    }

    public void testSquare() throws ParseException {
        String squareStr = "POLYGON ((3 -1, 2 2, 5 3, 6 0, 3 -1))";
        String expected = "GEOMETRYCOLLECTION (POLYGON ((2 2, 5 3, 6 0, 2 2)), "
                + "POLYGON ((2 2, 3 -1, 6 0, 2 2)))";
        runCompare(squareStr, expected);
    }

    public void testRectangle() throws ParseException {
        String rectStr = "POLYGON ((10 20, 10 10, 30 10, 30 20, 10 20))";
        String expected = "GEOMETRYCOLLECTION (POLYGON ((10 10, 30 20, 30 10, 10 10)), "
                + "POLYGON ((10 10, 30 20, 10 20, 10 10)))";
        runCompare(rectStr, expected);
    }

    public void testRandomConvex() throws ParseException {
        String poly = "POLYGON ((150 330, 250 370, 324 366, 380 340, 430 150, 300 110, 130 220, 150 330))";
        unionEqualsOrigin(poly);
    }

    public void testRandomConcave() throws ParseException {
        String poly = "POLYGON ((190 400, 200 150, 210 380, 320 370, 260 290, 430 250, 260 260, 475 144, 180 20, 40 190, 20 310, 140 320, 190 400))";
        unionEqualsOrigin(poly);
    }
    
    public void testCray() throws ParseException {
        String poly = "POLYGON ((100 400, 500 400, 500 100, 100 100, 100 400), (150 350, 200 350, 200 300, 150 300, 150 350), (150 250, 200 250, 200 200, 150 200, 150 250), (150 170, 200 170, 200 130, 150 130, 150 170), (225 325, 270 325, 270 290, 225 290, 225 325), (230 270, 270 270, 270 230, 230 230, 230 270), (230 200, 270 200, 270 160, 230 160, 230 200), (300 160, 340 160, 340 130, 300 130, 300 160), (300 230, 340 230, 340 180, 300 180, 300 230), (300 300, 340 300, 340 260, 300 260, 300 300), (300 370, 340 370, 340 330, 300 330, 300 370), (360 330, 410 330, 410 290, 360 290, 360 330), (360 260, 410 260, 410 220, 360 220, 360 260), (375 185, 420 185, 420 150, 375 150, 375 185), (430 300, 470 300, 470 240, 430 240, 430 300), (430 380, 469 380, 469 335, 430 335, 430 380), (440 210, 470 210, 470 170, 440 170, 440 210), (440 140, 470 140, 470 110, 440 110, 440 140), (220 380, 270 380, 270 350, 220 350, 220 380), (125 282, 211 282, 211 265, 125 265, 125 282), (225 137, 278 137, 278 115, 225 115, 225 137))";
        unionEqualsOrigin(poly);
    }

    public void testRegularPolygon() throws ParseException {
        int length = 11;
        int numOfSide = 10;
        Coordinate[] coordinates = getRegularCoordinates(length, numOfSide);
        Polygon poly = createRegularPoly(coordinates);
        GeometryCollection expected = getExpectedRegularGeo(coordinates.clone());
        runCompare(poly, expected);
    }

    public void testTriangleWithHoles() throws ParseException {
        triangleWithHolesHelper(1);
        triangleWithHolesHelper(2);
        triangleWithHolesHelper(3);
    }

    public void triangleWithHolesHelper(int numOfHoles) throws ParseException {
        unionEqualsOrigin(getHoles(numOfHoles));
    }

    protected String getHoles(int numOfHoles) throws ParseException {
        String[] holeStr = new String[4];
        holeStr[0] = "POLYGON((-2 -1, 5 0, 0 6, -2 -1), (-1 0, 0 0, -1 1, -1 0))";
        holeStr[1] = "POLYGON((-2 -1, 5 0, 0 6, -2 -1), (-1 0, 0 0, -1 1, -1 0), (1 2, 0 1, 1 1, 1 2))";
        holeStr[2] = "POLYGON((-2 -1, 5 0, 0 6, -2 -1), (-1 0, 0 0, -1 1, -1 0), (-1 2, 0 2, 0 3, -1 2))";
        holeStr[2] = "POLYGON((-2 -1, 5 0, 0 6, -2 -1), (1 2, 0 1, 1 1, 1 2), (-1 0, 0 0, -1 1, -1 0), (-1 2, 0 2, 0 3, -1 2), (4 0, 4 1, 3 0, 4 0))";
        return holeStr[numOfHoles - 1];
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
        Geometry result = runEarClip(geo);
        System.out.println(result);
        unionEqualsOrigin(result, geo.union());
    }

    protected GeometryCollection getExpectedRegularGeo(Coordinate[] coordinates) {
        int size = coordinates.length - 2 - 1;
        Polygon[] array = new Polygon[size];
        for (int i = 0; i < size; i++) {
            Coordinate[] tmp = { coordinates[0], coordinates[i + 1],
                    coordinates[i + 2], coordinates[0] };
            LinearRing ring = new GeometryFactory().createLinearRing(tmp);
            array[i] = new Polygon(ring, null, fact);
        }
        return new GeometryCollection(array, fact);
    }

    protected Polygon createRegularPoly(Coordinate[] coordinates) {
        LinearRing linear = new GeometryFactory().createLinearRing(coordinates);
        Polygon poly = new Polygon(linear, null, fact);
        poly.normalize();
        return poly;
    }

    /**
     * Create "random" regular polygon based on edge length and number of edges
     * 
     * @param length
     * @param numOfSide
     * @return
     */
    protected Coordinate[] getRegularCoordinates(int length, int numOfSide) {
        Coordinate[] ring = new Coordinate[numOfSide + 1];
        // always pass origin point for now
        int x = 0;
        int y = 0;
        // relative angle, depended on the shape
        double addAngle = Math.toRadians(180
                - ((double) (180 * (numOfSide - 2))) / numOfSide);
        ring[0] = new Coordinate(x, y);
        ring[1] = new Coordinate(x, y + length);
        ring[numOfSide] = new Coordinate(x, y);
        for (int i = 1; i < numOfSide - 1; i++) {
            double angle = getAngle(ring[i].x - ring[i - 1].x, ring[i].y
                    - ring[i - 1].y)
                    + addAngle;
            double newX = ring[i].x + length * Math.cos(angle);
            double newY = ring[i].y + length * Math.sin(angle);
            ring[i + 1] = new Coordinate(newX, newY);
        }
        return ring;
    }

    protected double getAngle(double x, double y) {
        if (x == 0 && y > 0)
            return Math.PI / 2;
        if (x == 0 && y < 0)
            return Math.PI * 3 / 2;
        double angle = Math.atan(y / x);
        if (x < 0)
            angle += Math.PI;
        return angle;
    }

    protected void runCompare(Geometry sitesGeo, Geometry expected) {
        Geometry result = runEarClip(sitesGeo);
        result.normalize();
        expected.normalize();
        assertTrue(expected.equalsExact(result, COMPARISON_TOLERANCE));
    }

    protected void runCompare(String sitesWKT, String expectedWKT)
            throws ParseException {
        Geometry sitesGeo = reader.read(sitesWKT);
        Geometry expected = reader.read(expectedWKT);
        runCompare(sitesGeo, expected);
    }

    protected Geometry runEarClip(Geometry g) {
        return runEarClip(g, false);
    }

    protected Geometry runEarClip(Geometry g, boolean improve) {
        // extract first polygon
        EarClipper clipper = new EarClipper((Polygon) g.getGeometryN(0));
        clipper.setImprove(improve);
        Geometry ears = clipper.getResult();
        return ears;
    }
}
