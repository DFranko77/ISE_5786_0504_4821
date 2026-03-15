package geometries.api;

import primitives.Point;
import primitives.Vector;

/**
 * Base type for geometric objects.
 */
public abstract class Geometry {

    /**
     * Returns the normal vector to the geometry at the given point.
     *
     * @param point a point on the surface of the geometry
     * @return the normalized normal vector at that point
     */
    public abstract Vector getNormal(Point point);
}
