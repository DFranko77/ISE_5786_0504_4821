package geometries.api;

import primitives.Point;
import primitives.Ray;

import java.util.List;

/**
 * Base type for geometric objects that can be intersected by a {@link Ray}.
 */
public abstract class  Intersectable {
    /**
     * Finds intersection points between this object and the given ray.
     *
     * @param ray the ray to test
     * @return a list of intersection points, or {@code null} if no intersections exist
     */
    public abstract List<Point> findIntersections(Ray ray);

}
