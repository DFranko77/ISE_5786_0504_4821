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

    /**
     * Tests {@link Cylinder#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        final int epTestCases = 1;

        Cylinder cylinder = assertDoesNotThrow(
                () -> new Cylinder(1d, new Ray(new Point(0, 0, 0), new Vector(0, 0, 1)), 5d),
                "Cylinder construction should not throw an exception for valid axis, radius, and height");

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Request the normal at a point on the curved surface of the cylinder. The normal should point directly away from the axis.
                    Vector normal = assertDoesNotThrow(() -> cylinder.getNormal(new Point(1, 0, 2)),
                            "getNormal should not throw an exception for a valid cylinder surface point");

                    assertEquals(new Vector(1, 0, 0), normal,
                            "Cylinder normal is incorrect for a point on the curved surface");
                    assertEquals(1, normal.length(), DELTA,
                            "Cylinder normal must be normalized");
                });
    }
}
