package geometries.impl;

import primitives.Ray;

/**
 * Represents a finite cylinder (tube with two caps) in 3D space.
 */
public class Cylinder extends Tube {

    /** The height of the cylinder. */
    private final double _height;

    /**
     * Constructs a cylinder from an axis ray, a radius, and a height.
     *
     * @param axis   the axis ray of the cylinder
     * @param radius the radius of the cylinder
     * @param height the height of the cylinder
     */
    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        this._height = height;
    }
}
