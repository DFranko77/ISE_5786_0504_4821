package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static primitives.Util.alignZero;

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

    /**
     * Finds the forward intersection points between this sphere and the given ray.
     * The ray origin itself and tangent contacts are not considered intersections.
     *
     * @param ray the ray to intersect with the sphere
     * @return one or two intersection points ordered by distance from the ray origin,
     *         or {@code null} when there are no forward intersections
     */
    @Override
    public List<Point> findIntersections(Ray ray) {
        Point p0 = ray.origin();
        Vector v = ray.direction();

        if (_center.equals(p0)) {
            return List.of(ray.getPoint(_radius));
        }

        Vector u = _center.subtract(p0);
        double tm = alignZero(v.dotProduct(u));
        double dSquared = alignZero(u.lengthSquared() - tm * tm);
        double thSquared = alignZero(_radiusSquared - dSquared);

        if (thSquared <= 0) {
            return null;
        }

        double th = Math.sqrt(thSquared);
        double t1 = alignZero(tm - th);
        double t2 = alignZero(tm + th);

        if (t1 > 0 && t2 > 0) {
            Point p1 = ray.getPoint(t1);
            Point p2 = ray.getPoint(t2);
            return t1 < t2 ? List.of(p1, p2) : List.of(p2, p1);
        }

        return t1 > 0
                ? List.of(ray.getPoint(t1))
                : t2 > 0
                ? List.of(ray.getPoint(t2))
                : null;
    }
}
