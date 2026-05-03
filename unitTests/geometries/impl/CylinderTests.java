package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Cylinder}.
 */
class CylinderTests {

    /**
     * Comparison tolerance for floating-point results returned by
     * {@link Cylinder#getNormal(Point)}.
     */
    private static final double DELTA = 1e-6;
    private static final double RADIUS = 1d;
    private static final double HEIGHT = 2d;
    private static final Ray AXIS = new Ray(Point.ZERO, Vector.AXIS_Z);
    private static final Cylinder C1 = new Cylinder(RADIUS, AXIS, HEIGHT);
    private static final Vector DOWN = new Vector(0, 0, -1);
    private static final Vector UP = Vector.AXIS_Z;

    /**
     * Tests {@link Cylinder#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        final int epTestCases = 3;
        final int bvTestCases = 4;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Point on the curved side away from the cylinder bases.
                    Vector normal = assertDoesNotThrow(() -> C1.getNormal(new Point(1, 0, 1)),
                            "getNormal should not throw an exception for a valid cylinder surface point");

                    assertEquals(Vector.AXIS_X, normal,
                            "Cylinder normal is incorrect for an interior point on the curved surface");
                    assertEquals(1, normal.length(), DELTA,
                            "Cylinder normal must be normalized");
                },
                () -> {
                    // TC EP02: Point on the bottom base away from its center and edge.
                    Vector normal = assertDoesNotThrow(() -> C1.getNormal(new Point(0.5, 0, 0)),
                            "getNormal should not throw an exception for a valid bottom-base point");

                    assertEquals(DOWN, normal,
                            "Cylinder normal is incorrect for an interior point on the bottom base");
                    assertEquals(1, normal.length(), DELTA,
                            "Cylinder normal must be normalized");
                },
                () -> {
                    // TC EP03: Point on the top base away from its center and edge.
                    Vector normal = assertDoesNotThrow(() -> C1.getNormal(new Point(0.5, 0, HEIGHT)),
                            "getNormal should not throw an exception for a valid top-base point");

                    assertEquals(UP, normal,
                            "Cylinder normal is incorrect for an interior point on the top base");
                    assertEquals(1, normal.length(), DELTA,
                            "Cylinder normal must be normalized");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Point at the center of the bottom base.
                    Vector normal = assertDoesNotThrow(() -> C1.getNormal(Point.ZERO),
                            "getNormal should not throw an exception for the bottom-base center");

                    assertEquals(DOWN, normal,
                            "Cylinder normal is incorrect at the bottom-base center");
                    assertEquals(1, normal.length(), DELTA,
                            "Cylinder normal must be normalized");
                },
                () -> {
                    // TC BV02: Point at the center of the top base.
                    Vector normal = assertDoesNotThrow(() -> C1.getNormal(new Point(0, 0, HEIGHT)),
                            "getNormal should not throw an exception for the top-base center");

                    assertEquals(UP, normal,
                            "Cylinder normal is incorrect at the top-base center");
                    assertEquals(1, normal.length(), DELTA,
                            "Cylinder normal must be normalized");
                },
                () -> {
                    // TC BV03: Point on the rim of the bottom base.
                    Vector normal = assertDoesNotThrow(() -> C1.getNormal(new Point(1, 0, 0)),
                            "getNormal should not throw an exception for the bottom-base rim");

                    assertEquals(DOWN, normal,
                            "Cylinder normal is incorrect at the bottom-base rim");
                    assertEquals(1, normal.length(), DELTA,
                            "Cylinder normal must be normalized");
                },
                () -> {
                    // TC BV04: Point on the rim of the top base.
                    Vector normal = assertDoesNotThrow(() -> C1.getNormal(new Point(1, 0, HEIGHT)),
                            "getNormal should not throw an exception for the top-base rim");

                    assertEquals(UP, normal,
                            "Cylinder normal is incorrect at the top-base rim");
                    assertEquals(1, normal.length(), DELTA,
                            "Cylinder normal must be normalized");
                });
    }

    /**
     * Tests {@link Cylinder#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        final double offset = Math.sqrt(3) / 2;
        Point shellEntry = new Point(-offset, 0.5, 1);
        Point shellExit = new Point(offset, 0.5, 1);

        assertAll("Axis-parallel rays through the caps",
                () -> assertIntersections(C1, new Ray(new Point(0, 0, -1), Vector.AXIS_Z),
                        List.of(Point.ZERO, new Point(0, 0, HEIGHT)),
                        "Axis-parallel ray from below through the center should hit both bases"),
                () -> assertIntersections(C1, new Ray(Point.ZERO, Vector.AXIS_Z),
                        List.of(new Point(0, 0, HEIGHT)),
                        "Axis-parallel ray from the bottom-base center should hit only the top base"),
                () -> assertIntersections(C1, new Ray(new Point(0, 0, 1), Vector.AXIS_Z),
                        List.of(new Point(0, 0, HEIGHT)),
                        "Axis-parallel ray from inside the cylinder should hit only the top base"),
                () -> assertNoIntersections(C1, new Ray(new Point(0, 0, HEIGHT), Vector.AXIS_Z),
                        "Axis-parallel ray from the top-base center outward should not intersect again"),
                () -> assertNoIntersections(C1, new Ray(new Point(0, 0, 3), Vector.AXIS_Z),
                        "Axis-parallel ray starting beyond the cylinder should not intersect it"),
                () -> assertIntersections(C1, new Ray(new Point(0.5, 0, -1), Vector.AXIS_Z),
                        List.of(new Point(0.5, 0, 0), new Point(0.5, 0, HEIGHT)),
                        "Axis-parallel ray below the cylinder inside the cap disk should hit both bases"),
                () -> assertIntersections(C1, new Ray(new Point(0.5, 0, 0), Vector.AXIS_Z),
                        List.of(new Point(0.5, 0, HEIGHT)),
                        "Axis-parallel ray from the bottom base interior should hit only the top base"),
                () -> assertIntersections(C1, new Ray(new Point(0.5, 0, 1), Vector.AXIS_Z),
                        List.of(new Point(0.5, 0, HEIGHT)),
                        "Axis-parallel ray from inside off the axis should hit only the top base"),
                () -> assertNoIntersections(C1, new Ray(new Point(2, 0, -1), Vector.AXIS_Z),
                        "Axis-parallel ray outside the cylinder radius should not intersect it"),
                () -> assertIntersections(C1, new Ray(new Point(1, 0, -1), Vector.AXIS_Z),
                        List.of(new Point(1, 0, 0), new Point(1, 0, HEIGHT)),
                        "Axis-parallel ray on the seam line should count both seam points"),
                () -> assertIntersections(C1, new Ray(new Point(1, 0, 0), Vector.AXIS_Z),
                        List.of(new Point(1, 0, HEIGHT)),
                        "Axis-parallel ray from the bottom seam should hit only the top seam"),
                () -> assertIntersections(C1, new Ray(new Point(0.5, 0, 3), DOWN),
                        List.of(new Point(0.5, 0, HEIGHT), new Point(0.5, 0, 0)),
                        "Reverse axis-parallel ray through the cylinder should return the top hit before the bottom hit"));

        assertAll("Orthogonal shell-only rays",
                () -> assertNoIntersections(C1, new Ray(new Point(-2, 2, 1), Vector.AXIS_X),
                        "Orthogonal ray whose line is outside the cylinder shell should not intersect it"),
                () -> assertIntersections(C1, new Ray(new Point(-2, 0.5, 1), Vector.AXIS_X),
                        List.of(shellEntry, shellExit),
                        "Orthogonal non-central ray through the shell should return two side intersections"),
                () -> assertIntersections(C1, new Ray(new Point(-0.5, 0.5, 1), Vector.AXIS_X),
                        List.of(shellExit),
                        "Orthogonal non-central ray starting inside should return one shell exit"),
                () -> assertNoIntersections(C1, new Ray(new Point(2, 0.5, 1), Vector.AXIS_X),
                        "Orthogonal non-central ray starting beyond the shell should not intersect it"),
                () -> assertIntersections(C1, new Ray(shellEntry, Vector.AXIS_X),
                        List.of(shellExit),
                        "Orthogonal non-central ray from the shell inward should return one exit point"),
                () -> assertNoIntersections(C1, new Ray(shellEntry, new Vector(-1, 0, 0)),
                        "Orthogonal non-central ray from the shell outward should not intersect again"),
                () -> assertIntersections(C1, new Ray(new Point(-2, 0, 1), Vector.AXIS_X),
                        List.of(new Point(-1, 0, 1), new Point(1, 0, 1)),
                        "Orthogonal axis-crossing ray starting before the cylinder should return both shell hits"),
                () -> assertIntersections(C1, new Ray(new Point(-1, 0, 1), Vector.AXIS_X),
                        List.of(new Point(1, 0, 1)),
                        "Orthogonal axis-crossing ray from the shell inward should return one exit point"),
                () -> assertIntersections(C1, new Ray(new Point(-0.5, 0, 1), Vector.AXIS_X),
                        List.of(new Point(1, 0, 1)),
                        "Orthogonal axis-crossing ray starting inside should return one exit point"),
                () -> assertIntersections(C1, new Ray(new Point(0, 0, 1), Vector.AXIS_X),
                        List.of(new Point(1, 0, 1)),
                        "Orthogonal ray from the cylinder axis should return one shell point"),
                () -> assertNoIntersections(C1, new Ray(new Point(1, 0, 1), Vector.AXIS_X),
                        "Orthogonal axis-crossing ray from the shell outward should not intersect again"),
                () -> assertNoIntersections(C1, new Ray(new Point(2, 0, 1), Vector.AXIS_X),
                        "Orthogonal axis-crossing ray starting after the cylinder should not intersect it"),
                () -> assertNoIntersections(C1, new Ray(new Point(-2, 1, 1), Vector.AXIS_X),
                        "Orthogonal tangent ray before the cylinder should not intersect it"),
                () -> assertNoIntersections(C1, new Ray(new Point(0, 1, 1), Vector.AXIS_X),
                        "Orthogonal tangent ray from the tangent point should not intersect it"),
                () -> assertNoIntersections(C1, new Ray(new Point(2, 1, 1), Vector.AXIS_X),
                        "Orthogonal tangent ray after the cylinder should not intersect it"));

        assertAll("Mixed shell and cap rays",
                () -> assertIntersections(C1, new Ray(new Point(0.5, 0, -1), new Vector(1, 0, 5)),
                        List.of(new Point(0.7, 0, 0), new Point(1, 0, 1.5)),
                        "Angled ray from below should hit the bottom base and then exit through the shell"),
                () -> assertIntersections(C1, new Ray(new Point(0.5, 0, 3), new Vector(1, 0, -5)),
                        List.of(new Point(0.7, 0, HEIGHT), new Point(1, 0, 0.5)),
                        "Angled ray from above should hit the top base and then exit through the shell"),
                () -> assertIntersections(C1, new Ray(new Point(-2, 0, 1), new Vector(2, 0, 1)),
                        List.of(new Point(-1, 0, 1.5), new Point(0, 0, HEIGHT)),
                        "Angled ray should hit the shell first and then the top base"),
                () -> assertIntersections(C1, new Ray(new Point(-2, 0, 1), new Vector(2, 0, -1)),
                        List.of(new Point(-1, 0, 0.5), Point.ZERO),
                        "Angled ray should hit the shell first and then the bottom base"),
                () -> assertIntersections(C1, new Ray(new Point(0, 0, -1), new Vector(1, 0, 1)),
                        List.of(new Point(1, 0, 0)),
                        "Angled ray should count a bottom seam hit when it reaches the shell-base connection point"),
                () -> assertIntersections(C1, new Ray(new Point(0, 0, 3), new Vector(1, 0, -1)),
                        List.of(new Point(1, 0, HEIGHT)),
                        "Angled ray should count a top seam hit when it reaches the shell-base connection point"),
                () -> assertNoIntersections(C1, new Ray(new Point(0, 2, -1), new Vector(1, 0, 1)),
                        "Acute angled ray whose line stays outside the finite cylinder should not intersect it"),
                () -> assertNoIntersections(C1, new Ray(new Point(0, 2, 3), new Vector(1, 0, -1)),
                        "Obtuse angled ray whose line stays outside the finite cylinder should not intersect it"));
    }

    /**
     * Asserts that the cylinder-ray intersection result matches the expected ordered points.
     *
     * @param cylinder tested cylinder
     * @param ray      tested ray
     * @param expected expected ordered intersection points
     * @param message  assertion message
     */
    private void assertIntersections(Cylinder cylinder, Ray ray, List<Point> expected, String message) {
        assertEquals(expected, cylinder.findIntersections(ray), message);
    }

    /**
     * Asserts that the given ray does not intersect the tested cylinder.
     *
     * @param cylinder tested cylinder
     * @param ray      tested ray
     * @param message  assertion message
     */
    private void assertNoIntersections(Cylinder cylinder, Ray ray, String message) {
        assertNull(cylinder.findIntersections(ray), message);
    }
}
