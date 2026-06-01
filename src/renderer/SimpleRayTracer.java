package renderer;

import geometries.api.Intersectable.Intersection;
import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

/**
 * Basic ray tracer that returns scene background or ambient color.
 */
class SimpleRayTracer extends RayTracerBase {
   private static final double DELTA = 0.1;

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
   private Color calcColor(Intersection intersection, Vector v) {
      return !preprocessIntersection(intersection, v)
         ? Color.BLACK
         : _scene.ambientLight.getIntensity().scale(intersection.material.kA)
            .add(calcColorLocalEffects(intersection));
   }

   /**
    * Calculates local lighting effects from all scene light sources.
    *
    * @param intersection intersection shading context
    * @return accumulated local light color
    */
   private Color calcColorLocalEffects(Intersection intersection) {
      Color color = intersection.geometry.getEmission();
      for (var lightSource : _scene.lights) {
         if (preprocessLightSource(intersection, lightSource) && unshaded(intersection)) {
            Double3 lightFactor = calcDiffuse(intersection).add(calcSpecular(intersection));
            color = color.add(lightSource.getIntensity(intersection.point).scale(lightFactor));
         }
      }
      return color;
   }

   /**
    * Checks whether the intersection point is unshaded for its active light source.
    *
    * @param intersection intersection shading context
    * @return {@code true} if no geometry blocks the path to the light
    */
   private boolean unshaded(Intersection intersection) {
      Vector shift = intersection.normal.scale(intersection.vNormal < 0 ? DELTA : -DELTA);
      Point shiftedHead = intersection.point.add(shift);
      Ray shadowRay = new Ray(shiftedHead, intersection.l.scale(-1d));
      var shadowIntersections = _scene.geometries.calcIntersections(shadowRay);
      if (shadowIntersections == null) return true;
      double lightDistance = intersection.light.getDistance(intersection.point);
      return shadowIntersections.stream()
         .noneMatch(si -> si.point.distance(shiftedHead) < lightDistance);
   }

   /**
    * Calculates the diffuse reflection coefficient for the active light.
    *
    * @param intersection intersection shading context
    * @return diffuse coefficient per RGB channel
    */
   private Double3 calcDiffuse(Intersection intersection) {
      return intersection.material.kD.scale(Math.abs(intersection.lNormal));
   }

   /**
    * Calculates the specular reflection coefficient for the active light.
    *
    * @param intersection intersection shading context
    * @return specular coefficient per RGB channel
    */
   private Double3 calcSpecular(Intersection intersection) {
      Vector r = intersection.l.subtract(intersection.normal.scale(2d * intersection.lNormal));
      double minusVR = -r.dotProduct(intersection.v);
      if (minusVR <= 0d) {
         return Double3.ZERO;
      }

      return intersection.material.kS.scale(Math.pow(minusVR, intersection.material.nShininess));
   }

   @Override
   Color traceRay(Ray ray) {
      var intersections = _scene.geometries.calcIntersections(ray);
      return intersections == null
         ? _scene.background
         : calcColor(ray.findClosestIntersection(intersections), ray.direction());
   }
}
