package com.vividsolutions.jts.polytriangulate.tri;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.triangulate.quadedge.LocateFailureException;
import com.vividsolutions.jts.triangulate.quadedge.Vertex;

public class TriSubdivision
{
  private Vertex[] frameVertex = new Vertex[3];
  private Envelope frameEnv;
  private Tri someTri;
  private LastFoundTriLocator locator = null;

  public TriSubdivision(Envelope env, double tolerance) {
    // currentSubdiv = this;

    createFrame(env);
    
    someTri = initSubdiv();
    locator = new LastFoundTriLocator(this);
  }

  private void createFrame(Envelope env)
  {
    double deltaX = env.getWidth();
    double deltaY = env.getHeight();
    double offset = 0.0;
    if (deltaX > deltaY) {
      offset = deltaX * 10.0;
    } else {
      offset = deltaY * 10.0;
    }

    frameVertex[0] = new Vertex((env.getMaxX() + env.getMinX()) / 2.0, env
        .getMaxY()
        + offset);
    frameVertex[1] = new Vertex(env.getMinX() - offset, env.getMinY() - offset);
    frameVertex[2] = new Vertex(env.getMaxX() + offset, env.getMinY() - offset);

    frameEnv = new Envelope(frameVertex[0].getCoordinate(), frameVertex[1]
        .getCoordinate());
    frameEnv.expandToInclude(frameVertex[2].getCoordinate());
  }
  
  private Tri initSubdiv()
  {
    // build initial subdivision from frame
    return new Tri(frameVertex[0], frameVertex[1], frameVertex[2]);
  }

  public Tri findTri()
  {
    return someTri;
  }
  
  public Tri locateFrom(Vertex v, Tri startTri) {
    int iter = 0;
    int maxIter = 10000;

    Tri tri = startTri;

    while (true) {
      iter++;

      /**
       * So far it has always been the case that failure to locate indicates an
       * invalid subdivision. So just fail completely. (An alternative would be
       * to perform an exhaustive search for the containing triangle, but this
       * would mask errors in the subdivision topology)
       * 
       * This can also happen if two vertices are located very close together,
       * since the orientation predicates may experience precision failures.
       */
      if (iter > maxIter) {
        throw new LocateFailureException("at tri....");
        // String msg = "Locate failed to converge (at edge: " + e + ").
        // Possible causes include invalid Subdivision topology or very close
        // sites";
        // System.err.println(msg);
        // dumpTriangles();
      }

      if (tri.isVertex(v)) {
        break;
      } else if (tri.isRightOf(0, v)) {
        tri = tri.neighbor(0);
      } else if (tri.isRightOf(1, v)) {
        tri = tri.neighbor(1);
      } else if (tri.isRightOf(2, v)) {
        tri = tri.neighbor(2);
      } else {
        // inside triangle or on edge
        break;
      }
    }
    // System.out.println("Locate count: " + iter);
    return tri;
  }

  public Tri locate(Vertex v) {
    return locator.locate(v);
  }
}
