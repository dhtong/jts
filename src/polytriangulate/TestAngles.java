package com.vividsolutions.jts.polytriangulate;

import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class TestAngles {

  GeometryFactory geomFact = new GeometryFactory();
  
  public static void main(String[] args) {
    TestAngles test = new TestAngles();
//    test.test();
    test.testAngle();
  }

  public TestAngles() {
    super();
  }

  public void testAngle() {
    GeometricShapeFactory gsf = new GeometricShapeFactory();
    final Coordinate center = new Coordinate(1,1);
    gsf.setCentre(center);
    gsf.setSize(1);
    gsf.setNumPoints(360);
    Polygon circle = gsf.createCircle();
    Coordinate[] c = circle.getCoordinates();
    Coordinate start = circle.getExteriorRing().getStartPoint().getCoordinate();
    int i=0;
    for(Coordinate cc:c) {
      System.out.println(i);
      i++;
      System.out.println(geomFact.createLineString(new Coordinate[] { start, center, cc}));
      System.out.println("interiorAngle "+Angle.interiorAngle(start, center, cc));
      System.out.println("angleBetweenOriented "+Angle.angleBetweenOriented(start, center, cc));
      System.out.println("isAcute "+Angle.isAcute(start, center, cc)+""+Angle.isObtuse(start, center, cc));
    }
  }
}
