package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

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

    /**
     * Tests {@link Tube#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        final int epTestCases = 2;
        final int bvTestCases = 1;

        Tube tube = assertDoesNotThrow(() -> new Tube(1d, new Ray(new Point(0, 0, 0), new Vector(0, 0, 1))),
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
}
