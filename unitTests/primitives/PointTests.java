package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Point}.
 */
class PointTests {

    /**
     * Comparison tolerance for floating-point results returned by
     * {@link Point#distance(Point)}.
     */
    private static final double DELTA = 1e-6;

    /**
     * Tests {@link Point#add(Vector)}.
     */
    @Test
    void testAdd() {
        final int epTestCases = 1;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Add a regular vector to a point. The result should be the translated point.
                    Point p1 = new Point(1, 0, 0);
                    Vector v1 = new Vector(1, 0, 0);
                    Point result = assertDoesNotThrow(() -> p1.add(v1),
                            "Point addition should not throw an exception");

                    assertEquals(new Point(2, 0, 0), result,
                            "Point addition result is incorrect");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Add the inverse vector so the result lands on the origin. The origin should be returned correctly.
                    Point p1 = new Point(1, 0, 0);
                    Vector v1 = new Vector(-1, 0, 0);
                    Point result = assertDoesNotThrow(() -> p1.add(v1),
                            "Point addition to the origin should not throw an exception");

                    assertEquals(Point.ZERO, result,
                            "Adding an inverse vector should move the point to the origin");
                });
    }

    /**
     * Tests {@link Point#subtract(Point)}.
     */
    @Test
    void testSubtract() {
        final int epTestCases = 1;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Subtract two different points. The result should be the vector from the second point to the first.
                    Point p1 = new Point(2, 3, 4);
                    Point p2 = new Point(1, 1, 1);
                    Vector result = assertDoesNotThrow(() -> p1.subtract(p2),
                            "Point subtraction should not throw an exception for distinct points");

                    assertEquals(new Vector(1, 2, 3), result,
                            "Point subtraction result is incorrect");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Subtract a point from itself. This should attempt to create the forbidden zero vector and throw an exception.
                    Point p1 = new Point(2, 3, 4);

                    assertThrows(IllegalArgumentException.class, () -> p1.subtract(p1),
                            "Subtracting identical points should create the forbidden zero vector");
                });
    }

    /**
     * Tests {@link Point#distanceSquared(Point)}.
     */
    @Test
    void testDistanceSquared() {
        final int epTestCases = 1;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Measure squared distance between two distinct points. The squared result should match the Pythagorean sum.
                    Point p1 = new Point(1, 2, 3);
                    Point p2 = new Point(4, 6, 3);
                    double result = assertDoesNotThrow(() -> p1.distanceSquared(p2),
                            "distanceSquared should not throw an exception for valid points");

                    assertEquals(25, result,
                            "Squared distance is incorrect");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Measure squared distance from a point to itself. The minimal distance value should be zero.
                    Point p1 = new Point(1, 2, 3);
                    double result = assertDoesNotThrow(() -> p1.distanceSquared(p1),
                            "distanceSquared should not throw an exception for identical points");

                    assertEquals(0, result,
                            "Squared distance from a point to itself must be zero");
                });
    }

    /**
     * Tests {@link Point#distance(Point)}.
     */
    @Test
    void testDistance() {
        final int epTestCases = 1;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Measure distance between two distinct points. The result should equal the Euclidean distance.
                    Point p1 = new Point(1, 2, 3);
                    Point p2 = new Point(4, 6, 3);
                    double result = assertDoesNotThrow(() -> p1.distance(p2),
                            "distance should not throw an exception for valid points");

                    assertEquals(5, result, DELTA,
                            "Distance is incorrect");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Measure distance from a point to itself. The boundary value should be exactly zero.
                    Point p1 = new Point(1, 2, 3);
                    double result = assertDoesNotThrow(() -> p1.distance(p1),
                            "distance should not throw an exception for identical points");

                    assertEquals(0, result, DELTA,
                            "Distance from a point to itself must be zero");
                });
    }
}
