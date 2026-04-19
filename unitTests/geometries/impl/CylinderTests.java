package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

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
    private static final double HEIGHT = 5d;
    private static final Ray AXIS = new Ray(Point.ZERO, Vector.AXIS_Z);
    private static final Cylinder C1 = new Cylinder(RADIUS, AXIS, HEIGHT);

    private static final Point P1 = new Point(1, 0, 2);
    private static final Point P2 = new Point(0, 1, -2);
    private static final Point P3 = new Point(-1, 0, 3);

    private static final Vector V1 = new Vector(1, 0, 0);
    private static final Vector V2 = new Vector(0, 1, 0);
    private static final Vector V3 = new Vector(-1, 0, 0);

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
                    // TC EP01: Surface point with positive projection on the axis direction.
                    Vector normal = assertDoesNotThrow(() -> C1.getNormal(P1),
                            "getNormal should not throw an exception for a valid cylinder surface point");

                    assertEquals(V1, normal,
                            "Cylinder normal is incorrect for a point on the curved surface");
                    assertEquals(1, normal.length(), DELTA,
                            "Cylinder normal must be normalized");
                },
                () -> {
                    // TC EP02: Surface point with negative projection on the axis direction.
                    Vector normal = assertDoesNotThrow(() -> C1.getNormal(P2),
                            "getNormal should not throw an exception for a valid cylinder surface point");

                    assertEquals(V2, normal,
                            "Cylinder normal is incorrect for a point behind the axis origin");
                    assertEquals(1, normal.length(), DELTA,
                            "Cylinder normal must be normalized");
                },
                () -> {
                    // TC EP03: Surface point on a different radial direction around the same axis.
                    Vector normal = assertDoesNotThrow(() -> C1.getNormal(P3),
                            "getNormal should not throw an exception for a valid cylinder surface point");

                    assertEquals(V3, normal,
                            "Cylinder normal is incorrect for a point on the opposite radial side");
                    assertEquals(1, normal.length(), DELTA,
                            "Cylinder normal must be normalized");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Projection at the axis origin (t = 0).
                    Vector normal = assertDoesNotThrow(() -> C1.getNormal(new Point(1, 0, 0)),
                            "getNormal should not throw an exception when projection is exactly at axis origin");

                    assertEquals(V1, normal,
                            "Cylinder normal is incorrect at projection boundary t = 0");
                    assertEquals(1, normal.length(), DELTA,
                            "Cylinder normal must be normalized");
                },
                () -> {
                    // TC BV02: First integer projection after axis origin (t = 1).
                    Vector normal = assertDoesNotThrow(() -> C1.getNormal(new Point(1, 0, 1)),
                            "getNormal should not throw an exception for boundary-adjacent projection t = 1");

                    assertEquals(V1, normal,
                            "Cylinder normal is incorrect at projection t = 1");
                    assertEquals(1, normal.length(), DELTA,
                            "Cylinder normal must be normalized");
                },
                () -> {
                    // TC BV03: First integer projection before axis origin (t = -1).
                    Vector normal = assertDoesNotThrow(() -> C1.getNormal(new Point(1, 0, -1)),
                            "getNormal should not throw an exception for boundary-adjacent projection t = -1");

                    assertEquals(V1, normal,
                            "Cylinder normal is incorrect at projection t = -1");
                    assertEquals(1, normal.length(), DELTA,
                            "Cylinder normal must be normalized");
                },
                () -> {
                    // TC BV04: Opposite radial side at projection boundary (t = 0).
                    Vector normal = assertDoesNotThrow(() -> C1.getNormal(new Point(-1, 0, 0)),
                            "getNormal should not throw an exception for opposite radial boundary case");

                    assertEquals(V3, normal,
                            "Cylinder normal is incorrect on opposite radial boundary");
                    assertEquals(1, normal.length(), DELTA,
                            "Cylinder normal must be normalized");
                });
    }
}
