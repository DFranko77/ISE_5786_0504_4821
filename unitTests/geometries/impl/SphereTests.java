package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

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

    /**
     * Tests {@link Sphere#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        final int epTestCases = 1;

        Sphere sphere = assertDoesNotThrow(() -> new Sphere(new Point(1, 0, 0), 1d),
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
}
