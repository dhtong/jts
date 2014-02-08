package com.vividsolutions.jts.polytriangulate;

import java.util.ArrayList;
import java.util.Arrays;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import junit.framework.TestCase;

public class HoleJoinerTest extends TestCase {
    protected WKTReader reader = new WKTReader();
    protected PreparedGeometry inputPrepGeom;
    GeometryFactory gf = new GeometryFactory();

    public HoleJoinerTest(String name) {
        super(name);
    }

    public void testJoinHoles() throws ParseException {
        String polyStr = "POLYGON((-2 -1, 5 0, 0 6, -2 -1), (-1 0, 0 0, -1 1, -1 0), (1 2, 0 1, 1 1, 1 2), (0 3, -0.5 2, 0.6 2, 0 3))";
        String polyStr2 = "POLYGON ((-0.9 6.7, -2.7 -0.6, 5 0.3, 4.1 5.8, -0.9 6.7), (-0.2 5.6, -0.6 5.8, -1.5 0.3, -1.1 0.2, 0.5 1.3, 0.4 3.3, 2.1 5.2, 3.2 3.8, 2 2.8, 1.7 4.1, 1.2 3.7, 2.1 1.2, 3.6 3, 3 5.5, 1.3 5.2, 0.4 4.3, -1 1.1, -0.2 5.6), (0.5 6, 0 5.4, -0.4 3.1, 0.5 5.3, 0.5 6), (2.1 4.8, 1.9 4, 2.4 3.3, 2.3 4.6, 2.1 4.8), (1 3.3, 0.8 0.4, 1.3 0.5, 1 3.3))";
        String polyStr3 = "POLYGON ((3 5.4, 0 6, -3.2 2.3, -0.1 -0.7, 5.9 4.4, 2.2 3.8, 3 5.4), (0 5, 0 1, 1 3, 0 5))";
        String polyCray = "POLYGON ((100 400, 500 400, 500 100, 100 100, 100 400), (150 350, 200 350, 200 300, 150 300, 150 350), (150 250, 200 250, 200 200, 150 200, 150 250), (150 170, 200 170, 200 130, 150 130, 150 170), (225 325, 270 325, 270 290, 225 290, 225 325), (230 270, 270 270, 270 230, 230 230, 230 270), (230 200, 270 200, 270 160, 230 160, 230 200), (300 160, 340 160, 340 130, 300 130, 300 160), (300 230, 340 230, 340 180, 300 180, 300 230), (300 300, 340 300, 340 260, 300 260, 300 300), (300 370, 340 370, 340 330, 300 330, 300 370), (360 330, 410 330, 410 290, 360 290, 360 330), (360 260, 410 260, 410 220, 360 220, 360 260), (375 185, 420 185, 420 150, 375 150, 375 185), (430 300, 470 300, 470 240, 430 240, 430 300), (430 380, 469 380, 469 335, 430 335, 430 380), (440 210, 470 210, 470 170, 440 170, 440 210), (440 140, 470 140, 470 110, 440 110, 440 140), (220 380, 270 380, 270 350, 220 350, 220 380), (125 282, 211 282, 211 265, 125 265, 125 282), (225 137, 278 137, 278 115, 225 115, 225 137))";
        showWKTPoly(polyStr);
        showWKTPoly(polyStr2);
        showWKTPoly(polyStr3);
        showWKTPoly(polyCray);
    }

    public void showWKTPoly(String polyStr) throws ParseException {
        Polygon poly = (Polygon) reader.read(polyStr);
        poly.normalize();
        PreparedGeometryFactory pgFact = new PreparedGeometryFactory();
        inputPrepGeom = pgFact.create(poly);
        Coordinate[] coords = poly.getExteriorRing().getCoordinates();
        ArrayList<Coordinate> shellCoords = new ArrayList<Coordinate>();
        shellCoords.addAll(Arrays.asList(coords));
        HoleJoinerLM holeJoiner = new HoleJoinerLM(inputPrepGeom);
        holeJoiner.joinHoles(shellCoords);
        Coordinate[] coorArray = shellCoords.toArray(new Coordinate[0]);
        Polygon tmp = gf.createPolygon(gf.createLinearRing(coorArray));
        System.out.println(tmp);
    }
}
