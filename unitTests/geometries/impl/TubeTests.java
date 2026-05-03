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
    private static final Point P2 = new Point(3, 2, 4);
    private static final Point P3 = new Point(3, 2, 0);
    private static final Point P4 = new Point(3, 2, 2);
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

        Tube tube = assertDoesNotThrow(() -> new Tube(R1, new Ray(P1, V1)),
                "Tube construction should not throw an exception for valid axis and radius");

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Request the normal at a surface point whose orthogonal projection is in front of the axis ray head. The normal should point directly away from the axis.
                    Vector normal = assertDoesNotThrow(() -> tube.getNormal(P2),
                            "getNormal should not throw an exception for a valid tube surface point");

                    assertEquals(V2, normal,
                            "Tube normal is incorrect for a point opposite the axis ray");
                    assertEquals(1, normal.length(), DELTA,
                            "Tube normal must be normalized");
                },
                () -> {
                    // TC EP02: Request the normal at a surface point whose orthogonal projection is behind the axis ray head. The normal should still point directly away from the axis.
                    Vector normal = assertDoesNotThrow(() -> tube.getNormal(P3),
                            "getNormal should not throw an exception for a valid tube surface point");

                    assertEquals(V2, normal,
                            "Tube normal is incorrect for a point opposite the back of the axis ray");
                    assertEquals(1, normal.length(), DELTA,
                            "Tube normal must be normalized");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Request the normal at a surface point whose orthogonal projection is exactly the axis head. The normal should be computed from the axis head itself.
                    Vector normal = assertDoesNotThrow(() -> tube.getNormal(P4),
                            "getNormal should not throw an exception for a boundary tube surface point");

                    assertEquals(V2, normal,
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
        final double offset = Math.sqrt(3) / 2;
        final Vector acute = new Vector(1, 0, 1);
        final Vector obtuse = new Vector(1, 0, -1);
        final Vector oppositeAcute = new Vector(-1, 0, -1);
        final Vector oppositeObtuse = new Vector(-1, 0, 1);

        Tube tube = new Tube(R1, new Ray(P1, V1));
        Point nonCentralEntry = new Point(2 - offset, 2.5, 2);
        Point nonCentralExit = new Point(2 + offset, 2.5, 2);
        Point acuteEntry = new Point(2 - offset, 2.5, 2 - offset);
        Point acuteExit = new Point(2 + offset, 2.5, 2 + offset);
        Point obtuseEntry = new Point(2 - offset, 2.5, 2 + offset);
        Point obtuseExit = new Point(2 + offset, 2.5, 2 - offset);

        assertAll("Orthogonal rays to the axis",
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(0, 4, 2), V2),
                            "Orthogonal ray whose line is outside the tube should not intersect it");
                },
                () -> {
                    assertIntersections(tube, new Ray(new Point(0, 2.5, 2), V2),
                            List.of(nonCentralEntry, nonCentralExit),
                            "Orthogonal non-central ray starting before the tube should return two intersections");
                },
                () -> {
                    assertIntersections(tube, new Ray(new Point(1.5, 2.5, 2), V2),
                            List.of(nonCentralExit),
                            "Orthogonal non-central ray starting inside the tube should return one exit point");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(4, 2.5, 2), V2),
                            "Orthogonal non-central ray starting beyond the tube should not intersect it");
                });

        assertAll("Orthogonal boundary and axis-related rays",
                () -> {
                    assertIntersections(tube, new Ray(nonCentralEntry, V2),
                            List.of(nonCentralExit),
                            "Orthogonal non-central ray from the surface inward should return the opposite surface point");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(nonCentralEntry, V2.scale(-1)),
                            "Orthogonal non-central ray from the surface outward should not intersect again");
                },
                () -> {
                    assertIntersections(tube, new Ray(new Point(0, 2, 2), V2),
                            List.of(new Point(1, 2, 2), new Point(3, 2, 2)),
                            "Orthogonal axis-crossing ray starting before the tube should return two intersections");
                },
                () -> {
                    assertIntersections(tube, new Ray(new Point(1, 2, 2), V2),
                            List.of(new Point(3, 2, 2)),
                            "Orthogonal axis-crossing ray from the surface inward should return one exit point");
                },
                () -> {
                    assertIntersections(tube, new Ray(new Point(1.5, 2, 2), V2),
                            List.of(new Point(3, 2, 2)),
                            "Orthogonal axis-crossing ray starting inside the tube should return one exit point");
                },
                () -> {
                    assertIntersections(tube, new Ray(P1, V2),
                            List.of(new Point(3, 2, 2)),
                            "Orthogonal ray from the tube axis should return one surface point");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(3, 2, 2), V2),
                            "Orthogonal axis-crossing ray from the surface outward should not intersect again");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(4, 2, 2), V2),
                            "Orthogonal axis-crossing ray starting after the tube should not intersect it");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(0, 3, 2), V2),
                            "Orthogonal tangent ray starting before the tangent point should not intersect the tube");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(2, 3, 2), V2),
                            "Orthogonal tangent ray starting at the tangent point should not intersect the tube");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(4, 3, 2), V2),
                            "Orthogonal tangent ray starting after the tangent point should not intersect the tube");
                });

        assertAll("Parallel rays to the axis",
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(4, 2, 0), V1),
                            "Parallel ray outside the tube should not intersect it");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(3, 2, 0), V1),
                            "Parallel ray on the tube surface should not count as intersecting it");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(2.5, 2, 0), V1),
                            "Parallel ray inside the tube should not intersect the side surface");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(P1, V1),
                            "Parallel ray on the tube axis should not intersect the side surface");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(4, 2, 4), V1.scale(-1)),
                            "Reverse parallel ray outside the tube should not intersect it");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(3, 2, 4), V1.scale(-1)),
                            "Reverse parallel ray on the tube surface should not count as intersecting it");
                });

        assertAll("Acute-angle rays relative to the axis",
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(0, 4, 0), acute),
                            "Acute ray whose line is outside the tube should not intersect it");
                },
                () -> {
                    assertIntersections(tube, new Ray(new Point(0, 2.5, 0), acute),
                            List.of(acuteEntry, acuteExit),
                            "Acute non-central ray starting before the tube should return two intersections");
                },
                () -> {
                    assertIntersections(tube, new Ray(new Point(1.5, 2.5, 1.5), acute),
                            List.of(acuteExit),
                            "Acute non-central ray starting inside the tube should return one exit point");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(4, 2.5, 4), acute),
                            "Acute non-central ray starting beyond the tube should not intersect it");
                },
                () -> {
                    assertIntersections(tube, new Ray(acuteEntry, acute),
                            List.of(acuteExit),
                            "Acute non-central ray from the surface inward should return one exit point");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(acuteEntry, oppositeAcute),
                            "Acute non-central ray from the surface outward should not intersect again");
                },
                () -> {
                    assertIntersections(tube, new Ray(P1, acute),
                            List.of(new Point(3, 2, 3)),
                            "Acute ray from the tube axis should return one surface point");
                });

        assertAll("Obtuse-angle rays relative to the axis",
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(0, 4, 4), obtuse),
                            "Obtuse ray whose line is outside the tube should not intersect it");
                },
                () -> {
                    assertIntersections(tube, new Ray(new Point(0, 2.5, 4), obtuse),
                            List.of(obtuseEntry, obtuseExit),
                            "Obtuse non-central ray starting before the tube should return two intersections");
                },
                () -> {
                    assertIntersections(tube, new Ray(new Point(1.5, 2.5, 2.5), obtuse),
                            List.of(obtuseExit),
                            "Obtuse non-central ray starting inside the tube should return one exit point");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(new Point(4, 2.5, 0), obtuse),
                            "Obtuse non-central ray starting beyond the tube should not intersect it");
                },
                () -> {
                    assertIntersections(tube, new Ray(obtuseEntry, obtuse),
                            List.of(obtuseExit),
                            "Obtuse non-central ray from the surface inward should return one exit point");
                },
                () -> {
                    assertNoIntersections(tube, new Ray(obtuseEntry, oppositeObtuse),
                            "Obtuse non-central ray from the surface outward should not intersect again");
                },
                () -> {
                    assertIntersections(tube, new Ray(P1, obtuse),
                            List.of(new Point(3, 2, 1)),
                            "Obtuse ray from the tube axis should return one surface point");
                });
    }

    /**
     * Asserts that the tube-ray intersection result matches the expected ordered points.
     *
     * @param tube     tested tube
     * @param ray      tested ray
     * @param expected expected ordered intersection points
     * @param message  assertion message
     */
    private void assertIntersections(Tube tube, Ray ray, List<Point> expected, String message) {
        assertEquals(expected, tube.findIntersections(ray), message);
    }

    /**
     * Asserts that the given ray does not intersect the tested tube.
     *
     * @param tube    tested tube
     * @param ray     tested ray
     * @param message assertion message
     */
    private void assertNoIntersections(Tube tube, Ray ray, String message) {
        assertNull(tube.findIntersections(ray), message);
    }
}
