package primitives;

import java.util.List;

/**
 * Axis-Aligned Bounding Box (AABB) used as the cheap conservative volume of an
 * {@code Intersectable} for the Bounding Volume Hierarchy (BVH) acceleration.
 * <p>
 * A box is defined by two corner points: {@link #min} (smallest coordinate on
 * every axis) and {@link #max} (largest coordinate on every axis). Testing a ray
 * against the box is far cheaper than testing it against the geometry it wraps,
 * so a ray that misses the box can skip the contained geometry entirely.
 * </p>
 */
public class AABB {

    /**
     * Small absolute padding added around every constructed box. Flat geometries
     * (an axis-aligned floor, a triangle) produce a box that is degenerate on one
     * axis; padding keeps a grazing ray from being rejected by floating-point
     * round-off before the real intersection test runs.
     */
    private static final double EPS = 1e-5;

    /** Corner with the smallest coordinate on every axis. */
    public final Point min;
    /** Corner with the largest coordinate on every axis. */
    public final Point max;

    /**
     * Constructs a box from its two corner points as given, without padding.
     *
     * @param min corner with the smallest coordinate on every axis
     * @param max corner with the largest coordinate on every axis
     */
    public AABB(Point min, Point max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Builds the tightest padded box enclosing all of the given points.
     *
     * @param points points to enclose (at least one)
     * @return enclosing box, padded by {@link #EPS} on every side
     */
    public static AABB fromPoints(List<Point> points) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        for (Point p : points) {
            double x = p._xyz._d1(), y = p._xyz._d2(), z = p._xyz._d3();
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
        }
        return padded(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Builds the padded box of a sphere-like volume centered at {@code center}
     * with the given radius.
     *
     * @param center volume center
     * @param radius volume radius (non-negative)
     * @return enclosing box, padded by {@link #EPS} on every side
     */
    public static AABB fromCenterRadius(Point center, double radius) {
        double x = center._xyz._d1(), y = center._xyz._d2(), z = center._xyz._d3();
        return padded(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
    }

    /**
     * Builds a box from raw coordinate bounds, expanding it by {@link #EPS} on
     * every side.
     */
    private static AABB padded(double minX, double minY, double minZ,
                               double maxX, double maxY, double maxZ) {
        return new AABB(
            new Point(minX - EPS, minY - EPS, minZ - EPS),
            new Point(maxX + EPS, maxY + EPS, maxZ + EPS));
    }

    /**
     * Returns the smallest box that encloses both this box and {@code other}.
     *
     * @param other box to merge with
     * @return merged enclosing box
     */
    public AABB union(AABB other) {
        return new AABB(
            new Point(
                Math.min(min._xyz._d1(), other.min._xyz._d1()),
                Math.min(min._xyz._d2(), other.min._xyz._d2()),
                Math.min(min._xyz._d3(), other.min._xyz._d3())),
            new Point(
                Math.max(max._xyz._d1(), other.max._xyz._d1()),
                Math.max(max._xyz._d2(), other.max._xyz._d2()),
                Math.max(max._xyz._d3(), other.max._xyz._d3())));
    }

    /**
     * Returns the center coordinate of the box along the requested axis. Used by
     * the BVH builder to sort geometries before splitting.
     *
     * @param axis 0 for X, 1 for Y, 2 for Z
     * @return midpoint coordinate on that axis
     */
    public double centerCoord(int axis) {
        return switch (axis) {
            case 0 -> (min._xyz._d1() + max._xyz._d1()) / 2d;
            case 1 -> (min._xyz._d2() + max._xyz._d2()) / 2d;
            default -> (min._xyz._d3() + max._xyz._d3()) / 2d;
        };
    }

    /**
     * Returns the surface area of the box. The BVH builder minimizes the combined
     * surface area of the two child boxes when choosing a split ("smallest box"
     * heuristic): tighter boxes catch fewer rays and therefore traverse faster.
     *
     * @return surface area of the box
     */
    public double surfaceArea() {
        double dx = max._xyz._d1() - min._xyz._d1();
        double dy = max._xyz._d2() - min._xyz._d2();
        double dz = max._xyz._d3() - min._xyz._d3();
        return 2d * (dx * dy + dy * dz + dz * dx);
    }

    /**
     * Tests whether the given ray enters this box within {@code maxDistance} of
     * its origin, using the classic slab method.
     *
     * @param ray         ray to test
     * @param maxDistance maximum distance along the ray to consider
     * @return {@code true} if the ray intersects the box within range
     */
    public boolean hasIntersection(Ray ray, double maxDistance) {
        Point origin = ray.origin();
        Vector dir = ray.direction();

        double tMin = 0d;
        double tMax = maxDistance;

        // Intersect the ray with the pair of parallel planes (the "slab") of each
        // axis, shrinking [tMin, tMax] to the overlap. Empty overlap => miss.
        for (int axis = 0; axis < 3; axis++) {
            double o = coord(origin, axis);
            double d = coord(dir, axis);
            double lo = coord(min, axis);
            double hi = coord(max, axis);

            if (Math.abs(d) < EPS) {
                // Ray is parallel to this slab: it can only hit the box if its
                // origin already lies between the two planes.
                if (o < lo || o > hi) return false;
            } else {
                double inv = 1d / d;
                double t1 = (lo - o) * inv;
                double t2 = (hi - o) * inv;
                if (t1 > t2) {
                    double tmp = t1;
                    t1 = t2;
                    t2 = tmp;
                }
                if (t1 > tMin) tMin = t1;
                if (t2 < tMax) tMax = t2;
                if (tMin > tMax) return false;
            }
        }
        return true;
    }

    /** Reads the coordinate of a point on the requested axis. */
    private static double coord(Point p, int axis) {
        return switch (axis) {
            case 0 -> p._xyz._d1();
            case 1 -> p._xyz._d2();
            default -> p._xyz._d3();
        };
    }
}
