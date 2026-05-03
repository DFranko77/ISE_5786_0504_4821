package primitives;

import org.junit.jupiter.api.Test;

import java.util.List;

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
    private static final Point P1 = new Point(1, 2, 3);
    private static final Vector V1 = new Vector(0, 3, 4);
    private static final Vector V2 = new Vector(1, 2, 2);

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
                    Point origin = P1;
                    Vector direction = V1;
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

        Ray ray = new Ray(P1, V2);

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
                    assertEquals(P1, ray.getPoint(0),
                            "getPoint with t = 0 should return the ray origin");
                });
    }

    /**
     * Tests {@link Ray#findClosestPoint(List)}.
     */
    @Test
    void testFindClosestPoint() {
        final int epTestCases = 1;
        final int bvTestCases = 3;

        Ray ray = new Ray(Point.ZERO, Vector.AXIS_X);

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: A list with at least three points where the middle point is closest to the ray head.
                    List<Point> points = List.of(new Point(5, 0, 0), new Point(1, 0, 0), new Point(7, 0, 0));
                    assertEquals(new Point(1, 0, 0), ray.findClosestPoint(points),
                            "findClosestPoint should return the middle list point when it is the closest to the ray head");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Null list should return null.
                    assertNull(ray.findClosestPoint(null),
                            "findClosestPoint should return null for a null list");
                },
                () -> {
                    // TC BV02: A list with at least three points where the first point is closest to the ray head.
                    List<Point> points = List.of(new Point(1, 0, 0), new Point(5, 0, 0), new Point(7, 0, 0));
                    assertEquals(new Point(1, 0, 0), ray.findClosestPoint(points),
                            "findClosestPoint should return the first list point when it is the closest to the ray head");
                },
                () -> {
                    // TC BV03: A list with at least three points where the last point is closest to the ray head.
                    List<Point> points = List.of(new Point(5, 0, 0), new Point(7, 0, 0), new Point(1, 0, 0));
                    assertEquals(new Point(1, 0, 0), ray.findClosestPoint(points),
                            "findClosestPoint should return the last list point when it is the closest to the ray head");
                });
    }
}
