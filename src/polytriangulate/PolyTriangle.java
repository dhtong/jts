package com.vividsolutions.jts.polytriangulate;

import com.vividsolutions.jts.geom.Coordinate;

public class PolyTriangle {
    private final Coordinate[] vertices;

    /**
     * Constructor. No checking is done on the values supplied.
     * 
     * @param v0 first vertex
     * @param v1 second vertex
     * @param v2 third vertex
     */
    public PolyTriangle(Coordinate v0, Coordinate v1, Coordinate v2) {
        vertices = new Coordinate[3];
        setVertices(v0, v1, v2);
    }

    /**
     * Set the vertex indices for this Triangle. No checking is done on the
     * values supplied.
     * 
     * @param v0 first vertex
     * @param v1 second vertex
     * @param v2 third vertex
     */
    public void setVertices(Coordinate v0, Coordinate v1, Coordinate v2) {
        vertices[0] = v0;
        vertices[1] = v1;
        vertices[2] = v2;
    }

    /**
     * Get this Triangle's vertex indices
     * 
     * @return a new array with the vertex indices
     */
    public Coordinate[] getVertices() {
        Coordinate[] copy = new Coordinate[3];
        for (int i = 0; i < 3; i++) {
            copy[i] = vertices[i];
        }
        return copy;
    }

    /**
     * TODO: If necessary, implement this later
     * @param other
     * @return
     */
    public Coordinate[] getSharedVertices(PolygonTriangle other) {
        return null;
    }

    /**
     * Return a string representation of this Triangle
     * 
     * @return string of the form "Triangle(%d %d %d)"
     */
    @Override
    public String toString() {
        return String.format("Triangle(%d %d %d)", vertices[0], vertices[1],
                vertices[2]);
    }
}
