package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Triangle}.
 */
class TriangleTests {

    /**
     * Comparison tolerance for floating-point results returned by
     * {@link Triangle#getNormal(Point)}.
     */
    private static final double DELTA = 1e-6;

    /**
     * Tests {@link Triangle#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        final int epTestCases = 1;

        Triangle triangle = assertDoesNotThrow(() -> new Triangle(
                        new Point(0, 0, 0),
                        new Point(3, 0, 0),
                        new Point(0, 4, 0)),
                "Triangle construction should not throw an exception for valid vertices");

        Vector edge1 = new Point(3, 0, 0).subtract(new Point(0, 0, 0));
        Vector edge2 = new Point(0, 4, 0).subtract(new Point(0, 0, 0));

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Request the normal at a point strictly inside the triangle. The result should be a unit vector perpendicular to both triangle edges.
                    Vector normal = assertDoesNotThrow(() -> triangle.getNormal(new Point(1, 1, 0)),
                            "getNormal should not throw an exception for a point inside the triangle");

                    assertEquals(1, normal.length(), DELTA,
                            "Triangle normal must be normalized");
                    assertEquals(0d, normal.dotProduct(edge1), DELTA,
                            "Triangle normal must be perpendicular to the first triangle edge");
                    assertEquals(0d, normal.dotProduct(edge2), DELTA,
                            "Triangle normal must be perpendicular to the second triangle edge");
                });
    }
}
