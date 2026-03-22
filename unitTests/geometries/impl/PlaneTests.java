package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

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
                    Vector inputNormal = new Vector(0, 3, 4);
                    Plane plane = assertDoesNotThrow(() -> new Plane(new Point(0, 0, 1), inputNormal),
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
                                    new Point(0, 0, 1),
                                    new Point(1, 0, 0),
                                    new Point(0, 1, 0)),
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
                        new Point(0, 0, 1),
                        new Point(1, 0, 0),
                        new Point(0, 1, 0)),
                "Plane construction should not throw an exception for valid points");

        Vector edge1 = new Point(1, 0, 0).subtract(new Point(0, 0, 1));
        Vector edge2 = new Point(0, 1, 0).subtract(new Point(0, 0, 1));

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
}
