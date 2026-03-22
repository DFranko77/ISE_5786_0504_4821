package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;
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

        Point axisPoint = axisOrigin.add(axisDirection.scale(projection));
        return point.subtract(axisPoint).normalize();
    }
}
