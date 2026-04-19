package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Tube}.
 */
class TubeTests {

    /**
     * Comparison tolerance for floating-point results returned by
     * {@link Tube#getNormal(Point)}.
     */
    private static final double DELTA = 1e-6;
    private static final Point P1 = new Point(2, 2, 2);
    private static final Vector V1 = Vector.AXIS_Z;
    private static final double R1 = 1d;
    private static final Vector V2 = Vector.AXIS_X;

    /**
     * Tests {@link Tube#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        final int epTestCases = 2;
        final int bvTestCases = 1;

        Tube tube = assertDoesNotThrow(() -> new Tube(R1, new Ray(Point.ZERO, V1)),
                "Tube construction should not throw an exception for valid axis and radius");

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Request the normal at a surface point whose orthogonal projection is in front of the axis ray head. The normal should point directly away from the axis.
                    Vector normal = assertDoesNotThrow(() -> tube.getNormal(new Point(1, 0, 2)),
                            "getNormal should not throw an exception for a valid tube surface point");

                    assertEquals(new Vector(1, 0, 0), normal,
                            "Tube normal is incorrect for a point opposite the axis ray");
                    assertEquals(1, normal.length(), DELTA,
                            "Tube normal must be normalized");
                },
                () -> {
                    // TC EP02: Request the normal at a surface point whose orthogonal projection is behind the axis ray head. The normal should still point directly away from the axis.
                    Vector normal = assertDoesNotThrow(() -> tube.getNormal(new Point(1, 0, -2)),
                            "getNormal should not throw an exception for a valid tube surface point");

                    assertEquals(new Vector(1, 0, 0), normal,
                            "Tube normal is incorrect for a point opposite the back of the axis ray");
                    assertEquals(1, normal.length(), DELTA,
                            "Tube normal must be normalized");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Request the normal at a surface point whose orthogonal projection is exactly the axis head. The normal should be computed from the axis head itself.
                    Vector normal = assertDoesNotThrow(() -> tube.getNormal(new Point(1, 0, 0)),
                            "getNormal should not throw an exception for a boundary tube surface point");

                    assertEquals(new Vector(1, 0, 0), normal,
                            "Tube normal is incorrect for a point opposite the axis head");
                    assertEquals(1, normal.length(), DELTA,
                            "Tube normal must be normalized");
                });
    }

    /**
     * Tests {@link Tube#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        final int epTestCases = 4;
        final int bvTestCases = 11;
        final double offset = Math.sqrt(3) / 2;

        Tube tube = new Tube(R1, new Ray(P1, V1));

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Ray line is outside the tube. No intersections are expected.
                    Ray ray = new Ray(new Point(0, 4, 2), V2);
                    assertNull(tube.findIntersections(ray),
                            "Ray whose line is outside the tube should not intersect it");
                },
                () -> {
                    // TC EP02: Ray starts before the tube and crosses it. Two forward intersections are expected in ray order.
                    Ray ray = new Ray(new Point(0, 2.5, 2), V2);
                    assertEquals(List.of(
                                    new Point(2 - offset, 2.5, 2),
                                    new Point(2 + offset, 2.5, 2)),
                            tube.findIntersections(ray),
                            "Tube should return both forward intersection points in ray order");
                },
                () -> {
                    // TC EP03: Ray starts inside the tube on a non-central line. One exit intersection is expected.
                    Ray ray = new Ray(new Point(1.5, 2.5, 2), V2);
                    assertEquals(List.of(new Point(2 + offset, 2.5, 2)), tube.findIntersections(ray),
                            "Ray starting inside the tube should return the single exit point");
                },
                () -> {
                    // TC EP04: Ray starts after the tube on an intersecting line. No forward intersections are expected.
                    Ray ray = new Ray(new Point(4, 2.5, 2), V2);
                    assertNull(tube.findIntersections(ray),
                            "Ray starting beyond the tube should not intersect it");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV11: Ray starts on the tube and goes inward on a line that does not pass through the axis.
                    Ray ray = new Ray(new Point(2 - offset, 2.5, 2), V2);
                    assertEquals(List.of(new Point(2 + offset, 2.5, 2)), tube.findIntersections(ray),
                            "Ray starting on the tube and going inward should return the second surface point");
                },
                () -> {
                    // TC BV12: Ray starts on the tube and goes outward on a line that does not pass through the axis.
                    Ray ray = new Ray(new Point(2 - offset, 2.5, 2), new Vector(-1, 0, 0));
                    assertNull(tube.findIntersections(ray),
                            "Ray starting on the tube and going outward should not intersect it again");
                },
                () -> {
                    // TC BV21: Ray goes through the tube axis and starts before the tube.
                    Ray ray = new Ray(new Point(0, 2, 2), V2);
                    assertEquals(List.of(new Point(1, 2, 2), new Point(3, 2, 2)), tube.findIntersections(ray),
                            "Axis-crossing ray starting before the tube should return two intersections");
                },
                () -> {
                    // TC BV22: Ray goes through the tube axis and starts on the tube heading inward.
                    Ray ray = new Ray(new Point(1, 2, 2), V2);
                    assertEquals(List.of(new Point(3, 2, 2)), tube.findIntersections(ray),
                            "Axis-crossing ray starting on the tube and heading inward should return one exit point");
                },
                () -> {
                    // TC BV23: Ray goes through the tube axis and starts inside the tube.
                    Ray ray = new Ray(new Point(1.5, 2, 2), V2);
                    assertEquals(List.of(new Point(3, 2, 2)), tube.findIntersections(ray),
                            "Axis-crossing ray starting inside the tube should return one exit point");
                },
                () -> {
                    // TC BV24: Ray starts on the tube axis.
                    Ray ray = new Ray(P1, V2);
                    assertEquals(List.of(new Point(3, 2, 2)), tube.findIntersections(ray),
                            "Ray from the tube axis should return one surface point");
                },
                () -> {
                    // TC BV25: Ray goes through the tube axis and starts on the tube heading outward.
                    Ray ray = new Ray(new Point(3, 2, 2), V2);
                    assertNull(tube.findIntersections(ray),
                            "Axis-crossing ray starting on the tube and heading outward should not intersect it again");
                },
                () -> {
                    // TC BV31: Ray line is tangent to the tube and starts before the tangent point.
                    Ray ray = new Ray(new Point(0, 3, 2), V2);
                    assertNull(tube.findIntersections(ray),
                            "Ray approaching the tangent point should not count as intersecting the tube");
                },
                () -> {
                    // TC BV32: Ray starts at the tangent point.
                    Ray ray = new Ray(new Point(2, 3, 2), V2);
                    assertNull(tube.findIntersections(ray),
                            "Ray starting at the tangent point should not intersect the tube");
                },
                () -> {
                    // TC BV33: Ray line is tangent to the tube and starts after the tangent point.
                    Ray ray = new Ray(new Point(4, 3, 2), V2);
                    assertNull(tube.findIntersections(ray),
                            "Ray starting after the tangent point should not intersect the tube");
                },
                () -> {
                    // TC BV41: Ray is parallel to the tube axis and outside the tube.
                    Ray ray = new Ray(new Point(4, 2, 2), Vector.AXIS_Z);
                    assertNull(tube.findIntersections(ray),
                            "Ray parallel to the tube axis outside the tube should not intersect it");
                });
    }
}
