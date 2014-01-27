package com.vividsolutions.jts.polytriangulate;

import com.vividsolutions.jts.geom.*;

public class PolygonTriangulationFunctions 
{

  public static Geometry earClip(Geometry g)    
  {   
    // extract first polygon
    Polygon poly = (Polygon) g.getGeometryN(0);
    EarClipper clipper = new EarClipper(poly);
    Geometry ears = clipper.getResult();
    return ears;
  }
  
  public static Geometry earClipNoImprove(Geometry g)    
  { 
    // extract first polygon
    Polygon poly = (Polygon) g.getGeometryN(0);
    EarClipper clipper = new EarClipper(poly);
    clipper.setImprove(false);
    Geometry ears = clipper.getResult();
    return ears;
  }


}
