package lighting;

import primitives.Color;

/**
 * Base type for all light entities.
 * <p>
 * This class stores the original (source) light intensity only.
 * </p>
 */
abstract class Light {
   /** Original light intensity. */
   protected final Color _intensity;

   /**
	* Creates a light with the given source intensity.
	*
	* @param intensity source light intensity
	*/
   protected Light(Color intensity) {
	  if (intensity == null) {
		 throw new IllegalArgumentException("Light intensity must not be null");
	  }
	  _intensity = intensity;
   }

   /**
	* Returns the original light intensity.
	*
	* @return source light intensity
	*/
   public Color getIntensity() {
	  return _intensity;
   }
}
