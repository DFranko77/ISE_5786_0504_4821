package geometries.impl;

import geometries.api.Geometry;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a plane in 3D space.
 */
public class Plane extends Geometry {

    /** A reference point on the plane. */
    private final Point _point;

    /** The normal vector to the plane. */
    private final Vector _normal;

    /**
     * Constructs a plane from three points.
     * Saves the first point as reference; normal is set to null for now.
     *
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     */
    public Plane(Point p1, Point p2, Point p3) {
        _point = p1;
        Vector v1 = p2.subtract(p1);
        Vector v2 = p3.subtract(p1);
        _normal = v1.crossProduct(v2).normalize();
    }

    /**
     * Constructs a plane from a point and a normal vector.
     *
     * @param point  a point on the plane
     * @param normal the normal vector to the plane
     */
    public Plane(Point point, Vector normal) {
        _point = point;
        _normal = normal.normalize();
    }

    @Override
    public Vector getNormal(Point point) {
        return _normal;
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        Point p0 = ray.origin();
        if (_point.equals(p0)) {
            return null;
        }

        Vector v = ray.direction();
        double nv = alignZero(_normal.dotProduct(v));
        if (isZero(nv)) {
            return null;
        }

        double t = alignZero(_normal.dotProduct(_point.subtract(p0)) / nv);
        if (t <= 0) {
            return null;
        }

        return List.of(ray.getPoint(t));
    }
}
