package lighting;

import primitives.Color;

/**
 * Immutable ambient light definition for a scene.
 */
public final class AmbientLight extends Light {

   /** Ambient light with no intensity. */
   public static final AmbientLight NONE = new AmbientLight(Color.BLACK);

   /**
	* Creates ambient light with the given intensity color.
	*
	* @param intensity ambient intensity color
	*/
   public AmbientLight(Color intensity) {
	  super(intensity);
   }
}
