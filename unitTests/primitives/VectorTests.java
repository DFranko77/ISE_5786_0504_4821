package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Vector}.
 */
class VectorTests {

    /**
     * Comparison tolerance for floating-point results returned by
     * {@link Vector#crossProduct(Vector)}, {@link Vector#length()}, and
     * {@link Vector#normalize()}.
     */
    private static final double DELTA = 1e-6;

    /**
     * Tests {@link Vector#Vector(Double3)}.
     */
    @Test
    void testVectorDouble3() {
        final int epTestCases = 1;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Construct a vector from a non-zero coordinate triad. Construction should succeed and preserve the coordinates.
                    Double3 xyz = new Double3(1, 2, 3);

                    Vector result = assertDoesNotThrow(() -> new Vector(xyz),
                            "Vector(Double3) should not throw an exception for a non-zero triad");

                    assertEquals(new Vector(1, 2, 3), result,
                            "Vector(Double3) stored incorrect coordinates");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Construct a vector from the zero triad. Construction should fail because the zero vector is forbidden.
                    assertThrows(IllegalArgumentException.class, () -> new Vector(Double3.ZERO),
                            "Vector(Double3) should reject the zero triad");
                });
    }

    /**
     * Tests {@link Vector#Vector(double, double, double)}.
     */
    @Test
    void testVector() {
        final int epTestCases = 1;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Construct a vector from non-zero numeric coordinates. Construction should succeed and preserve the coordinates.
                    Vector result = assertDoesNotThrow(() -> new Vector(1, 2, 3),
                            "Vector(double, double, double) should not throw an exception for non-zero coordinates");

                    assertEquals(new Vector(1, 2, 3), result,
                            "Vector(double, double, double) stored incorrect coordinates");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Construct a vector from zero coordinates. Construction should fail because the zero vector is forbidden.
                    assertThrows(IllegalArgumentException.class, () -> new Vector(0, 0, 0),
                            "Vector(double, double, double) should reject zero coordinates");
                });
    }

    /**
     * Tests {@link Vector#add(Vector)}.
     */
    @Test
    void testAdd() {
        final int epTestCases = 1;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Add two non-opposite vectors. The result should be their vector sum.
                    Vector v1 = new Vector(1, 2, 3);
                    Vector v2 = new Vector(4, -1, 2);
                    Vector result = assertDoesNotThrow(() -> v1.add(v2),
                            "Vector addition should not throw an exception for non-opposite vectors");

                    assertEquals(new Vector(5, 1, 5), result,
                            "Vector addition result is incorrect");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Add opposite vectors. This should attempt to create the forbidden zero vector and throw an exception.
                    Vector v1 = new Vector(1, 0, 0);
                    Vector v2 = new Vector(-1, 0, 0);

                    assertThrows(IllegalArgumentException.class, () -> v1.add(v2),
                            "Adding opposite vectors should create the forbidden zero vector");
                });
    }

    /**
     * Tests the inherited {@link Point#subtract(Point)} behavior on {@link Vector} instances.
     */
    @Test
    void testSubtract() {
        final int epTestCases = 1;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Subtract two different vectors treated as points. The result should be the vector difference between their coordinates.
                    Vector v1 = new Vector(2, 3, 4);
                    Vector v2 = new Vector(1, 1, 1);
                    Vector result = assertDoesNotThrow(() -> v1.subtract(v2),
                            "Vector subtraction should not throw an exception for distinct vectors");

                    assertEquals(new Vector(1, 2, 3), result,
                            "Vector subtraction result is incorrect");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Subtract a vector from itself. This should attempt to create the forbidden zero vector and throw an exception.
                    Vector v1 = new Vector(2, 3, 4);

                    assertThrows(IllegalArgumentException.class, () -> v1.subtract(v1),
                            "Subtracting identical vectors should create the forbidden zero vector");
                });
    }

    /**
     * Tests {@link Vector#scale(double)}.
     */
    @Test
    void testScale() {
        final int epTestCases = 2;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Scale a vector by a positive factor. Each coordinate should be multiplied by that factor.
                    Vector v = new Vector(1, -2, 3);
                    Vector result = assertDoesNotThrow(() -> v.scale(2),
                            "Scaling by a positive factor should not throw an exception");

                    assertEquals(new Vector(2, -4, 6), result,
                            "Scaling by a positive factor is incorrect");
                },
                () -> {
                    // TC EP02: Scale a vector by a negative factor. The result should flip direction and scale magnitude.
                    Vector v = new Vector(1, -2, 3);
                    Vector result = assertDoesNotThrow(() -> v.scale(-1),
                            "Scaling by a negative factor should not throw an exception");

                    assertEquals(new Vector(-1, 2, -3), result,
                            "Scaling by a negative factor is incorrect");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Scale a vector by zero. This should attempt to create the forbidden zero vector and throw an exception.
                    Vector v = new Vector(1, -2, 3);

                    assertThrows(IllegalArgumentException.class, () -> v.scale(0),
                            "Scaling by zero should create the zero vector");
                });
    }

    /**
     * Tests {@link Vector#dotProduct(Vector)}.
     */
    @Test
    void testDotProduct() {
        final int epTestCases = 1;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Compute the dot product of two non-orthogonal vectors. The result should match the algebraic sum.
                    Vector v1 = new Vector(1, 2, 3);
                    Vector v2 = new Vector(-2, -4, -6);
                    double result = assertDoesNotThrow(() -> v1.dotProduct(v2),
                            "dotProduct should not throw an exception for valid vectors");

                    assertEquals(-28, result,
                            "Dot product result is incorrect");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Compute the dot product of orthogonal vectors. The boundary result should be zero.
                    Vector v1 = new Vector(1, 2, 3);
                    Vector v3 = new Vector(0, 3, -2);
                    double result = assertDoesNotThrow(() -> v1.dotProduct(v3),
                            "dotProduct should not throw an exception for orthogonal vectors");

                    assertEquals(0d, result, DELTA,
                            "Dot product of orthogonal vectors must be zero");
                });
    }

    /**
     * Tests {@link Vector#crossProduct(Vector)}.
     */
    @Test
    void testCrossProduct() {
        final int epTestCases = 3;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Cross two non-parallel vectors. The result should be orthogonal to the first operand.
                    Vector v1 = new Vector(1, 2, 3);
                    Vector v2 = new Vector(0, 3, -2);
                    Vector vr = assertDoesNotThrow(() -> v1.crossProduct(v2),
                            "crossProduct should not throw an exception for non-parallel vectors");

                    assertEquals(0d, vr.dotProduct(v1), DELTA,
                            "Cross product result must be orthogonal to the first operand");
                },
                () -> {
                    // TC EP02: Cross two non-parallel vectors. The result should also be orthogonal to the second operand.
                    Vector v1 = new Vector(1, 2, 3);
                    Vector v2 = new Vector(0, 3, -2);
                    Vector vr = assertDoesNotThrow(() -> v1.crossProduct(v2),
                            "crossProduct should not throw an exception for non-parallel vectors");

                    assertEquals(0d, vr.dotProduct(v2), DELTA,
                            "Cross product result must be orthogonal to the second operand");
                },
                () -> {
                    // TC EP03: Cross two non-parallel vectors. The result magnitude should equal the product of operand lengths here.
                    Vector v1 = new Vector(1, 2, 3);
                    Vector v2 = new Vector(0, 3, -2);
                    Vector vr = assertDoesNotThrow(() -> v1.crossProduct(v2),
                            "crossProduct should not throw an exception for non-parallel vectors");

                    assertEquals(v1.length() * v2.length(), vr.length(), DELTA,
                            "Cross product result has an incorrect magnitude");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Cross parallel vectors. This should attempt to create the forbidden zero vector and throw an exception.
                    Vector v1 = new Vector(1, 2, 3);

                    assertThrows(IllegalArgumentException.class,
                            () -> v1.crossProduct(new Vector(-2, -4, -6)),
                            "Cross product of parallel vectors should throw an exception");
                });
    }

    /**
     * Tests {@link Vector#lengthSquared()}.
     */
    @Test
    void testLengthSquared() {
        final int epTestCases = 1;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Compute squared length for a regular vector. The result should equal the sum of squared coordinates.
                    Vector v = new Vector(1, 2, 3);
                    double result = assertDoesNotThrow(v::lengthSquared,
                            "lengthSquared should not throw an exception for a valid vector");

                    assertEquals(14, result,
                            "Squared length is incorrect");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Compute squared length for a unit axis vector. The minimal non-zero squared length should be one.
                    Vector v = new Vector(1, 0, 0);
                    double result = assertDoesNotThrow(v::lengthSquared,
                            "lengthSquared should not throw an exception for a unit vector");

                    assertEquals(1, result,
                            "Squared length of a unit vector must be one");
                });
    }

    /**
     * Tests {@link Vector#length()}.
     */
    @Test
    void testLength() {
        final int epTestCases = 1;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Compute the length of a regular vector. The result should equal the Euclidean norm.
                    Vector v = new Vector(0, 3, 4);
                    double result = assertDoesNotThrow(v::length,
                            "length should not throw an exception for a valid vector");

                    assertEquals(5, result, DELTA,
                            "Vector length is incorrect");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Compute length for a unit axis vector. The minimal non-zero vector length should be one.
                    Vector v = new Vector(1, 0, 0);
                    double result = assertDoesNotThrow(v::length,
                            "length should not throw an exception for a unit vector");

                    assertEquals(1, result, DELTA,
                            "Length of a unit vector must be one");
                });
    }

    /**
     * Tests {@link Vector#normalize()}.
     */
    @Test
    void testNormalize() {
        final int epTestCases = 2;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Normalize a regular vector. The method should return a new vector instance.
                    Vector v = new Vector(1, 2, 3);
                    Vector normalized = assertDoesNotThrow(v::normalize,
                            "normalize should not throw an exception for a valid vector");

                    assertNotSame(v, normalized,
                            "Normalize should create a new vector");
                },
                () -> {
                    // TC EP02: Normalize a regular vector. The result should have unit length.
                    Vector v = new Vector(1, 2, 3);
                    Vector normalized = assertDoesNotThrow(v::normalize,
                            "normalize should not throw an exception for a valid vector");

                    assertEquals(1, normalized.length(), DELTA,
                            "Normalized vector must have unit length");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Normalize a vector and compare direction to the original. The normalized result should remain parallel.
                    Vector v = new Vector(1, 2, 3);
                    Vector normalized = assertDoesNotThrow(v::normalize,
                            "normalize should not throw an exception for a valid vector");

                    assertThrows(IllegalArgumentException.class, () -> v.crossProduct(normalized),
                            "Normalized vector should remain parallel to the original vector");
                });
    }
}
