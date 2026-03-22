package primitives;

/**
 * Represents a non-zero vector in a 3D Cartesian coordinate system.
 *
 * <p>The vector shares the same coordinate representation as {@link Point}.</p>
 */
public class Vector extends Point {

    /** Unit vector along the X axis. */
    public static final Vector AXIS_X = new Vector(1, 0, 0);
    /** Unit vector along the Y axis. */
    public static final Vector AXIS_Y = new Vector(0, 1, 0);
    /** Unit vector along the Z axis. */
    public static final Vector AXIS_Z = new Vector(0, 0, 1);

    /**
     * Constructs a vector from a coordinate triad.
     *
     * @param xyz vector coordinates
     */
    public Vector(Double3 xyz) {
        super(xyz);
        if (xyz.equals(Double3.ZERO))
            throw new IllegalArgumentException("Vector cannot be zero");
    }

    /**
     * Constructs a vector from numeric coordinates.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    public Vector(double x, double y, double z) {
        super(x, y, z);
        if (_xyz.equals(Double3.ZERO))
            throw new IllegalArgumentException("Vector cannot be zero");
    }

    /** Returns a new vector that is the sum of this vector and {@code other}. */
    public Vector add(Vector other) {
        return new Vector(_xyz.add(other._xyz));
    }

    /** Returns a new vector scaled by the given scalar. */
    public Vector scale(double scalar) {
        return new Vector(_xyz.scale(scalar));
    }

    /** Returns the dot product of this vector and {@code other}. */
    public double dotProduct(Vector other) {
        return _xyz._d1() * other._xyz._d1()
             + _xyz._d2() * other._xyz._d2()
             + _xyz._d3() * other._xyz._d3();
    }

    /** Returns the cross product of this vector and {@code other}. */
    public Vector crossProduct(Vector other) {
        return new Vector(
            _xyz._d2() * other._xyz._d3() - _xyz._d3() * other._xyz._d2(),
            _xyz._d3() * other._xyz._d1() - _xyz._d1() * other._xyz._d3(),
            _xyz._d1() * other._xyz._d2() - _xyz._d2() * other._xyz._d1()
        );
    }

    /** Returns the squared length of this vector. */
    public double lengthSquared() {
        return dotProduct(this);
    }

    /** Returns the length of this vector. */
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    /** Returns a new unit vector in the same direction as this vector. */
    public Vector normalize() {
        return new Vector(_xyz.scale(1.0 / length()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector vector)) return false;
        return _xyz.equals(vector._xyz);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
