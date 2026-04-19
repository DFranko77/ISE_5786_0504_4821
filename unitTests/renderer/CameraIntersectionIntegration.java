package renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import geometries.api.Intersectable;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Integration tests for camera ray generation against geometry intersections.
 */
class CameraIntersectionIntegration {
   /** Shared integration-test resolution along the horizontal axis. */
   private static final int    NX = 3;
   /** Shared integration-test resolution along the vertical axis. */
   private static final int    NY = 3;
   /** Shared camera forward direction. */
   private static final Vector V_TO = new Vector(0, 0, -1);
   /** Shared camera up direction. */
   private static final Vector V_UP = Vector.AXIS_Y;

   /**
    * Builds a camera for the 3x3 integration scenarios.
    *
    * @param location camera location
    * @return camera configured with the shared 3x3 view plane
    */
   private Camera createCamera(Point location) {
      return Camera.getBuilder()
         .setLocation(location)
         .setDirection(V_TO, V_UP)
         .setVpDistance(1)
         .setVpSize(3, 3)
         .setResolution(NX, NY)
         .build();
   }

   /**
    * Integration tests for camera rays with spheres.
    */
   @Test
   void testCameraRaySphereIntegration() {
      Camera cameraAtOrigin = createCamera(Point.ZERO);
      Camera cameraForward  = createCamera(new Point(0, 0, 0.5));

      assertIntersectionsCount(
         cameraAtOrigin,
         new Sphere(new Point(0, 0, -3), 1),
         2,
         "Sphere TC01: small sphere in front of the camera should intersect 2 rays");

      assertIntersectionsCount(
         cameraForward,
         new Sphere(new Point(0, 0, -2.5), 2.5),
         18,
         "Sphere TC02: large sphere should produce 18 intersections");

      assertIntersectionsCount(
         cameraForward,
         new Sphere(new Point(0, 0, -2), 2),
         10,
         "Sphere TC03: medium sphere should produce 10 intersections");

      assertIntersectionsCount(
         cameraForward,
         new Sphere(new Point(0, 0, -1), 4),
         9,
         "Sphere TC04: camera inside sphere should produce 9 intersections");

      assertIntersectionsCount(
         cameraAtOrigin,
         new Sphere(new Point(0, 0, 1), 0.5),
         0,
         "Sphere TC05: sphere behind camera should produce no intersections");
   }

   /**
    * Integration tests for camera rays with planes.
    */
   @Test
   void testCameraRayPlaneIntegration() {
      Camera camera = createCamera(Point.ZERO);

      assertIntersectionsCount(
         camera,
         new Plane(new Point(0, 0, -5), new Vector(0, 0, 1)),
         9,
         "Plane TC01: plane parallel to the view plane should intersect all 9 rays");

      assertIntersectionsCount(
         camera,
         new Plane(new Point(0, 0, -5), new Vector(0, -1, 2)),
         9,
         "Plane TC02: moderately tilted plane should intersect all 9 rays");

      assertIntersectionsCount(
         camera,
         new Plane(new Point(0, 0, -5), new Vector(0, -2, 1)),
         6,
         "Plane TC03: steeply tilted plane should intersect only 6 rays");
   }

   /**
    * Integration tests for camera rays with triangles.
    */
   @Test
   void testCameraRayTriangleIntegration() {
      Camera camera = createCamera(Point.ZERO);

      assertIntersectionsCount(
         camera,
         new Triangle(new Point(0, 1, -2), new Point(1, -1, -2), new Point(-1, -1, -2)),
         1,
         "Triangle TC01: narrow triangle should intersect exactly 1 ray");

      assertIntersectionsCount(
         camera,
         new Triangle(new Point(0, 20, -2), new Point(1, -1, -2), new Point(-1, -1, -2)),
         2,
         "Triangle TC02: taller triangle should intersect exactly 2 rays");
   }

   /**
    * Counts all intersections between camera rays and a geometry across the full view
    * plane and asserts the expected total.
    *
    * @param camera                camera generating the rays
    * @param body                  intersectable body under test
    * @param expectedIntersections expected total number of intersections
    * @param testName              failure message
    */
   private void assertIntersectionsCount(Camera camera, Intersectable body, int expectedIntersections, String testName) {
      int intersectionsCount = 0;

      for (int yIndex = 0; yIndex < NY; yIndex++) {
         for (int xIndex = 0; xIndex < NX; xIndex++) {
            Ray ray = camera.constructRay(xIndex, yIndex);
            List<Point> intersections = body.findIntersections(ray);
            if (intersections != null) {
               intersectionsCount += intersections.size();
            }
         }
      }

      assertEquals(expectedIntersections, intersectionsCount, testName);
   }
}
