package geometries.impl;

import geometries.api.Geometry;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a triangle in 3D space.
 * A triangle is a polygon with exactly three vertices.
 */
public class Triangle extends Polygon {

    /** Vector from p1 to p2 (v1) */
    private final Vector _edge1;
    /** Vector from p1 to p3 (v2) */
    private final Vector _edge2;

    /**
     * Constructs a triangle from three points.
     *
     * @param p1 first vertex
     * @param p2 second vertex
     * @param p3 third vertex
     */
    public Triangle(Point p1, Point p2, Point p3) {
        super(p1, p2, p3);
        _edge1 = p2.subtract(p1);
        _edge2 = p3.subtract(p1);
    }

    /**
     * Finds intersections between a ray and the triangle.
     * <p>
     * This implementation uses the Möller–Trumbore intersection algorithm, which computes
     * the intersection point using barycentric coordinates without needing to pre-calculate
     * the plane equation.
     * </p>
     * <p>
     * The algorithm solves the equation: P = (1 - u - v)A + uB + vC
     * where A, B, C are the triangle vertices and u, v are the barycentric coordinates.
     * An intersection occurs if u &ge; 0, v &ge; 0 and u + v &le; 1.
     * </p>
     *
     * @param ray the ray to intersect with the triangle
     * @return a list containing the intersection point if one exists, null otherwise
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        Point p0 = ray.origin();
        Vector v = ray.direction();

        Point p1 = _vertices.get(0);
        // p2 and p3 are implicitly used via _edge1 and _edge2

        // Möller–Trumbore intersection algorithm
        // This algorithm solves for u, v, t in the equation:
        // O + tD = (1 - u - v)V0 + uV1 + vV2
        // where O is ray origin, D is ray direction, and V0, V1, V2 are triangle vertices.

        Vector pvec = v.crossProduct(_edge2);
        double det = _edge1.dotProduct(pvec);

        // if determinant is near zero, ray is parallel to the triangle plane
        if (isZero(det)) return null;

        double invDet = 1 / det;
        Vector tvec;
        try {
            tvec = p0.subtract(p1); // Vector from V0 to Ray Origin
        } catch (IllegalArgumentException e) {
            // Ray origin is exactly at p1, so tvec is zero vector (illegal)
            return null;
        }

        // Calculate u parameter and test bounds
        // u corresponds to the weight of vertex V1 (p2)
        // condition: 0 < u < 1
        double u = alignZero(tvec.dotProduct(pvec) * invDet);
        if (u <= 0 || u >= 1) return null;

        Vector qvec = tvec.crossProduct(_edge1);
        
        // Calculate v parameter and test bounds
        // v corresponds to the weight of vertex V2 (p3)
        // condition: v > 0 and u + v < 1 (point is inside the triangle)
        double vParam = alignZero(v.dotProduct(qvec) * invDet);
        if (vParam <= 0 || alignZero(u + vParam - 1) >= 0) return null;

        // Calculate t - distance from ray origin to intersection point
        double t = alignZero(_edge2.dotProduct(qvec) * invDet);

        if (t <= 0 || alignZero(t - maxDistance) > 0) return null;

        return List.of(new Intersection(this, ray.getPoint(t)));
    }
}
