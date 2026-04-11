package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Geometries}.
 */
public class GeometriesTests {

    /**
     * Tests {@link Geometries#add(geometries.api.Intersectable...)}.
     */
    @Test
    void testAdd() {
        final int epTestCases = 1;
        final int bvTestCases = 1;

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Add several geometries to an initially empty composite. The added geometries should participate in intersection calculations.
                    Geometries geometries = assertDoesNotThrow(() -> new Geometries(),
                            "Empty Geometries construction should not throw an exception");

                    assertDoesNotThrow(() -> geometries.add(
                                    new Sphere(new Point(3, 2, 2), 1d),
                                    new Sphere(new Point(7, 2, 2), 1d)),
                            "Adding valid geometries should not throw an exception");

                    List<Point> intersections = geometries.findIntersections(new Ray(new Point(1, 2, 2), Vector.AXIS_X));

                    assertNotNull(intersections,
                            "Ray intersecting added geometries should return intersection points");
                    assertEquals(4, intersections.size(),
                            "Two added spheres should contribute four intersection points in total");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Add no geometries to an empty composite. The composite should remain empty and return null for intersections.
                    Geometries geometries = assertDoesNotThrow(() -> new Geometries(),
                            "Empty Geometries construction should not throw an exception");

                    assertDoesNotThrow(() -> geometries.add(),
                            "Adding no geometries should not throw an exception");
                    assertNull(geometries.findIntersections(new Ray(new Point(1, 2, 2), Vector.AXIS_X)),
                            "Empty composite should return null when no geometries were added");
                });
    }

    /**
     * Tests {@link Geometries#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        final int epTestCases = 1;
        final int bvTestCases = 3;

        Geometries geometries = assertDoesNotThrow(() -> new Geometries(
                        new Sphere(new Point(3, 2, 2), 1d),
                        new Sphere(new Point(7, 2, 2), 1d),
                        new Sphere(new Point(11, 2, 2), 1d)),
                "Geometries construction should not throw an exception for valid intersectables");

        // ============ EP: Equivalence Partitions Tests ============
        assertAll("EP (" + epTestCases + " cases)",
                () -> {
                    // TC EP01: Ray intersects some, but not all, of the geometries. Only the total number of intersection points should be checked.
                    Ray ray = new Ray(new Point(5, 2, 2), Vector.AXIS_X);
                    List<Point> intersections = geometries.findIntersections(ray);

                    assertNotNull(intersections,
                            "Ray intersecting some geometries should return intersection points");
                    assertEquals(4, intersections.size(),
                            "Ray should intersect exactly two spheres for a total of four points");
                });

        // ============ BVA: Boundary Value Analysis Tests ============
        assertAll("BVA (" + bvTestCases + " cases)",
                () -> {
                    // TC BV01: Ray misses all geometries. The method should return null and not an empty list.
                    Ray ray = new Ray(new Point(13, 2, 2), Vector.AXIS_X);
                    assertNull(geometries.findIntersections(ray),
                            "Ray missing all geometries should return null");
                },
                () -> {
                    // TC BV02: Ray intersects exactly one geometry. Only the number of returned points should be checked.
                    Ray ray = new Ray(new Point(9, 2, 2), Vector.AXIS_X);
                    List<Point> intersections = geometries.findIntersections(ray);

                    assertNotNull(intersections,
                            "Ray intersecting one geometry should return intersection points");
                    assertEquals(2, intersections.size(),
                            "Ray should intersect exactly one sphere for a total of two points");
                },
                () -> {
                    // TC BV03: Ray intersects all geometries. Only the number of returned points should be checked.
                    Ray ray = new Ray(new Point(1, 2, 2), Vector.AXIS_X);
                    List<Point> intersections = geometries.findIntersections(ray);

                    assertNotNull(intersections,
                            "Ray intersecting all geometries should return intersection points");
                    assertEquals(6, intersections.size(),
                            "Ray should intersect all three spheres for a total of six points");
                });
    }
}
