package geometries.impl;

import geometries.api.Intersectable;
import primitives.AABB;
import primitives.Ray;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Composite container of {@link Intersectable} geometries.
 * <p>
 * Besides holding a flat list of geometries, this composite can reorganize
 * itself into a Bounding Volume Hierarchy (BVH) via {@link #buildBVH()}: a
 * recursive binary tree of nested {@code Geometries} nodes, each guarded by its
 * own bounding box. A ray that misses a node's box skips that whole subtree,
 * turning the per-ray cost from linear in the number of geometries into roughly
 * logarithmic.
 * </p>
 */
public class Geometries extends Intersectable {

    /** Default cap on the depth of the automatically built BVH tree. */
    private static final int DEFAULT_MAX_DEPTH = 20;
    /** A node with at most this many geometries becomes a leaf (no further split). */
    private static final int MIN_LEAF_SIZE = 2;

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
     * Constructs a composite from a list of geometries.
     *
     * @param geometries the geometries to add initially
     */
    public Geometries(List<Intersectable> geometries) {
        this.geometries.addAll(geometries);
        invalidateBoundingBox();
    }

    /**
     * Adds the given geometries to this composite.
     *
     * @param geometries the geometries to add
     */
    public void add(Intersectable... geometries) {
        this.geometries.addAll(List.of(geometries));
        invalidateBoundingBox();
    }

    /**
     * Reorganizes the contained geometries into a Bounding Volume Hierarchy using
     * the default depth cap.
     *
     * @see #buildBVH(int)
     */
    public void buildBVH() {
        buildBVH(DEFAULT_MAX_DEPTH);
    }

    /**
     * Reorganizes the contained geometries into a recursive binary BVH tree.
     * <p>
     * Unbounded geometries (infinite planes and tubes, which have no finite box)
     * cannot be placed in the hierarchy, so they are kept at the top level and
     * always tested. All bounded geometries are recursively partitioned into two
     * groups, each wrapped in its own {@code Geometries} node, down to leaves of
     * at most {@link #MIN_LEAF_SIZE} geometries or until {@code maxDepth} is hit.
     * </p>
     *
     * @param maxDepth maximum recursion depth of the tree
     */
    public void buildBVH(int maxDepth) {
        List<Intersectable> bounded = new ArrayList<>();
        List<Intersectable> unbounded = new ArrayList<>();
        for (Intersectable geometry : geometries) {
            if (geometry.getBoundingBox() == null) {
                unbounded.add(geometry);
            } else {
                bounded.add(geometry);
            }
        }

        geometries.clear();
        if (!bounded.isEmpty()) {
            geometries.add(build(bounded, 0, maxDepth));
        }
        geometries.addAll(unbounded);
        invalidateBoundingBox();
    }

    /**
     * Recursively builds a BVH subtree from a list of bounded geometries.
     *
     * @param items    geometries to place under this node (all have a finite box)
     * @param depth    current recursion depth
     * @param maxDepth maximum recursion depth
     * @return a single {@code Intersectable}: a leaf {@code Geometries} for small
     *         lists, otherwise an internal node holding two child subtrees
     */
    private static Intersectable build(List<Intersectable> items, int depth, int maxDepth) {
        if (items.size() <= MIN_LEAF_SIZE || depth >= maxDepth) {
            return new Geometries(items);
        }

        int splitAxis = chooseSplitAxis(items);
        items.sort(Comparator.comparingDouble(g -> g.getBoundingBox().centerCoord(splitAxis)));

        int mid = items.size() / 2;
        Intersectable left = build(new ArrayList<>(items.subList(0, mid)), depth + 1, maxDepth);
        Intersectable right = build(new ArrayList<>(items.subList(mid, items.size())), depth + 1, maxDepth);
        return new Geometries(left, right);
    }

    /**
     * Chooses the axis to split along by trying all three and keeping the one that
     * yields the smallest combined child boxes. Each candidate sorts the items by
     * their box center on that axis, splits in the middle, and scores the split by
     * the summed surface area of the two halves' bounding boxes ("smallest box"
     * heuristic) — tighter boxes are missed by more rays and so traverse faster.
     *
     * @param items geometries to split (all have a finite box)
     * @return best split axis: 0 for X, 1 for Y, 2 for Z
     */
    private static int chooseSplitAxis(List<Intersectable> items) {
        int bestAxis = 0;
        double bestCost = Double.POSITIVE_INFINITY;

        for (int axis = 0; axis < 3; axis++) {
            final int a = axis;
            List<Intersectable> sorted = new ArrayList<>(items);
            sorted.sort(Comparator.comparingDouble(g -> g.getBoundingBox().centerCoord(a)));

            int mid = sorted.size() / 2;
            double cost = unionBox(sorted.subList(0, mid)).surfaceArea()
                + unionBox(sorted.subList(mid, sorted.size())).surfaceArea();
            if (cost < bestCost) {
                bestCost = cost;
                bestAxis = axis;
            }
        }
        return bestAxis;
    }

    /**
     * Computes the union of the bounding boxes of a non-empty list of bounded
     * geometries.
     *
     * @param items geometries whose boxes to merge (all have a finite box)
     * @return the smallest box enclosing all of them
     */
    private static AABB unionBox(List<Intersectable> items) {
        AABB box = items.get(0).getBoundingBox();
        for (int i = 1; i < items.size(); i++) {
            box = box.union(items.get(i).getBoundingBox());
        }
        return box;
    }

    @Override
    protected AABB calcBoundingBox() {
        AABB box = null;
        for (Intersectable geometry : geometries) {
            AABB childBox = geometry.getBoundingBox();
            // An unbounded child makes the whole composite unbounded.
            if (childBox == null) {
                return null;
            }
            box = box == null ? childBox : box.union(childBox);
        }
        return box;
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
