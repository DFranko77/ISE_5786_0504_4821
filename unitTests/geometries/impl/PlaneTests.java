package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Plane}.
 */
class PlaneTests {

    /**
     * Comparison tolerance for floating-point results returned by
     * {@link Plane#getNormal(Point)} and by planes created through the constructors.
     */
    private static final double DELTA = 1e-6;
    private static final Point P1 = new Point(0, 0, 1);
    private static final Point P2 = new Point(1, 0, 0);
    private static final Point P3 = new Point(0, 1, 0);
    private static final Vector V1 = new Vector(0, 3, 4);

    /**
     * Tests {@link Plane#Plane(Point, Vector)}.
     */
    @Test
    void testConstructorPointVector() {
        final int epTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Create a plane from a point and a non-unit normal vector. The stored normal should be normalized and parallel to the input normal.
                    Vector inputNormal = V1;
                    Plane plane = assertDoesNotThrow(() -> new Plane(P1, inputNormal),
                            "Plane construction should not throw an exception for valid point-normal data");
                    Vector normal = assertDoesNotThrow(() -> plane.getNormal(new Point(1, 0, 1)),
                            "getNormal should not throw an exception for a valid plane point");

                    assertEquals(1, normal.length(), DELTA,
                            "Plane normal must be normalized");
                    assertThrows(IllegalArgumentException.class, () -> normal.crossProduct(inputNormal),
                            "Plane normal should remain parallel to the constructor normal");
                });
    }

    /**
     * Tests {@link Plane#Plane(Point, Point, Point)}.
     */
    @Test
    void testConstructorThreePoints() {
        final int epTestCases = 1;
        final int bvTestCases = 4;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Create a plane from three distinct non-collinear points. Construction should succeed for a valid plane definition.
                    assertDoesNotThrow(() -> new Plane(
                                    P1,
                                    P2,
                                    P3),
                            "Plane construction should succeed for three distinct non-collinear points");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Use the first and second points as the same point. Construction should fail because two points coincide.
                    assertThrows(IllegalArgumentException.class, () -> new Plane(
                                    new Point(0, 0, 1),
                                    new Point(0, 0, 1),
                                    new Point(0, 1, 0)),
                            "Plane construction should fail when the first and second points coincide");
                },
                () -> {
                    // TC BV02: Use the first and third points as the same point. Construction should fail because two points coincide.
                    assertThrows(IllegalArgumentException.class, () -> new Plane(
                                    new Point(0, 0, 1),
                                    new Point(1, 0, 0),
                                    new Point(0, 0, 1)),
                            "Plane construction should fail when the first and third points coincide");
                },
                () -> {
                    // TC BV03: Use all three points as the same point. Construction should fail because no plane can be defined.
                    assertThrows(IllegalArgumentException.class, () -> new Plane(
                                    new Point(1, 1, 1),
                                    new Point(1, 1, 1),
                                    new Point(1, 1, 1)),
                            "Plane construction should fail when all three points coincide");
                },
                () -> {
                    // TC BV04: Use three distinct points on the same line. Construction should fail because the points do not span a plane.
                    assertThrows(IllegalArgumentException.class, () -> new Plane(
                                    new Point(0, 0, 0),
                                    new Point(1, 1, 1),
                                    new Point(2, 2, 2)),
                            "Plane construction should fail for three collinear points");
                });
    }

    /**
     * Tests {@link Plane#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        final int epTestCases = 1;
        final int bvTestCases = 1;

        Plane plane = assertDoesNotThrow(() -> new Plane(
                        P1,
                        P2,
                        P3),
                "Plane construction should not throw an exception for valid points");

        Vector edge1 = P2.subtract(P1);
        Vector edge2 = P3.subtract(P1);

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Request the normal at a plane point that is not a reference point. The result should be a unit vector perpendicular to the plane.
                    Vector normal = assertDoesNotThrow(() -> plane.getNormal(new Point(1, 1, -1)),
                            "getNormal should not throw an exception for a valid plane point");

                    assertEquals(1, normal.length(), DELTA,
                            "Plane normal must be normalized");
                    assertEquals(0d, normal.dotProduct(edge1), DELTA,
                            "Plane normal must be perpendicular to the first in-plane vector");
                    assertEquals(0d, normal.dotProduct(edge2), DELTA,
                            "Plane normal must be perpendicular to the second in-plane vector");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Request the normal at a reference point of the plane. The same unit normal should be returned.
                    Vector normal = assertDoesNotThrow(() -> plane.getNormal(new Point(0, 0, 1)),
                            "getNormal should not throw an exception at a plane reference point");

                    assertEquals(1, normal.length(), DELTA,
                            "Plane normal must be normalized at a reference point");
                    assertEquals(0d, normal.dotProduct(edge1), DELTA,
                            "Plane normal must remain perpendicular to the first in-plane vector");
                    assertEquals(0d, normal.dotProduct(edge2), DELTA,
                            "Plane normal must remain perpendicular to the second in-plane vector");
                });
    }

    /**
     * Tests {@link Plane#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        final int epTestCases = 2;
        final int bvTestCases = 5;

         Plane plane = new Plane(P1, Vector.AXIS_Z);

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Ray is neither parallel nor orthogonal to the plane and intersects it in front of the origin.
                    Ray ray = new Ray(new Point(0, 0, 0), new Vector(1, 0, 1));
                    assertEquals(List.of(new Point(1, 0, 1)), plane.findIntersections(ray),
                            "Plane should return the single forward intersection point");
                },
                () -> {
                    // TC EP02: Ray points away from the plane, so the mathematical intersection lies behind the ray origin.
                    Ray ray = new Ray(new Point(0, 0, 2), new Vector(1, 0, 1));
                    assertNull(plane.findIntersections(ray),
                            "Plane should not return intersections behind the ray origin");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Ray is parallel to the plane and does not lie in it.
                    Ray ray = new Ray(new Point(0, 0, 0), Vector.AXIS_X);
                    assertNull(plane.findIntersections(ray),
                            "Parallel ray outside the plane should not intersect it");
                },
                () -> {
                    // TC BV02: Ray lies entirely in the plane.
                    Ray ray = new Ray(new Point(0, 0, 1), Vector.AXIS_X);
                    assertNull(plane.findIntersections(ray),
                            "Ray contained in the plane should not report intersections");
                },
                () -> {
                    // TC BV03: Ray is orthogonal to the plane and starts before it.
                    Ray ray = new Ray(new Point(0, 0, 0), Vector.AXIS_Z);
                    assertEquals(List.of(new Point(0, 0, 1)), plane.findIntersections(ray),
                            "Orthogonal ray should intersect the plane once when starting before it");
                },
                () -> {
                    // TC BV04: Ray is orthogonal to the plane and starts on it.
                    Ray ray = new Ray(new Point(0, 0, 1), Vector.AXIS_Z);
                    assertNull(plane.findIntersections(ray),
                            "Ray starting on the plane should not report its origin as an intersection");
                },
                () -> {
                    // TC BV05: Ray is orthogonal to the plane and starts after it.
                    Ray ray = new Ray(new Point(0, 0, 2), Vector.AXIS_Z);
                    assertNull(plane.findIntersections(ray),
                            "Orthogonal ray starting beyond the plane should not intersect it");
                });
    }
}
