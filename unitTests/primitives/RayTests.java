package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Ray}.
 */
class RayTests {

    /**
     * Comparison tolerance for floating-point results returned by
     * {@link Ray#direction()}.
     */
    private static final double DELTA = 1e-6;

    /**
     * Tests {@link Ray#Ray(Point, Vector)}.
     */
    @Test
    void testRay() {
        final int epTestCases = 1;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Create a ray with a regular origin point and a non-unit direction vector. Construction should preserve the origin and normalize the direction.
                    Point origin = new Point(1, 2, 3);
                    Vector direction = new Vector(0, 3, 4);
                    Ray ray = assertDoesNotThrow(() -> new Ray(origin, direction),
                            "Ray construction should not throw an exception for valid arguments");
                    Point resultOrigin = assertDoesNotThrow(ray::origin,
                            "Ray origin accessor should not throw an exception");
                    Vector resultDirection = assertDoesNotThrow(ray::direction,
                            "Ray direction accessor should not throw an exception");

                    assertEquals(origin, resultOrigin,
                            "Ray constructor stored an incorrect origin");
                    assertEquals(direction.normalize(), resultDirection,
                            "Ray constructor should normalize the direction");
                    assertEquals(1, resultDirection.length(), DELTA,
                            "Ray constructor must store a unit direction vector");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Create a ray with the coordinate-system origin and an already normalized direction vector. Construction should preserve both values.
                    Vector direction = Vector.AXIS_X;
                    Ray ray = assertDoesNotThrow(() -> new Ray(Point.ZERO, direction),
                            "Ray construction should not throw an exception for valid boundary arguments");
                    Point resultOrigin = assertDoesNotThrow(ray::origin,
                            "Ray origin accessor should not throw an exception");
                    Vector resultDirection = assertDoesNotThrow(ray::direction,
                            "Ray direction accessor should not throw an exception");

                    assertEquals(Point.ZERO, resultOrigin,
                            "Ray constructor should preserve the coordinate-system origin");
                    assertEquals(direction, resultDirection,
                            "Ray constructor should preserve an already normalized direction");
                });
    }

    /**
     * Tests {@link Ray#getPoint(double)}.
     */
    @Test
    void testGetPoint() {
        final int epTestCases = 2;
        final int bvTestCases = 1;

        Ray ray = new Ray(new Point(1, 2, 3), new Vector(1, 2, 2));

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Request a point in the positive direction of the ray line. The returned point should be shifted forward by t units.
                    assertEquals(new Point(2, 4, 5), ray.getPoint(3),
                            "getPoint should return the point located t units forward on the ray line");
                },
                () -> {
                    // TC EP02: Request a point in the negative direction of the ray line. The returned point should be shifted backward by |t| units.
                    assertEquals(new Point(0, 0, 1), ray.getPoint(-3),
                            "getPoint should return the point located |t| units backward on the ray line");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Request the point at zero distance from the ray origin. The origin itself should be returned.
                    assertEquals(new Point(1, 2, 3), ray.getPoint(0),
                            "getPoint with t = 0 should return the ray origin");
                });
    }
}
