package lighting;

import primitives.Color;

/**
 * Immutable ambient light definition for a scene.
 */
public final class AmbientLight {
   /** Ambient light intensity color. */
   private final Color _intensity;

   /** Ambient light with no intensity. */
   public static final AmbientLight NONE = new AmbientLight(Color.BLACK);

   /**
	* Creates ambient light with the given intensity color.
	*
	* @param intensity ambient intensity color
	*/
   public AmbientLight(Color intensity) {
	  if (intensity == null)
		 throw new IllegalArgumentException("Ambient intensity must not be null");
	  _intensity = intensity;
   }

   /**
	* Returns the ambient light intensity.
	*
	* @return ambient intensity color
	*/
   public Color getIntensity() {
	  return _intensity;
   }
}
