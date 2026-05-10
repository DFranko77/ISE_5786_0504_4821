package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Sphere}.
 */
class SphereTests {

    /**
     * Comparison tolerance for floating-point results returned by
     * {@link Sphere#getNormal(Point)}.
     */
    private static final double DELTA = 1e-6;
    private static final Point P1 = new Point(2, 1, 1);
    private static final double R1 = 1d;
    private static final Vector V1 = Vector.AXIS_X;

    /**
     * Tests {@link Sphere#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        final int epTestCases = 1;

        Sphere sphere = assertDoesNotThrow(() -> new Sphere(new Point(1, 0, 0), R1),
                "Sphere construction should not throw an exception for valid center and radius");

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Request the normal at a point on the sphere surface. The result should be a unit vector from the center toward that surface point.
                    Point surfacePoint = new Point(1, 1, 0);
                    Vector normal = assertDoesNotThrow(() -> sphere.getNormal(surfacePoint),
                            "getNormal should not throw an exception for a sphere surface point");

                    assertEquals(new Vector(0, 1, 0), normal,
                            "Sphere normal must point from the center to the surface point");
                    assertEquals(1, normal.length(), DELTA,
                            "Sphere normal must be normalized");
                });
    }

    /**
     * Tests {@link Sphere#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        final int epTestCases = 4;
        final int bvTestCases = 13;
        final double offset = Math.sqrt(3) / 2;

        Sphere sphere = new Sphere(P1, R1);

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Ray line is outside the sphere. No intersections are expected.
                    Ray ray = new Ray(new Point(0, 3, 1), V1);
                    assertNull(sphere.findIntersections(ray),
                            "Ray whose line is outside the sphere should not intersect it");
                },
                () -> {
                    // TC EP02: Ray starts before the sphere and crosses it. Two forward intersections are expected.
                    Ray ray = new Ray(new Point(0, 1.5, 1), V1);
                    assertEquals(List.of(
                                    new Point(2 - offset, 1.5, 1),
                                    new Point(2 + offset, 1.5, 1)),
                            sphere.findIntersections(ray),
                            "Sphere should return both forward intersection points in ray order");
                },
                () -> {
                    // TC EP03: Ray starts inside the sphere on a non-central, non-orthogonal line. One exit intersection is expected.
                    Ray ray = new Ray(new Point(1.5, 1.5, 1), V1);
                    assertEquals(List.of(new Point(2 + offset, 1.5, 1)), sphere.findIntersections(ray),
                            "Ray starting inside the sphere should return the single exit point");
                },
                () -> {
                    // TC EP04: Ray starts after the sphere on an intersecting line. No forward intersections are expected.
                    Ray ray = new Ray(new Point(4, 1.5, 1), V1);
                    assertNull(sphere.findIntersections(ray),
                            "Ray starting beyond the sphere should not intersect it");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV11: Ray starts on the sphere and goes inward on a line that does not pass through the center.
                    Ray ray = new Ray(new Point(2 - offset, 1.5, 1), V1);
                    assertEquals(List.of(new Point(2 + offset, 1.5, 1)), sphere.findIntersections(ray),
                            "Ray starting on the sphere and going inward should return the second surface point");
                },
                () -> {
                    // TC BV12: Ray starts on the sphere and goes outward on a line that does not pass through the center.
                    Ray ray = new Ray(new Point(2 - offset, 1.5, 1), new Vector(-1, 0, 0));
                    assertNull(sphere.findIntersections(ray),
                            "Ray starting on the sphere and going outward should not intersect it again");
                },
                () -> {
                    // TC BV21: Ray goes through the center and starts before the sphere.
                    Ray ray = new Ray(new Point(0, 1, 1), V1);
                    assertEquals(List.of(new Point(1, 1, 1), new Point(3, 1, 1)), sphere.findIntersections(ray),
                            "Central ray starting before the sphere should return two intersections");
                },
                () -> {
                    // TC BV22: Ray goes through the center and starts on the sphere heading inward.
                    Ray ray = new Ray(new Point(1, 1, 1), V1);
                    assertEquals(List.of(new Point(3, 1, 1)), sphere.findIntersections(ray),
                            "Central ray starting on the sphere and heading inward should return one exit point");
                },
                () -> {
                    // TC BV23: Ray goes through the center and starts inside the sphere.
                    Ray ray = new Ray(new Point(1.5, 1, 1), V1);
                    assertEquals(List.of(new Point(3, 1, 1)), sphere.findIntersections(ray),
                            "Central ray starting inside the sphere should return one exit point");
                },
                () -> {
                    // TC BV24: Ray starts at the sphere center.
                    Ray ray = new Ray(P1, V1);
                    assertEquals(List.of(new Point(3, 1, 1)), sphere.findIntersections(ray),
                            "Ray from the sphere center should return one surface point");
                },
                () -> {
                    // TC BV25: Ray goes through the center and starts on the sphere heading outward.
                    Ray ray = new Ray(new Point(3, 1, 1), V1);
                    assertNull(sphere.findIntersections(ray),
                            "Central ray starting on the sphere and heading outward should not intersect it again");
                },
                () -> {
                    // TC BV26: Ray goes through the center line and starts after the sphere.
                    Ray ray = new Ray(new Point(4, 1, 1), V1);
                    assertNull(sphere.findIntersections(ray),
                            "Central ray starting after the sphere should not intersect it");
                },
                () -> {
                    // TC BV31: Ray line is tangent to the sphere and starts before the tangent point.
                    Ray ray = new Ray(new Point(0, 2, 1), V1);
                    assertNull(sphere.findIntersections(ray),
                            "Ray approaching the tangent point should not count as intersecting the sphere");
                },
                () -> {
                    // TC BV32: Ray starts at the tangent point.
                    Ray ray = new Ray(new Point(2, 2, 1), V1);
                    assertNull(sphere.findIntersections(ray),
                            "Ray starting at the tangent point should not intersect the sphere");
                },
                () -> {
                    // TC BV33: Ray line is tangent to the sphere and starts after the tangent point.
                    Ray ray = new Ray(new Point(4, 2, 1), V1);
                    assertNull(sphere.findIntersections(ray),
                            "Ray starting after the tangent point should not intersect the sphere");
                },
                () -> {
                    // TC BV41: Ray is orthogonal to the line from its start point to the sphere center and starts outside the sphere.
                    Ray ray = new Ray(new Point(2, 3, 1), V1);
                    assertNull(sphere.findIntersections(ray),
                            "Orthogonal ray outside the sphere should not intersect it");
                },
                () -> {
                    // TC BV42: Ray is orthogonal to the line from its start point to the sphere center and starts inside the sphere.
                    Ray ray = new Ray(new Point(2, 1.5, 1), V1);
                    assertEquals(List.of(new Point(2 + offset, 1.5, 1)), sphere.findIntersections(ray),
                            "Orthogonal ray starting inside the sphere should return one exit point");
                });
    }

    /**
     * Tests {@link Sphere#calcIntersections(Ray)} geometry binding.
     */
    @Test
    void testCalcIntersections() {
        Sphere sphere = new Sphere(P1, R1);
        Ray ray = new Ray(new Point(0, 1.5, 1), V1);

        var intersections = sphere.calcIntersections(ray);

        assertNotNull(intersections, "Expected full intersection details");
        assertEquals(2, intersections.size(), "Expected two sphere intersections");
        assertSame(sphere, intersections.get(0).geometry,
                "calcIntersections must return the exact sphere instance in each hit");
        assertSame(sphere, intersections.get(1).geometry,
                "calcIntersections must return the exact sphere instance in each hit");
    }
}
