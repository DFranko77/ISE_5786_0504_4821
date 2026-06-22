package geometries.api;

import lighting.LightSource;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Base type for geometric objects that can be intersected by a {@link Ray}.
 */
public abstract class  Intersectable {

    /**
     * Plain hit-data structure for a ray/geometry intersection.
     */
    public static final class Intersection {
        /** Intersected geometry instance. */
        public final Geometry geometry;
        /** Intersection point on the geometry. */
        public final Point point;
        /** Material of the intersected geometry (or default material for null geometry). */
        public final Material material;

        /** Normal vector at the intersection point (cached during shading). */
        public Vector normal;
        /** View direction vector (ray direction) used during shading. */
        public Vector v;
        /** Cached dot product v dot normal. */
        public double vNormal;

        /** Light source currently evaluated during shading. */
        public LightSource light;
        /** Direction vector from light to intersection point. */
        public Vector l;
        /** Cached dot product l dot normal. */
        public double lNormal;

        /**
         * Creates an intersection pair of geometry and point.
         *
         * @param geometry intersected geometry
         * @param point intersection point
         */
        public Intersection(Geometry geometry, Point point) {
            this.geometry = geometry;
            this.point = point;
            this.material = geometry == null ? new Material() : geometry.getMaterial();
        }

        @Override
        public String toString() {
            return "Intersection{geometry=" + geometry + ", point=" + point + "}";
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj
                || (obj instanceof Intersection other
                && geometry == other.geometry
                && point.equals(other.point));
        }
    }

    /**
     * Finds intersection points between this object and the given ray.
     *
     * @param ray the ray to test
     * @return a list of intersection points, or {@code null} if no intersections exist
     */
    public final List<Point> findIntersections(Ray ray) {
        var intersections = calcIntersections(ray);
        return intersections == null ? null
            : intersections.stream()
                .map(intersection -> intersection.point)
                .toList();
    }

    /**
     * Public NVI entry-point for full intersection details.
     *
     * @param ray ray to test
     * @return list of geometry+point intersections, or {@code null}
     */
    public final List<Intersection> calcIntersections(Ray ray) {
        return calcIntersectionsHelper(ray, Double.POSITIVE_INFINITY);
    }

    /**
     * Public NVI entry-point for full intersection details up to a maximum distance.
     *
     * @param ray         ray to test
     * @param maxDistance maximum allowed intersection distance along the ray
     * @return list of geometry+point intersections within range, or {@code null}
     */
    public final List<Intersection> calcIntersections(Ray ray, double maxDistance) {
        return calcIntersectionsHelper(ray, maxDistance);
    }

    /**
     * Internal intersection calculation hook for NVI-based APIs.
     *
     * @param ray         ray to test
     * @param maxDistance maximum allowed intersection distance along the ray
     * @return list of geometry+point intersections within range, or {@code null}
     */
    protected abstract List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance);

}
