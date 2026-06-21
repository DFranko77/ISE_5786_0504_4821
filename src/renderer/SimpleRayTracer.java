package renderer;

import geometries.api.Intersectable.Intersection;
import lighting.LightSource;
import primitives.Blackboard;
import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import java.util.List;

/**
 * Basic ray tracer that returns scene background or ambient color.
 */
class SimpleRayTracer extends RayTracerBase {
   private static final double DELTA = 0.1;
   private static final int MAX_CALC_COLOR_LEVEL = 10;
   private static final double MIN_CALC_COLOR_K = 0.001;
   private static final Double3 INITIAL_K = Double3.ONE;

   /**
    * Creates a simple ray tracer for the given scene.
    *
    * @param scene scene to render
    */
   SimpleRayTracer(Scene scene) {
      super(scene);
   }

   /**
    * Entry-point: pre-processes the intersection and adds ambient light on top of
    * the recursively computed color.
    *
    * @param intersection closest intersection details
    * @param v            incoming ray direction
    * @return color at the intersection point
    */
   private Color calcColor(Intersection intersection, Vector v) {
      return !preprocessIntersection(intersection, v)
         ? Color.BLACK
         : calcColor(intersection, MAX_CALC_COLOR_LEVEL, INITIAL_K)
            .add(_scene.ambientLight.getIntensity().scale(intersection.material.kA));
   }

