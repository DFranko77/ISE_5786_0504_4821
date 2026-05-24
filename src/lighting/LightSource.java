package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Represents an external light source in the scene.
 */
public interface LightSource {
   /**
    * Calculates the normalized direction vector from the light source
    * to the illuminated point.
    *
    * @param p illuminated point
    * @return normalized direction vector from light to point
    */
   Vector getL(Point p);

   /**
    * Calculates the light intensity reaching a given point.
    *
    * @param p illuminated point
    * @return light intensity at the point
    */
   Color getIntensity(Point p);
}
