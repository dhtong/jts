package com.vividsolutions.jts.polytriangulate.tri;

import com.vividsolutions.jts.geom.Coordinate;

public class TriEdge {
    public Coordinate one;
    public Coordinate two;
    
    

    public TriEdge(Coordinate a, Coordinate b) {
        one = a;
        two = b;
        normalize();
    }

    public void normalize() {
        if (one.compareTo(two) < 0) {
            reverse();
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + Coordinate.hashCode(one.x);
        result = 37 * result + Coordinate.hashCode(two.x);
        result = 37 * result + Coordinate.hashCode(one.y);
        result = 37 * result + Coordinate.hashCode(two.y);
        return result;
    }
    
    @Override
    public boolean equals(Object arg){
        if(!(arg instanceof TriEdge))
            return false;
        TriEdge other = (TriEdge)arg;
        if(one.equals(other.one) && two.equals(other.two))
            return true;
        return false;
    }

    private void reverse() {
        Coordinate tmp = one;
        one = two;
        two = tmp;
    }
}
