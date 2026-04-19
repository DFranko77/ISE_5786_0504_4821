package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

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
    private static final Point P1 = new Point(0, 0, 0);
    private static final Point P2 = new Point(3, 0, 0);
    private static final Point P3 = new Point(0, 4, 0);
    private static final Vector V1 = new Vector(0, 0, -1);

    /**
     * Tests {@link Triangle#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        final int epTestCases = 1;

        Triangle triangle = assertDoesNotThrow(() -> new Triangle(
                        P1,
                        P2,
                        P3),
                "Triangle construction should not throw an exception for valid vertices");

        Vector edge1 = P2.subtract(P1);
        Vector edge2 = P3.subtract(P1);

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

    /**
     * Tests {@link Triangle#findIntersections(Ray)}.
     */
    @Test
    public void testFindIntersections() {
        Triangle triangle = new Triangle(new Point(0, 0, 1), new Point(1, 0, 0), new Point(0, 1, 0));

        // ============ EP: Equivalence Partitions Tests ============

        // TC01: Inside triangle
        List<Point> result = triangle.findIntersections(new Ray(new Point(0.2, 0.2, 2), V1));
        assertEquals(1, result.size(), "Wrong number of points");
        assertEquals(new Point(0.2, 0.2, 0.6), result.get(0), "Ray crosses triangle");

        // TC02: Outside against edge
        assertNull(triangle.findIntersections(new Ray(new Point(0, -1, 2), V1)), "Ray's line out of triangle");

        // TC03: Outside against vertex
        assertNull(triangle.findIntersections(new Ray(new Point(-1, -1, 2), V1)), "Ray's line out of triangle");

        // ============ BVA: Boundary Value Analysis Tests ============

        // TC11: On edge
        assertNull(triangle.findIntersections(new Ray(new Point(0, 0.5, 2), V1)), "Ray's line on edge");

        // TC12: In vertex
        assertNull(triangle.findIntersections(new Ray(new Point(1, 0, 2), V1)), "Ray's line on vertex");

        // TC13: On edge's continuation
        assertNull(triangle.findIntersections(new Ray(new Point(0, 2, 2), V1)), "Ray's line on edge continuation");

    }
}
