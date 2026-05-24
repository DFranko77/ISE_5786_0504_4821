package renderer;

import geometries.api.Intersectable.Intersection;
import lighting.LightSource;
import primitives.Color;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static primitives.Util.alignZero;
import static primitives.Util.compareSign;
import static primitives.Util.isZero;

/**
 * Base class for ray tracers.
 */
abstract class RayTracerBase {
   /** Scene rendered by this tracer. */
   protected final Scene _scene;

   /**
	* Creates a ray tracer for the given scene.
	*
	* @param scene scene to render
	*/
   RayTracerBase(Scene scene) {
	  if (scene == null)
		 throw new IllegalArgumentException("Scene must not be null");
	  _scene = scene;
   }

   /**
	* Traces a ray and returns its resulting color.
	*
	* @param ray ray to trace
	* @return resulting color
	*/
   abstract Color traceRay(Ray ray);

   /**
	* Pre-computes intersection values used repeatedly during local shading.
	*
	* @param  intersection hit details to initialize
	* @param  rayDirection incoming ray direction
	* @return              {@code true} if the incoming ray is not tangent to the
	*                      surface ({@code vNormal != 0})
	*/
   protected boolean preprocessIntersection(Intersection intersection, Vector rayDirection) {
	  intersection.normal = intersection.geometry.getNormal(intersection.point);
	  intersection.v = rayDirection;
	  intersection.vNormal = alignZero(intersection.v.dotProduct(intersection.normal));
	  return !isZero(intersection.vNormal);
   }

   /**
	* Pre-computes light-source values used repeatedly during local shading.
	*
	* @param  intersection hit details to update
	* @param  lightSource  active light source
	* @return              {@code true} when light and view are on the same side of
	*                      the surface normal
	*/
   protected boolean preprocessLightSource(Intersection intersection, LightSource lightSource) {
	  intersection.light = lightSource;
	  intersection.l = lightSource.getL(intersection.point);
	  intersection.lNormal = alignZero(intersection.l.dotProduct(intersection.normal));
	  return !isZero(intersection.lNormal) && compareSign(intersection.lNormal, intersection.vNormal);
   }
}
