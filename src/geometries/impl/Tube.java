package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

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

    @Override
    public Vector getNormal(Point point) {
        return null;
    }
}
