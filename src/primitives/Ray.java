package primitives;

/**
 * Represents a ray (half-line) in 3D space, defined by an origin point and a normalized direction vector.
 */
public class Ray {

    /** The origin point of the ray. */
    private final Point _origin;

    /** The normalized direction vector of the ray. */
    private final Vector _direction;

    /**
     * Constructs a ray from an origin point and a direction vector.
     * The direction vector is normalized before being stored.
     *
     * @param origin    the starting point of the ray
     * @param direction the direction of the ray (will be normalized)
     */
    public Ray(Point origin, Vector direction) {
        this._origin = origin;
        this._direction = direction.normalize();
    }

    /** Returns the origin point of the ray. */
    public Point origin() { return _origin; }

    /** Returns the normalized direction vector of the ray. */
    public Vector direction() { return _direction; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ray ray)) return false;
        return _origin.equals(ray._origin) && _direction.equals(ray._direction);
    }

    @Override
    public int hashCode() {
        return 31 * _origin.hashCode() + _direction.hashCode();
    }

    @Override
    public String toString() {
        return "Ray{origin=" + _origin + ", direction=" + _direction + "}";
    }
}
