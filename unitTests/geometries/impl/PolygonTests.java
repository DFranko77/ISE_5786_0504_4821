package geometries.impl;

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
    private static final Point P1 = new Point(0, 0, 1);
    private static final Point P2 = new Point(1, 0, 0);
    private static final Point P3 = new Point(0, 1, 0);
    private static final Point P4 = new Point(-1, 1, 1);
    private static final Vector V1 = Vector.AXIS_Z;
    /** Test method for {@link geometries.impl.Polygon(primitives.Point...)}. */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Correct concave quadrangular with vertices in correct order
        assertDoesNotThrow(() -> new Polygon(P1,
                        P2,
                        P3,
                        P4),
                "Failed constructing a correct polygon");
        // TC02: Wrong vertices order
        assertThrows(IllegalArgumentException.class, //
                () -> new Polygon(P1, P3, P2, P4), //
                "Constructed a polygon with wrong order of vertices");
        // TC03: Not in the same plane
        assertThrows(IllegalArgumentException.class, //
                () -> new Polygon(P1, P2, P3, new Point(0, 2, 2)), //
                "Constructed a polygon with vertices that are not in the same plane");
        // TC04: Concave quadrangular
        assertThrows(IllegalArgumentException.class, //
                () -> new Polygon(P1, P2, P3,
                        new Point(0.5, 0.25, 0.5)), //
                "Constructed a concave polygon");
        // =============== Boundary Values Tests ==================
        // TC10: Vertex on a side of a quadrangular
        assertThrows(IllegalArgumentException.class, //
                () -> new Polygon(P1, P2, P3,
                        new Point(0, 0.5, 0.5)),
                "Constructed a polygon with vertix on a side");
        // TC11: Last point = first point
        assertThrows(IllegalArgumentException.class, //
                () -> new Polygon(P1, P2, P3, P1),
                "Constructed a polygon with vertice on a side");
        // TC12: Co-located points
        assertThrows(IllegalArgumentException.class, //
                () -> new Polygon(P1, P2, P3, P3),
                "Constructed a polygon with vertice on a side");
    }
    /** Test method for {@link geometries.impl.Polygon#getNormal(Point)}. */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: There is a simple single test here - using a quad
        Point[] pts =
                { P1, P2, P3, P4 };
        Polygon pol = new Polygon(pts);
        // ensure there are no exceptions
        assertDoesNotThrow(() -> pol.getNormal(P1), "");
        // generate the test result
        Vector result = pol.getNormal(P1);
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
        Polygon pol        = new Polygon(P1, new Point(2, 0, 1), new Point(2, 2, 1), new Point(0, 2, 1));
        Plane   pl         = new Plane(P1, new Point(1, 0, 1), new Point(0, 1, 1));
        Ray ray;
        String  errorPlane = "Wrong intersection with plane";
        String  errorBad   = "Bad intersection";
        // ============ Equivalence Partitions Tests ==============
        // TC01: Inside polygon
        ray = new Ray(new Point(1, 1, 0), V1);
        assertEquals(List.of(new Point(1, 1, 1)), pol.findIntersections(ray), errorBad);
        // TC02: Against edge
        ray = new Ray(new Point(-1, 1, 0), V1);
        assertEquals(List.of(new Point(-1, 1, 1)), pl.findIntersections(ray), errorPlane);
        assertNull(pol.findIntersections(ray), errorBad);
        // TC03: Against vertex
        ray = new Ray(new Point(-1, -1, 0), V1);
        assertEquals(List.of(new Point(-1, -1, 1)), pl.findIntersections(ray), errorPlane);
        assertNull(pol.findIntersections(ray), errorBad);
        // =============== Boundary Values Tests ==================
        // TC11: In vertex
        ray = new Ray(new Point(0, 2, 0), V1);
        assertEquals(List.of(new Point(0, 2, 1)), pl.findIntersections(ray), errorPlane);
        assertNull(pol.findIntersections(ray), errorBad);
        // TC12: On edge
        ray = new Ray(new Point(0, 1, 0), V1);
        assertEquals(List.of(new Point(0, 1, 1)), pl.findIntersections(ray), errorPlane);
        assertNull(pol.findIntersections(ray), errorBad);
        // TC13: On edge continuation
        ray = new Ray(new Point(0, 3, 0), V1);
        assertEquals(List.of(new Point(0, 3, 1)), pl.findIntersections(ray), errorPlane);
        assertNull(pol.findIntersections(ray), errorBad);
    }
}
