package geometries.impl;

import primitives.Point;
import primitives.Vector;

/**
 * Represents a sphere in 3D space.
 */
public class Sphere extends RadialGeometry {

    /** The center point of the sphere. */
    private final Point _center;

    /**
     * Constructs a sphere from a center point and a radius.
     *
     * @param center the center of the sphere
     * @param radius the radius of the sphere
     */
    public Sphere(Point center, double radius) {
        super(radius);
        this._center = center;
    }

    /**
     * Returns the outward unit normal vector to the sphere at the given surface point.
     *
     * @param point a point on the sphere surface
     * @return normalized vector from the sphere center to {@code point}
     */
    @Override
    public Vector getNormal(Point point) {
        return point.subtract(_center).normalize();
    }
}
