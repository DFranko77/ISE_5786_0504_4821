package geometries.impl;

import primitives.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a finite cylinder (tube with two caps) in 3D space.
 */
public class Cylinder extends Tube {

    /** The height of the cylinder. */
    private final double _height;
    /** Cached center point of the top base. */
    private final Point _topCenter;

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
        this._topCenter = axis.getPoint(height);
    }

    @Override
    public Vector getNormal(Point point) {
        Point baseCenter = _axis.origin();
        Vector axisDirection = _axis.direction();

        if (point.equals(baseCenter)) {
            return axisDirection.scale(-1);
        }
        if (point.equals(_topCenter)) {
            return axisDirection;
        }

        double projection = alignZero(axisDirection.dotProduct(point.subtract(baseCenter)));

        if (isZero(projection)) {
            return axisDirection.scale(-1);
        }
        if (isZero(projection - _height)) {
            return axisDirection;
        }

        Point axisPoint = _axis.getPoint(projection);
        return point.subtract(axisPoint).normalize();
    }

    /**
     * Conservative bounding box of the finite cylinder: the union of a sphere-box
     * around each cap center, each expanded by the radius. This over-estimates the
     * true box for a tilted cylinder but is always correct and cheap to compute.
     *
     * @return enclosing box
     */
    @Override
    protected AABB calcBoundingBox() {
        return AABB.fromCenterRadius(_axis.origin(), _radius)
            .union(AABB.fromCenterRadius(_topCenter, _radius));
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        List<Intersection> intersections = new ArrayList<>(4);

        List<Intersection> sideIntersections = super.calcIntersectionsHelper(ray, maxDistance);
        if (sideIntersections != null) {
            for (Intersection intersection : sideIntersections) {
                if (isOnFiniteCylinderShell(intersection.point)) {
                    addIntersection(intersections, intersection);
                }
            }
        }

        Point bottomBaseIntersection = findBaseIntersection(ray, _axis.origin(), maxDistance);
        if (bottomBaseIntersection != null) {
            addIntersection(intersections, new Intersection(this, bottomBaseIntersection));
        }

        Point topBaseIntersection = findBaseIntersection(ray, _topCenter, maxDistance);
        if (topBaseIntersection != null) {
            addIntersection(intersections, new Intersection(this, topBaseIntersection));
        }

        if (intersections.isEmpty()) {
            return null;
        }

        intersections.sort(Comparator.comparingDouble(i -> ray.origin().distanceSquared(i.point)));
        return intersections;
    }

    /**
     * Checks whether a point on the infinite tube lies on the finite cylinder shell.
     *
     * @param point point known to lie on the infinite tube
     * @return {@code true} when the point projection is within the cylinder height range
     */
    private boolean isOnFiniteCylinderShell(Point point) {
        double projection = alignZero(_axis.direction().dotProduct(point.subtract(_axis.origin())));
        return projection >= 0 && alignZero(projection - _height) <= 0;
    }

    /**
     * Finds the ray intersection with one cylinder base disk.
     *
     * @param ray       ray to intersect
     * @param baseCenter center of the tested base disk
     * @return the disk intersection point, or {@code null} when no forward hit exists
     */
    private Point findBaseIntersection(Ray ray, Point baseCenter, double maxDistance) {
        double denominator = alignZero(_axis.direction().dotProduct(ray.direction()));
        if (isZero(denominator)) {
            return null;
        }

        double numerator;
        try {
            numerator = alignZero(_axis.direction().dotProduct(baseCenter.subtract(ray.origin())));
        } catch (IllegalArgumentException e) {
            return null;
        }

        double t = alignZero(numerator / denominator);
        if (t <= 0 || alignZero(t - maxDistance) > 0) {
            return null;
        }

        Point intersection = ray.getPoint(t);
        return alignZero(intersection.distanceSquared(baseCenter) - _radiusSquared) <= 0
                ? intersection
                : null;
    }

    /**
     * Adds a new intersection point unless an equal point is already present.
     *
     * @param intersections target list
     * @param intersection  candidate intersection
     */
    private void addIntersection(List<Intersection> intersections, Intersection intersection) {
        for (Intersection existing : intersections) {
            if (existing.equals(intersection)) {
                return;
            }
        }
        intersections.add(intersection);
    }
}
