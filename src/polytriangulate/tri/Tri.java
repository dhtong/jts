package com.vividsolutions.jts.polytriangulate.tri;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.triangulate.quadedge.Vertex;

public class Tri
{
  private Vertex p0;
  private Vertex p1;
  private Vertex p2;
  
  /**
   * triN is the neighbour triangle across the edge pN - pNN
   * 
   * pNN is the next vertex CW from pN
   * 
   */
  private Tri tri0;
  private Tri tri1;
  private Tri tri2;
  
  public Tri(Vertex p0, Vertex p1, Vertex p2 )
  {
    this.p0 = p0;
    this.p1 = p1;
    this.p2 = p2;
  }
  
  public void setNeighbours(Tri tri0, Tri tri1, Tri tri2)
  {
    this.tri0 = tri0;
    this.tri1 = tri1;
    this.tri2 = tri2;
  }
  
  /**
   * Spits a triangle by a point located inside the triangle.
   * 
   * Returns a new triangle whose 0'th vertex is p
   * 
   * @param p the point to insert
   * @return a new triangle whose 0'th vertex is p
   */
  public Tri split(Vertex p)
  {
    Tri tt0 = new Tri(p, p0, p1);
    Tri tt1 = new Tri(p, p1, p2);
    Tri tt2 = new Tri(p, p2, p0);
    
    tt0.setNeighbours(tt2, tri0, tt1);
    tt1.setNeighbours(tt0, tri1, tt2);
    tt2.setNeighbours(tt1, tri2, tt0);
    
    return tt0;
  }
  
  public boolean isVertex(Vertex v)
  {
    if (p0.equals(v) || p1.equals(v) || p2.equals(v)) return true;
    return false;
  }
  
  public boolean isRightOf(int iedge, Vertex v)
  {
    Coordinate e0 = getCoordinate(iedge);
    Coordinate e1 = getCoordinate(next(iedge));
    return CGAlgorithms.COUNTERCLOCKWISE 
      == CGAlgorithms.orientationIndex(e0, e1, v.getCoordinate());
  }
  
  public Coordinate getCoordinate(int i)
  {
    if (i == 0) return p0.getCoordinate();
    if (i == 1) return p1.getCoordinate();
    return p2.getCoordinate();
  }
  
  public Tri neighbor(int i)
  {
    if (i == 0) return tri0;
    if (i == 1) return tri1;
    return tri2;    
  }
  
  public Tri sym(int i)
  {
    return neighbor(i);
  }
  
  public static int next(int i)
  {
    switch (i) {
    case 0: return 1;
    case 1: return 2;
    case 2: return 0;
    }
    return 0;
  }
}
