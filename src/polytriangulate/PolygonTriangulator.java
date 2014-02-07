package com.vividsolutions.jts.polytriangulate;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.PolygonExtracter;

public class PolygonTriangulator {
    protected Geometry original;
    private final GeometryFactory gf;

    public PolygonTriangulator(Geometry geo) {
        original = geo;
        gf = new GeometryFactory();
    }

    /**
     * Run EarClipper algorithm and return a collection of triangles for
     * MultiPolygon, different geometries will be added to one collection
     * 
     * @param improve
     * @return
     */
    public GeometryCollection getEarClipperResult(boolean improve) {
        List inputPolyList = PolygonExtracter.getPolygons(original);
        int size = inputPolyList.size();
        EarClipper clip;
        ArrayList<Polygon> outputPolylist = new ArrayList<Polygon>();
        for (int i = 0; i < size; i++) {
            clip = new EarClipper((Polygon) inputPolyList.get(i));
            clip.setImprove(improve);
            Geometry triangles = clip.getResult();
            outputPolylist.addAll(PolygonExtracter.getPolygons(triangles));
        }
        return gf.createGeometryCollection(GeometryFactory
                .toGeometryArray(outputPolylist));
    }
}
