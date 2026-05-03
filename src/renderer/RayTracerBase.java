package renderer;

import primitives.Color;
import primitives.Ray;
import scene.Scene;

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
}