   /**
    * Recursive color calculation combining local and global effects.
    *
    * @param intersection shading context
    * @param level        remaining recursion depth
    * @param k            cumulative attenuation factor
    * @return computed color contribution
    */
   private Color calcColor(Intersection intersection, int level, Double3 k) {
      if (k.isLowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;
      Color color = calcColorLocalEffects(intersection);
      return level == 1 ? color : color.add(calcColorGlobalEffects(intersection, level, k));
   }

   /**
    * Sums the color contributions from reflected and refracted secondary rays.
    *
    * @param intersection shading context
    * @param level        remaining recursion depth
    * @param k            cumulative attenuation factor
    * @return global effects color contribution
    */
   private Color calcColorGlobalEffects(Intersection intersection, int level, Double3 k) {
      Color color = Color.BLACK;
      Double3 kr = intersection.material.kR;
      if (!kr.isLowerThan(MIN_CALC_COLOR_K))
         color = color.add(calcColorGlobalEffect(constructReflectedRay(intersection), level, k, kr));
      Double3 kt = intersection.material.kT;
      if (!kt.isLowerThan(MIN_CALC_COLOR_K))
         color = color.add(calcColorGlobalEffect(constructRefractedRay(intersection), level, k, kt));
      return color;
   }

   /**
    * Traces a single secondary ray and returns its attenuated color contribution.
    *
    * @param ray   secondary ray to trace
    * @param level remaining recursion depth
    * @param k     cumulative attenuation factor so far
    * @param kx    attenuation factor for this effect (kR or kT)
    * @return color contribution scaled by {@code kx}
    */
   private Color calcColorGlobalEffect(Ray ray, int level, Double3 k, Double3 kx) {
      Double3 kkx = k.product(kx);
      if (kkx.isLowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;
      var intersections = _scene.geometries.calcIntersections(ray);
      if (intersections == null) return _scene.background.scale(kx);
      Intersection intersection = ray.findClosestIntersection(intersections);
      return preprocessIntersection(intersection, ray.direction())
         ? calcColor(intersection, level - 1, kkx).scale(kx)
         : Color.BLACK;
   }

   /**
    * Constructs the reflected ray at the intersection point.
    *
    * @param intersection shading context
    * @return reflected ray
    */
   private Ray constructReflectedRay(Intersection intersection) {
      Vector n = intersection.normal;
      Vector v = intersection.v;
      // r = v - 2*(v·n)*n
      Vector r = v.subtract(n.scale(2d * intersection.vNormal));
      // shift the ray origin slightly along the normal to avoid self-intersection
      double sign = r.dotProduct(n) < 0 ? -DELTA : DELTA;
      Point head = intersection.point.add(n.scale(sign));
      return new Ray(head, r);
   }

   /**
    * Constructs the refracted (transparency) ray at the intersection point.
    *
    * @param intersection shading context
    * @return refracted ray
    */
   private Ray constructRefractedRay(Intersection intersection) {
      Vector n = intersection.normal;
      Vector v = intersection.v;
      // For simple transparency the refracted ray continues in the same direction as the incoming ray
      Vector t = v;
      double sign = t.dotProduct(n) < 0 ? -DELTA : DELTA;
      Point head = intersection.point.add(n.scale(sign));
      return new Ray(head, t);
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
         if (preprocessLightSource(intersection, lightSource)) {
            Double3 ktr = transparency(intersection);
            if (!ktr.isLowerThan(MIN_CALC_COLOR_K)) {
               Color lightIntensity = lightSource.getIntensity(intersection.point).scale(ktr);
               Double3 lightFactor = calcDiffuse(intersection).add(calcSpecular(intersection));
               color = color.add(lightIntensity.scale(lightFactor));
            }
         }
      }
      return color;
   }

   /**
    * Computes the cumulative transparency factor between the intersection point
    * and the given light source, accounting for all partially transparent objects
    * in between.
    *
    * @param intersection shading context
    * @param light        active light source
    * @param l            direction from light to the intersection point
    * @param n            surface normal at the intersection point
    * @return transparency factor per channel; {@link Double3#ZERO} if fully shadowed
    */
   private Double3 transparency(Intersection intersection, LightSource light, Vector l, Vector n) {
      Vector shift = n.scale(intersection.vNormal < 0 ? DELTA : -DELTA);
      Point shiftedHead = intersection.point.add(shift);

      double radius = light.getRadius();
      Point lightPosition = light.getPosition();
      // Hard shadow (default): a point-sized light fires a single ray at its center.
      if (radius <= 0d || light.getNumOfRays() <= 1 || lightPosition == null) {
         return shadowRayTransparency(shiftedHead, l.scale(-1d), light.getDistance(intersection.point));
      }

      // Soft shadow: sample the light's disk (oriented orthogonal to l) and
      // average the per-ray transparency. Occluded samples contribute zero,
      // which is what yields gradual penumbrae and the sunset effect.
      List<Point> samples = new Blackboard()
         .setSize(radius)
         .setNumOfRays(light.getNumOfRays())
         .orient(lightPosition, l)
         .points();
      Double3 ktrSum = Double3.ZERO;
      for (Point sample : samples) {
         Vector toSample = sample.subtract(shiftedHead);
         ktrSum = ktrSum.add(shadowRayTransparency(shiftedHead, toSample, toSample.length()));
      }
      return ktrSum.divide(samples.size());
   }

   /**
    * Accumulates the transparency factor along a single shadow ray, multiplying
    * the {@code kT} of every partially transparent blocker between the shaded
    * point and the light sample.
    *
    * @param origin      shifted shadow-ray origin (already offset off the surface)
    * @param direction   direction toward the light (or light sample)
    * @param maxDistance distance to the light (or light sample); blockers beyond it are ignored
    * @return transparency factor per channel; {@link Double3#ZERO} if fully shadowed
    */
   private Double3 shadowRayTransparency(Point origin, Vector direction, double maxDistance) {
      Ray shadowRay = new Ray(origin, direction);
      var shadowIntersections = _scene.geometries.calcIntersections(shadowRay, maxDistance);
      if (shadowIntersections == null) return Double3.ONE;
      Double3 ktr = Double3.ONE;
      for (Intersection hit : shadowIntersections) {
         ktr = ktr.product(hit.material.kT);
         if (ktr.isLowerThan(MIN_CALC_COLOR_K)) return Double3.ZERO;
      }
      return ktr;
   }

   private Double3 transparency(Intersection intersection) {
      return transparency(intersection, intersection.light, intersection.l, intersection.normal);
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
