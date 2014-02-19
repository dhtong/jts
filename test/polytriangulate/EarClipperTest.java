package com.vividsolutions.jts.polytriangulate;

import java.util.ArrayList;
import java.util.HashMap;

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

    /*
     * Martin's square cases
     */
    public void testCraySquare() throws ParseException {
        String poly = "POLYGON ((100 400, 500 400, 500 100, 100 100, 100 400), (150 350, 200 350, 200 300, 150 300, 150 350), (150 250, 200 250, 200 200, 150 200, 150 250), (150 170, 200 170, 200 130, 150 130, 150 170), (225 325, 270 325, 270 290, 225 290, 225 325), (230 270, 270 270, 270 230, 230 230, 230 270), (230 200, 270 200, 270 160, 230 160, 230 200), (300 160, 340 160, 340 130, 300 130, 300 160), (300 230, 340 230, 340 180, 300 180, 300 230), (300 300, 340 300, 340 260, 300 260, 300 300), (300 370, 340 370, 340 330, 300 330, 300 370), (360 330, 410 330, 410 290, 360 290, 360 330), (360 260, 410 260, 410 220, 360 220, 360 260), (375 185, 420 185, 420 150, 375 150, 375 185), (430 300, 470 300, 470 240, 430 240, 430 300), (430 380, 469 380, 469 335, 430 335, 430 380), (440 210, 470 210, 470 170, 440 170, 440 210), (440 140, 470 140, 470 110, 440 110, 440 140), (220 380, 270 380, 270 350, 220 350, 220 380), (125 282, 211 282, 211 265, 125 265, 125 282), (225 137, 278 137, 278 115, 225 115, 225 137))";
        unionEqualsOrigin(poly);
    }

    public void testCrayCircle() throws ParseException {
        String poly = "POLYGON ((20 0, 20 420, 470 420, 470 0, 20 0), (60 250, 80 240, 95 249, 100 270, 80 280, 60 270, 60 250), (64 175, 75 163, 90 160, 100 170, 100 190, 80 200, 70 190, 64 175), (90 110, 100 90, 120 90, 130 100, 130 120, 120 130, 100 130, 90 110), (106 324, 115 312, 130 310, 140 320, 140 340, 127 345, 110 340, 106 324), (140 210, 149 205, 160 210, 160 220, 160 230, 150 230, 140 220, 140 210), (145 364, 155 352, 170 350, 185 363, 180 380, 164 385, 150 380, 145 364), (150 240, 162 235.5, 170 240, 170 250, 158 257.5, 153 253, 150 240), (155 57, 170 40, 190 40, 194 54, 190 70, 180 80, 170 80, 160 70, 155 57), (160 270, 170 260, 180 260, 190 270, 190 280, 180 290, 170 290, 163 285, 160 280, 160 270), (170 170, 177 161.5, 184 159, 189.5 162.5, 193.5 170.5, 190 180, 181 184.5, 174.5 179.5, 170 170), (190 320, 200 310, 204 315, 208 325, 200 330, 191 325, 190 320), (199 215, 200 210, 206 205.5, 210 210, 213 215.5, 210 220, 203.5 225, 200 220, 199 215), (200 140, 205 131.5, 210 130, 216.5 134, 220 140, 215 150, 206.5 153, 200.5 146.5, 200 140), (200 370, 210 360, 230 360, 240 380, 235 395, 220 400, 210 390, 200 370), (206.5 250.5, 207 245.5, 213 241, 217 243, 219.5 247, 217.5 253.5, 212 257, 206.5 250.5), (221 194, 226.5 189.5, 233.5 193, 234 201, 227.5 205.5, 223.5 202, 221 194), (221 263, 223 257.5, 228 257.5, 230 260, 230 265.5, 225 269, 221 267, 221 263), (230 230, 232.5 226, 235 229.5, 232.5 234.5, 230 230), (230 310, 240 310, 246.66666666666666 315.55555555555554, 245.55555555555554 324.44444444444446, 240 330, 230 320, 230 310), (232 45, 240 30, 258 25, 271 35, 272 55, 255 64, 240 60, 232 45), (236.5 272, 241.5 266, 248 266, 250 270, 252 276.5, 245.5 280, 240 280, 236.5 272), (238.5 124, 244 117, 252.5 115, 260 120, 260 130, 255.5 135.5, 247.5 135.5, 243.5 132.5, 238.5 124), (240 210, 243 209.5, 245 213.5, 242 217, 240 215, 240 210), (243 227, 247 219.5, 251.5 217, 257 221.5, 257 228, 249.5 233.5, 243 227), (246.5 180.5, 253 177, 260 180, 259 186.5, 257 190, 250 190, 246.5 186, 246.5 180.5), (247.5 245.5, 250 240, 252 243, 250 247, 247.5 245.5), (254.5 208, 255 204, 258 204, 259 207, 257 211, 254.5 208), (255 376, 265 365, 280 360, 290 370, 290 390, 280 400, 270 400, 260 390, 255 376), (260 310, 270 300, 280 300, 285.55555555555554 305.55555555555554, 290 320, 275.55555555555554 324.44444444444446, 270 320, 260 310), (261.5 237, 265 232.5, 267.5 236, 265 240, 261.5 237), (264 263, 266.5 258.5, 270 260, 275 266.5, 270 270, 265 267, 264 263), (264.5 222.5, 266.5 217.5, 269 217.5, 270 220, 267.5 225, 264.5 222.5), (275.5 186.5, 280 180, 286.5 181, 288 186, 287.5 192, 281.5 194.5, 277.5 192, 275.5 186.5), (280 215.5, 283.5 212.5, 290 210, 293.5 214.5, 293.5 220.5, 286 222.5, 280 220, 280 215.5), (280 240, 283 236.5, 289.5 236, 290 240, 290 246.5, 284 249, 280 245.5, 280 240), (287 121, 295 110, 305 112, 308.5 122.5, 305.5 133.5, 297 134.5, 290 130, 287 121), (290 290, 300 280, 310 280, 320 290, 310 300, 300 300, 290 290), (310 40, 330 20, 350 30, 350 50, 340 60, 320 60, 310 40), (310 360, 320 350, 337 345, 352 355, 350 370, 340 380, 328 385, 315 378, 310 360), (320 150, 325.5 140, 330 140, 338 146, 338 157.5, 330 160, 324 158, 320 150), (320 243.5, 328 238, 340 240, 340 250, 337 259, 327.5 259.5, 320 254.5, 320 243.5), (328 183, 333 176, 341 175, 346.5 180.5, 346 189.5, 338 194.5, 332 192, 328 183), (330 210, 340 210, 345.5 216, 343.5 224, 337 226, 330 220, 330 210), (346 296, 350 280, 365 273, 380 270, 390 280, 400 290, 385 307, 368 315, 355 309, 346 296), (360 70, 380 60, 400 60, 410 80, 400 100, 380 100, 360 90, 360 70), (380 150, 390 130, 410 130, 420 150, 415 164, 400 170, 386 164, 380 150), (390 220, 400 200, 414 204, 430 220, 430 240, 410 250, 390 240, 390 220))";
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
        triangleWithHolesHelper(4);
    }

    public void triangleWithHolesHelper(int numOfHoles) throws ParseException {
        unionEqualsOrigin(getHoles(numOfHoles));
    }

    protected String getHoles(int numOfHoles) throws ParseException {
        String[] holeStr = new String[4];
        holeStr[0] = "POLYGON((-2 -1, 5 0, 0 6, -2 -1), (-1 0, 0 0, -1 1, -1 0))";
        // Two holes whose left most x on the same vertical line
        holeStr[1] = "POLYGON ((16 102, 346 409, 488 69, 359 24, 16 102), (250 250, 304 304, 326 254, 250 250), (250 150, 347 194, 380 144, 250 150))";
        holeStr[2] = "POLYGON((-2 -1, 5 0, 0 6, -2 -1), (-1 0, 0 0, -1 1, -1 0), (1 1, 0 2, 0 1, 1 1))";
        holeStr[3] = "POLYGON((-2 -1, 5 0, 0 6, -2 -1), (1 2, 0 1, 1 1, 1 2), (-1 0, 0 0, -1 1, -1 0), (-1 2, 0 2, 0 3, -1 2), (4 0, 4 1, 3 0, 4 0))";
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
