package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents an infinite tube (cylinder without caps) in 3D space.
 */
public class Tube extends RadialGeometry {

    /** The axis ray of the tube. */
    protected final Ray _axis;

    /**
     * Constructs a tube from an axis ray and a radius.
     *
     * @param axis   the axis ray of the tube
     * @param radius the radius of the tube
     */
    public Tube(double radius, Ray axis) {
        super(radius);
        this._axis = axis;
    }

    /**
     * Returns the outward unit normal vector to the tube at the given surface point.
     *
     * <p>The normal is computed from the closest point on the tube axis to the
     * given surface point.</p>
     *
     * @param point a point on the tube surface
     * @return normalized vector orthogonal to the tube axis and passing through
     *         {@code point}
     */
    @Override
    public Vector getNormal(Point point) {
        Point axisOrigin = _axis.origin();
        Vector axisDirection = _axis.direction();
        double projection = axisDirection.dotProduct(point.subtract(axisOrigin));

        if (isZero(projection)) {
            return point.subtract(axisOrigin).normalize();
        }

        Point axisPoint = _axis.getPoint(projection);
        return point.subtract(axisPoint).normalize();
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        Point p0 = ray.origin();
        Vector v = ray.direction();
        Point axisOrigin = _axis.origin();
        Vector axisDirection = _axis.direction();

        double vAxis = v.dotProduct(axisDirection);
        double a = alignZero(v.lengthSquared() - vAxis * vAxis);

        if (isZero(a)) {
            return null;
        }

        double b;
        double c;

        if (axisOrigin.equals(p0)) {
            b = 0;
            c = -_radiusSquared;
        } else {
            Vector deltaP = p0.subtract(axisOrigin);
            double deltaPAxis = deltaP.dotProduct(axisDirection);

            b = alignZero(2 * (v.dotProduct(deltaP) - vAxis * deltaPAxis));
            c = alignZero(deltaP.lengthSquared() - deltaPAxis * deltaPAxis - _radiusSquared);
        }

        double discriminant = alignZero(b * b - 4 * a * c);
        if (discriminant <= 0) {
            return null;
        }

        double sqrtDiscriminant = Math.sqrt(discriminant);
        double denominator = 2 * a;
        double t1 = alignZero((-b - sqrtDiscriminant) / denominator);
        double t2 = alignZero((-b + sqrtDiscriminant) / denominator);

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
