package renderer;

import geometries.api.Intersectable.Intersection;
import primitives.Color;
import primitives.Ray;
import scene.Scene;

/**
 * Basic ray tracer that returns scene background or ambient color.
 */
class SimpleRayTracer extends RayTracerBase {
   /**
    * Creates a simple ray tracer for the given scene.
    *
    * @param scene scene to render
    */
   SimpleRayTracer(Scene scene) {
      super(scene);
   }

   /**
    * Calculates the color for a ray-geometry intersection point.
    *
    * @param intersection closest intersection details
    * @return color at the intersection point
    */
   private Color calcColor(Intersection intersection) {
      return intersection.geometry.getEmission().add(
         _scene.ambientLight.getIntensity().scale(intersection.material.kA)
      );
   }

   @Override
   Color traceRay(Ray ray) {
      var intersections = _scene.geometries.calcIntersections(ray);
      if (intersections == null) {
         return _scene.background;
      }

      Intersection closestIntersection = ray.findClosestIntersection(intersections);
      return calcColor(closestIntersection);
   }
}
