package geometries.api;

import primitives.Point;
import primitives.Vector;

import java.io.Serializable;

/**
 * Base type for geometric objects.
 */
public abstract class Geometry implements Intersectable {


    /**
     * Returns the normal vector to the geometry at the given point.
     *
     * @param point a point on the surface of the geometry
     * @return the normalized normal vector at that point
     */
    public abstract Vector getNormal(Point point);
}
