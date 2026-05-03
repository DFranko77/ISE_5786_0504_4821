package renderer;

import java.util.List;

import primitives.Color;
import primitives.Point;
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
    * @param intersection closest intersection point
    * @return color at the intersection point
    */
   private Color calcColor(Point intersection) {
      return _scene.ambientLight.getIntensity();
   }

   @Override
   Color traceRay(Ray ray) {
      List<Point> intersections = _scene.geometries.findIntersections(ray);
      if (intersections == null) {
         return _scene.background;
      }

      Point closestPoint = ray.findClosestPoint(intersections);
      return calcColor(closestPoint);
   }
}
