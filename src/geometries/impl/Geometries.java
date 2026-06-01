package geometries.impl;

import geometries.api.Intersectable;
import primitives.Ray;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite container of {@link Intersectable} geometries.
 */
public class Geometries extends Intersectable {

    /**
     * The geometries contained in this composite.
     */
    private final List<Intersectable> geometries = new ArrayList<>();

    /**
     * Constructs a composite from the given geometries.
     *
     * @param geometries the geometries to add initially
     */
    public Geometries(Intersectable... geometries) {
        add(geometries);
    }

    /**
     * Adds the given geometries to this composite.
     *
     * @param geometries the geometries to add
     */
    public void add(Intersectable... geometries) {
        this.geometries.addAll(List.of(geometries));
    }

    /**
     * Finds all intersections between the given ray and all contained geometries.
     *
     * @param ray the ray to test
     * @return a merged list of all intersection points, or {@code null} if none are found
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        List<Intersection> intersections = null;

        for (Intersectable geometry : geometries) {
            List<Intersection> geometryIntersections = geometry.calcIntersections(ray, maxDistance);
            if (geometryIntersections != null) {
                if (intersections == null) {
                    intersections = new ArrayList<>();
                }
                intersections.addAll(geometryIntersections);
            }
        }

        return intersections;
    }
}
