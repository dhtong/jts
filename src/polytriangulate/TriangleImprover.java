package com.vividsolutions.jts.polytriangulate;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.triangulate.quadedge.TrianglePredicate;

import java.util.Collections;
import java.util.List;

/**
 * @author Michael Bedward
 * @author Martin Davis
 */
class TriangleImprover 
{
  private static int MAX_IMPROVE_SCAN = 20;

  private final List<Coordinate> shellCoords;

  TriangleImprover(List<Coordinate> shellCoords) {
    this.shellCoords = Collections.unmodifiableList(shellCoords);
  }

  public void improve(List<PolygonTriangle> triList)
  {
    // System.out.println("Refining...");
    for (int i = 0; i < MAX_IMPROVE_SCAN; i++) {
      int improveCount = doImprovementScan(triList);
      System.out.println("improve #" + i + " - count = " + improveCount);
      if (improveCount == 0)
        return;
    }
  }
  
  /**
   * Attempts to improve the triangulation by examining pairs of triangles with a
   * common edge, forming a quadrilateral, and testing if swapping the diagonal
   * of this quadrilateral would produce two new triangles with larger minimum
   * interior angles.
   * 
   * @return the number of improvement flips that were made
   */
  private int doImprovementScan(List<PolygonTriangle> triList)
  {
    int improveCount = 0;
    
    //TODO: improve this O(n^2) algorithm by using a suitable triangle subdivision structure
    for (int i = 0; i < triList.size() - 1; i++) {
      PolygonTriangle ear0 = triList.get(i);
      for (int j = i + 1; j < triList.size(); j++) {
        PolygonTriangle ear1 = triList.get(j);
        if (improve(ear0, ear1)) {
          improveCount++;
        }
      }
    }
    return improveCount;
  }

  public boolean improve(PolygonTriangle t0, PolygonTriangle t1) {
    return flip(t0, t1, t0.getSharedVertices(t1));
  }

  public boolean flip(PolygonTriangle tri0, 
      PolygonTriangle tri1, 
      int[] adjacentVertices) {
    
    /**
     * Ensure that triangles are adjacent.
     */
    if (adjacentVertices == null || adjacentVertices.length != 2) {
      return false;
    }

    Coordinate adj0 = shellCoords.get(adjacentVertices[0]);
    Coordinate adj1 = shellCoords.get(adjacentVertices[1]);
    
    int iOpp0 = oppositeIndex(tri0, adjacentVertices);
    Coordinate opp0 = shellCoords.get(iOpp0);
    int iOpp1 = oppositeIndex(tri1, adjacentVertices);
    Coordinate opp1 = shellCoords.get(iOpp1);

    /*
     * The candidate new edge is from opp0 to opp1. 
     * First check if this is inside
     * the quadrilateral, which is the case
     * iff the quadrilateral is convex
     */
    if (! isQuadConvex(opp0, adj0, adj1, opp1))
      return false;

    /**
     * The candidate edge is inside the quadrilateral.  
     * Check to see if the flipping criteria is met.
     * The flipping criteria is to flip iff the two
     * triangles are not Delaunay
     * (i.e. one of the opposite vertices is in 
     * the circumcircle of the other triangle).
     */
    boolean doFlip = false;

    if (! isDelaunay(opp0, adj0, adj1, opp1)) {
      doFlip = true;
    }

    if (doFlip) {
      tri0.setVertices(adjacentVertices[0], iOpp0, iOpp1);
      tri1.setVertices(iOpp1, iOpp0, adjacentVertices[1]);
      return true;
    }
    return false;
  }

  /**
   * Checks if the quadrilateral formed by the 
   * two triangles is convex.
   * 
   * @param opp0
   * @param adj0
   * @param adj1
   * @param opp1
   * @return
   */
  private boolean isQuadConvex(Coordinate opp0,
      Coordinate adj0, 
      Coordinate adj1,  
      Coordinate opp1)
  {
    int dir0 = CGAlgorithms.orientationIndex(opp0, opp1, adj0);
    int dir1 = CGAlgorithms.orientationIndex(opp0, opp1, adj1);
    boolean isQuadConvex = dir0 == -dir1;
    if (!isQuadConvex)
      return false;
    return true;
  }
  
  private int oppositeIndex(PolygonTriangle polyTri, int[] sharedVertices)
  {
    int[] vertices = polyTri.getVertices();
    int i = 0;
    while (vertices[i] == sharedVertices[0] || vertices[i] == sharedVertices[1]) {
      i++;
    }
    return vertices[i];

  }
  private boolean isDelaunay(Coordinate c0, Coordinate adj0, 
      Coordinate adj1, Coordinate c1)
  {
    return ! (isInCircle(c0, adj0, adj1, c1) 
        || isInCircle(c1, adj1, adj0, c0));
  }
  
  private boolean isInCircle(Coordinate a, Coordinate b, Coordinate c,
      Coordinate p) {
    if (isCCW(a, b, c)) {
      return TrianglePredicate.isInCircleRobust(a, b, c, p);
    }
    return TrianglePredicate.isInCircleRobust(a, c, b, p);
  }

  public final boolean isCCW(Coordinate a, Coordinate b, Coordinate c) {
    return CGAlgorithms.computeOrientation(a, b, c) == CGAlgorithms.COUNTERCLOCKWISE;
  }

}
