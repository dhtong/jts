package com.vividsolutions.jts.polytriangulate;

import java.util.ArrayList;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Triangle;
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
    
    public void testRegularPolygon() throws ParseException{
        int length = 11;
        int numOfSide = 10;
        Coordinate[] coordinates = getRegularCoordinates(length, numOfSide);
        Polygon poly = createRegularPoly(coordinates);
        GeometryCollection expected = getExpectedRegularGeo(coordinates);
        runCompare(poly, expected);
    }
    
    public GeometryCollection getExpectedRegularGeo(Coordinate[] coordinates){
        int size = coordinates.length - 2 - 1;
        Polygon[] array = new Polygon[size];
        for(int i = 0; i < size; i++){
            Coordinate[] tmp = {coordinates[0], coordinates[i+1], coordinates[i+2], coordinates[0]};
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
     * @param length
     * @param numOfSide
     * @return
     */
    protected Coordinate[] getRegularCoordinates(int length, int numOfSide) {
        Coordinate[] ring = new Coordinate[numOfSide + 1];
        int x = (int)Math.random()*10;
        int y = (int)Math.random()*10;
        //relative angle, depended on the shape
        double addAngle = Math.toRadians(180 - ((double)(180 * (numOfSide - 2))) / numOfSide);      
        ring[0] = new Coordinate(x, y);
        ring[1] = new Coordinate(x, y + length);
        ring[numOfSide] = new Coordinate(x, y);
        for(int i = 1; i < numOfSide - 1; i++) {
            double angle = getAngle(ring[i].x - ring[i - 1].x, ring[i].y - ring[i - 1].y) 
                    + addAngle;
            double newX = ring[i].x + length * Math.cos(angle);
            double newY = ring[i].y + length * Math.sin(angle);
            ring[i+1] = new Coordinate(newX, newY);
        }
        return ring;
    }
    
    protected double getAngle(double x, double y) {
        if(x == 0 && y > 0)
            return Math.PI / 2;
        if(x == 0 && y < 0)
            return Math.PI * 3 / 2;
        double angle = Math.atan(y / x);
        if(x < 0)
            angle += Math.PI;            
        return angle;
    }

    protected void runCompare(Geometry sitesGeo, Geometry expected) {
        Geometry result = runEarClip(sitesGeo);
        System.out.println(result);
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
        Polygon poly = (Polygon) g.getGeometryN(0);
        EarClipper clipper = new EarClipper(poly);
        clipper.setImprove(improve);
        Geometry ears = clipper.getResult();
        return ears;
    }
}
