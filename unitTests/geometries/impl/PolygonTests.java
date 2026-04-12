package geometries.impl;//package geometries.impl;
//
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//import org.junit.jupiter.api.Test;
//
//import primitives.Point;
//import primitives.Vector;
//
///**
// * Unit tests for class {@link Polygon}.
// * The tests verify:
// * <ul>
// * <li>Polygon constructor validity</li>
// * <li>{@link Polygon#getNormal(Point)}</li>
// * </ul>
// * Tests follow the methodology of
// * Equivalence Partitions (EP) and Boundary Values (BVA).
// */
//class PolygonTests {
//    /**
//     * Default constructor to satisfy JavaDoc generator
//     */
//    PolygonTests() { /* to satisfy JavaDoc generator */ }
//
//    /**
//     * Vertex (1,0,0) used in polygon tests
//     */
//    private static final Point POINT_X = new Point(1, 0, 0);
//    /**
//     * Vertex (0,1,0) used in polygon tests
//     */
//    private static final Point POINT_Y = new Point(0, 1, 0);
//    /**
//     * Vertex (0,0,1) used in polygon tests
//     */
//    private static final Point POINT_Z = new Point(0, 0, 1);
//
//    /**
//     * Additional vertex used for valid polygon construction
//     */
//    private static final Point POINT1 = new Point(-1, 1, 1);
//    /**
//     * Point not in the polygon plane
//     */
//    private static final Point POINT2 = new Point(0, 2, 2);
//    /**
//     * Point that creates a concave polygon
//     */
//    private static final Point POINT3 = new Point(0.5, 0.25, 0.5);
//    /**
//     * Point located on one of the polygon edges
//     */
//    private static final Point POINT4 = new Point(0, 0.5, 0.5);
//
//    /**
//     * Delta value for accuracy when comparing double values.
//     */
//    private static final double DELTA = 1e-6;
//
//    /**
//     * Error message for wrong plane intersection
//     */
//    private static final String ERROR_PLANE = "ERROR: wrong intersection with plane";
//    /**
//     * Error message for wrong polygon intersection
//     */
//    private static final String ERROR_POLYGON = "ERROR: wrong polygon intersection";
//
//    /**
//     * Test method for {@link Polygon#Polygon(Point...)}.
//     * Verifies correct and incorrect polygon constructions.
//     */
//    @Test
//    void testConstructor() {
//
//        // ============ Equivalence Partitions Tests ==============
//
//        // TC01: Correct convex quadrilateral with vertices in correct order
//        assertDoesNotThrow(() -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT1),
//                "Failed constructing a correct polygon");
//
//        // TC02: Wrong vertices order
//        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_Y, POINT_X, POINT1),
//                "Constructed a polygon with wrong order of vertices");
//
//        // TC03: Vertices not in the same plane
//        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT2),
//                "Constructed a polygon with vertices that are not in the same plane");
//
//        // TC04: Concave quadrilateral
//        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT3),
//                "Constructed a concave polygon");
//
//        // =============== Boundary Values Tests ==================
//
//        // TC11: Vertex on a side
//        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT4),
//                "Constructed a polygon with a vertex on a side");
//
//        // TC12: Last point equals first point
//        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_Z),
//                "Constructed a polygon with duplicate first/last vertex");
//
//        // TC13: Co-located points
//        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_Y),
//                "Constructed a polygon with co-located vertices");
//    }
//
//    /**
//     * Test method for {@link Polygon#getNormal(Point)}.
//     * Verifies that the returned normal vector is unit length and orthogonal
//     * to all polygon edges.
//     */
//    @Test
//    void testGetNormal() {
//        // ============ Equivalence Partitions Tests ==============
//        Point[] pts = {POINT_Z, POINT_X, POINT_Y, POINT1};
//        Polygon polygon = new Polygon(pts);
//        // Ensure method does not throw exception
//        assertDoesNotThrow(() -> polygon.getNormal(POINT_Z), "getNormal() threw unexpected exception");
//        Vector result = polygon.getNormal(POINT_Z);
//        // Ensure |n| = 1
//        assertEquals(1, result.length(), DELTA, "Polygon normal is not a unit vector");
//        // Ensure normal is orthogonal to all edges
//        for (int i = 0; i < pts.length; ++i) {
//            Vector edge = pts[i].subtract(pts[i == 0 ? pts.length - 1 : i - 1]);
//            assertEquals(0d, result.dotProduct(edge), DELTA, "Polygon normal is not orthogonal to an edge");
//        }
//    }
//}

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dan
 */
