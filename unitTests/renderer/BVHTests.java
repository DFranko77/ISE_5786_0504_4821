package renderer;

import org.junit.jupiter.api.Test;

import geometries.api.Intersectable;
import geometries.api.Intersectable.Intersection;
import geometries.impl.Geometries;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.PointLight;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the automatic Bounding Volume Hierarchy (BVH) acceleration.
 *
 * <p>Unlike soft shadows, the BVH does not change the rendered image — it only
 * makes the render faster by letting a ray skip whole groups of geometry whose
 * bounding box it misses. So the important test is not visual but a correctness
 * guarantee: {@link #bvhMatchesBruteForce()} fires a dense grid of rays at a
 * scene and asserts the BVH finds the <em>exact same</em> intersections as the
 * plain brute-force container.</p>
 *
 * <p>{@link #bvhSpeedup()} is the soft-shadow-style companion: it renders a
 * sphere-packed scene once with the BVH off and once with it on, prints both
 * render times so you can see the speedup, and writes {@code images/bvhTest.png}
 * (identical either way).</p>
 */
class BVHTests {

   /** Default constructor to satisfy the JavaDoc generator. */
   BVHTests() { /* to satisfy JavaDoc generator */ }

   /**
    * Asymmetric reference point used only to break ties when two intersection
    * points are the same distance from the ray origin, so the sorted comparison
    * is order-independent and never flaky.
    */
   private static final Point TIE_BREAK_REF = new Point(1009, 2017, 3023);

   /**
    * Builds a fresh list of geometries: a 5x5x5 lattice of small spheres plus an
    * infinite floor plane and a triangle. The plane and triangle make sure the
    * test also covers the unbounded path (the plane has no finite box and is
    * always tested) and a flat, axis-aligned box (the triangle), not just spheres.
    *
    * @return a new list of geometries (fresh instances each call)
    */
   private static List<Intersectable> buildGeometries() {
      List<Intersectable> geometries = new ArrayList<>();
      for (int x = -2; x <= 2; x++)
         for (int y = -2; y <= 2; y++)
            for (int z = -2; z <= 2; z++)
               geometries.add(new Sphere(new Point(x * 30, y * 30, -200 + z * 30), 10d));

      geometries.add(new Plane(new Point(0, -120, 0), new Vector(0, 1, 0)));
      geometries.add(new Triangle(
         new Point(-50, 50, -150), new Point(50, 50, -150), new Point(0, 90, -150)));
      return geometries;
   }

   /**
    * Returns the intersection points of a ray, sorted by distance from the ray
    * origin, so two result lists can be compared regardless of the order in which
    * each container happened to visit its geometries.
    *
    * @param container container to query
    * @param ray       ray to test
    * @return the intersection points, ordered near-to-far (empty list if none)
    */
   private static List<Point> sortedHits(Intersectable container, Ray ray) {
      List<Intersection> hits = container.calcIntersections(ray);
      if (hits == null) return List.of();
      return hits.stream()
         .map(hit -> hit.point)
         .sorted(Comparator
            .comparingDouble((Point p) -> p.distanceSquared(ray.origin()))
            .thenComparingDouble(p -> p.distanceSquared(TIE_BREAK_REF)))
         .toList();
   }

   /**
    * Correctness guard: the BVH must return exactly the same intersections as the
    * brute-force container for every ray.
    *
    * <p>The same geometry instances are placed in two containers; one is left flat
    * and the other is reorganized with {@link Geometries#buildBVH()}. A dense grid
    * of rays (covering hits on the spheres, the plane and the triangle, as well as
    * complete misses) is fired through both, and each ray's intersection points
    * are required to match.</p>
    */
   @Test
   void bvhMatchesBruteForce() {
      List<Intersectable> geometries = buildGeometries();
      Geometries bruteForce = new Geometries(geometries);
      Geometries bvh = new Geometries(geometries);
      bvh.buildBVH();

      Point eye = new Point(0, 0, 100);
      // Fan a 60x60 grid of rays from the eye through the z = -200 plane, spanning
      // the whole lattice (and beyond it, so plenty of rays miss everything).
      for (int i = 0; i < 60; i++) {
         for (int j = 0; j < 60; j++) {
            double tx = -120 + i * 240d / 59;
            double ty = -120 + j * 240d / 59;
            Ray ray = new Ray(eye, new Point(tx, ty, -200).subtract(eye));

            assertEquals(sortedHits(bruteForce, ray), sortedHits(bvh, ray),
               "BVH and brute force disagree for ray " + ray);
         }
      }
   }

   // ----------------------------------------------------------------------
   //  Soft-shadow-style demo: renders the same scene with the BVH off then on,
   //  prints both timings, and writes images/bvhTest.png (identical either way).
   // ----------------------------------------------------------------------

   /**
    * Renders a sphere-packed scene twice — once brute force, once with the BVH —
    * and prints how long each took, so the speedup is visible. The two renders
    * produce the same {@code images/bvhTest.png}; only the time differs.
    */
   @Test
   void bvhSpeedup() {
      System.out.printf("Brute force: %d ms%n", renderOnce(false));
      System.out.printf("With BVH:    %d ms%n", renderOnce(true));
   }

   /**
    * Builds and renders the demo scene once.
    *
    * @param useBvh whether to reorganize the scene into a BVH before rendering
    * @return wall-clock render time in milliseconds
    */
   private static long renderOnce(boolean useBvh) {
      Scene scene = new Scene("BVH test")
         .setBackground(new Color(40, 45, 55))
         .setAmbientLight(new AmbientLight(new Color(20, 20, 24)));

      // A thick wall of spheres: enough geometry that skipping most of it per ray
      // makes the BVH's advantage obvious.
      for (int x = -6; x <= 6; x++)
         for (int y = -6; y <= 6; y++)
            scene.geometries.add(new Sphere(new Point(x * 16, y * 16, -150), 7d)
               .setEmission(new Color(120, 40, 40))
               .setMaterial(new Material().setKD(0.5).setKS(0.5).setShininess(80)));

      scene.lights.add(new PointLight(new Color(700, 700, 700), new Point(-120, 140, 120))
         .setKl(2E-4).setKq(1E-6));

      if (useBvh) scene.geometries.buildBVH();

      long start = System.nanoTime();
      Camera.getBuilder()
         .setLocation(new Point(0, 0, 120))
         .setVpDistance(120)
         .setVpSize(240, 240)
         .setDirection(new Point(0, 0, -150), Vector.AXIS_Y)
         .setResolution(500, 500)
         .setRayTracer(scene, RayTracerType.SIMPLE)
         .build()
         .renderImage()
         .writeToImage("bvhTest");
      return (System.nanoTime() - start) / 1_000_000;
   }
}
