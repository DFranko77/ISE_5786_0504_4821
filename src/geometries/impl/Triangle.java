package geometries.impl;

import primitives.Point;

/**
 * Represents a triangle in 3D space.
 * A triangle is a polygon with exactly three vertices.
 */
public class Triangle extends Polygon {

    /**
     * Constructs a triangle from three points.
     *
     * @param p1 first vertex
     * @param p2 second vertex
     * @param p3 third vertex
     */
    public Triangle(Point p1, Point p2, Point p3) {
        super(p1, p2, p3);
    }
}