class PolygonTests {
    /**
     * Delta value for accuracy when comparing the numbers of type 'double' in
     * assertEquals
     */
    private static final double DELTA = 0.000001;
    /** Test method for {@link geometries.impl.Polygon(primitives.Point...)}. */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Correct concave quadrangular with vertices in correct order
        assertDoesNotThrow(() -> new Polygon(new Point(0, 0, 1),
                        new Point(1, 0, 0),
                        new Point(0, 1, 0),
                        new Point(-1, 1, 1)),
                "Failed constructing a correct polygon");
        // TC02: Wrong vertices order
        assertThrows(IllegalArgumentException.class, //
                () -> new Polygon(new Point(0, 0, 1), new Point(0, 1, 0), new Point(1, 0, 0), new Point(-1, 1, 1)), //
                "Constructed a polygon with wrong order of vertices");
        // TC03: Not in the same plane
        assertThrows(IllegalArgumentException.class, //
                () -> new Polygon(new Point(0, 0, 1), new Point(1, 0, 0), new Point(0, 1, 0), new Point(0, 2, 2)), //
                "Constructed a polygon with vertices that are not in the same plane");
        // TC04: Concave quadrangular
        assertThrows(IllegalArgumentException.class, //
                () -> new Polygon(new Point(0, 0, 1), new Point(1, 0, 0), new Point(0, 1, 0),
                        new Point(0.5, 0.25, 0.5)), //
                "Constructed a concave polygon");
        // =============== Boundary Values Tests ==================
        // TC10: Vertex on a side of a quadrangular
        assertThrows(IllegalArgumentException.class, //
                () -> new Polygon(new Point(0, 0, 1), new Point(1, 0, 0), new Point(0, 1, 0),
                        new Point(0, 0.5, 0.5)),
                "Constructed a polygon with vertix on a side");
        // TC11: Last point = first point
        assertThrows(IllegalArgumentException.class, //
                () -> new Polygon(new Point(0, 0, 1), new Point(1, 0, 0), new Point(0, 1, 0), new Point(0, 0, 1)),
                "Constructed a polygon with vertice on a side");
        // TC12: Co-located points
        assertThrows(IllegalArgumentException.class, //
                () -> new Polygon(new Point(0, 0, 1), new Point(1, 0, 0), new Point(0, 1, 0), new Point(0, 1, 0)),
                "Constructed a polygon with vertice on a side");
    }
    /** Test method for {@link geometries.impl.Polygon#getNormal(Point)}. */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: There is a simple single test here - using a quad
        Point[] pts =
                { new Point(0, 0, 1), new Point(1, 0, 0), new Point(0, 1, 0), new Point(-1, 1, 1) };
        Polygon pol = new Polygon(pts);
        // ensure there are no exceptions
        assertDoesNotThrow(() -> pol.getNormal(new Point(0, 0, 1)), "");
        // generate the test result
        Vector result = pol.getNormal(new Point(0, 0, 1));
        // ensure |result| = 1
        assertEquals(1, result.length(), DELTA, "Polygon's normal is not a unit vector");
        // ensure the result is orthogonal to all the edges
        for (int i = 0; i < 3; ++i)
            assertEquals(0d, result.dotProduct(pts[i].subtract(pts[i == 0 ? 3 : i - 1])), DELTA,
                    "Polygon's normal is not orthogonal to one of the edges");
    }
    /**
     * Test method for {@link geometries.impl.Polygon#findIntersections(primitives.Ray)}.
     */
    @Test
    void testFindIntersectionsRay() {
        Polygon pol        = new Polygon(new Point(0, 0, 1), new Point(2, 0, 1), new Point(2, 2, 1), new Point(0, 2, 1));
        Plane   pl         = new Plane(new Point(0, 0, 1), new Point(1, 0, 1), new Point(0, 1, 1));
        Ray ray;
        String  errorPlane = "Wrong intersection with plane";
        String  errorBad   = "Bad intersection";
        // ============ Equivalence Partitions Tests ==============
        // TC01: Inside polygon
        ray = new Ray(new Point(1, 1, 0), new Vector(0, 0, 1));
        assertEquals(List.of(new Point(1, 1, 1)), pol.findIntersections(ray), errorBad);
        // TC02: Against edge
        ray = new Ray(new Point(-1, 1, 0), new Vector(0, 0, 1));
        assertEquals(List.of(new Point(-1, 1, 1)), pl.findIntersections(ray), errorPlane);
        assertNull(pol.findIntersections(ray), errorBad);
        // TC03: Against vertex
        ray = new Ray(new Point(-1, -1, 0), new Vector(0, 0, 1));
        assertEquals(List.of(new Point(-1, -1, 1)), pl.findIntersections(ray), errorPlane);
        assertNull(pol.findIntersections(ray), errorBad);
        // =============== Boundary Values Tests ==================
        // TC11: In vertex
        ray = new Ray(new Point(0, 2, 0), new Vector(0, 0, 1));
        assertEquals(List.of(new Point(0, 2, 1)), pl.findIntersections(ray), errorPlane);
        assertNull(pol.findIntersections(ray), errorBad);
        // TC12: On edge
        ray = new Ray(new Point(0, 1, 0), new Vector(0, 0, 1));
        assertEquals(List.of(new Point(0, 1, 1)), pl.findIntersections(ray), errorPlane);
        assertNull(pol.findIntersections(ray), errorBad);
        // TC13: On edge continuation
        ray = new Ray(new Point(0, 3, 0), new Vector(0, 0, 1));
        assertEquals(List.of(new Point(0, 3, 1)), pl.findIntersections(ray), errorPlane);
        assertNull(pol.findIntersections(ray), errorBad);
    }
}
