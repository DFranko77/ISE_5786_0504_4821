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

   /**
    * Returns the distance from the light source to the given point.
    *
    * @param point illuminated point
    * @return distance to the light source
    */
   double getDistance(Point point);

   /**
    * Returns the radius of the light's emitting area. A radius of {@code 0}
    * means a point-sized source (hard shadows); a positive radius enables
    * soft shadows by sampling across the disk.
    *
    * @return area radius, or {@code 0} for a point-sized source
    */
   default double getRadius() {
      return 0d;
   }

   /**
    * Returns how many shadow rays to sample across the light's area. A value of
    * {@code 1} (with a zero radius) yields the single-ray hard shadow.
    *
    * @return number of soft-shadow sample rays
    */
   default int getNumOfRays() {
      return 1;
   }

   /**
    * Returns the position of the light source, or {@code null} for sources that
    * have no position (e.g. a directional light) and therefore no shadow area.
    *
    * @return light position, or {@code null}
    */
   default Point getPosition() {
      return null;
   }
}
