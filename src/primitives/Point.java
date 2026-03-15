package primitives;

import java.util.Objects;

/**
 * Represents an immutable point in a 3D Cartesian coordinate system.
 */
public class Point {

    /** The origin point (0,0,0). */
    public static final Point ZERO = new Point(Double3.ZERO);
    /** Cartesian coordinates of the point. */
    protected final Double3 _xyz;

    /**
     * Constructs a point from numeric coordinates.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    public Point(double x, double y, double z) {
        this(new Double3(x, y, z));
    }

    /**
     * Constructs a point from a coordinate triad.
     *
     * @param xyz coordinates container
     */
    public Point(Double3 xyz) {
        this._xyz = xyz;
    }

    /**
     * Moves this point by the given vector.
     *
     * @param v the vector to add
     * @return a new point displaced by {@code v}
     */
    public Point add(Vector v) {
        return new Point(_xyz.add(v._xyz));
    }

    /**
     * subtracts
     * @param p point to subtract from
     * @return vector
     */
    public Vector subtract(Point p) {
        return new Vector(_xyz.subtract(p._xyz));
    }

    /**
     * Returns the squared distance between this point and the given point.
     *
     * @param other the other point
     * @return squared distance
     */
    public double distanceSquared(Point other) {
        Double3 d = _xyz.subtract(other._xyz);
        return d._d1() * d._d1() + d._d2() * d._d2() + d._d3() * d._d3();
    }

    /**
     * Returns the distance between this point and the given point.
     *
     * @param other the other point
     * @return distance
     */
    public double distance(Point other) {
        return Math.sqrt(distanceSquared(other));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point point)) return false;
        return Objects.equals(_xyz, point._xyz);
    }

    @Override
    public int hashCode() {
        return _xyz.hashCode();
    }

    @Override
    public String toString() {
        return "(" + _xyz + ")";
    }



}
