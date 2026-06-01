package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Directional light with parallel rays.
 */
public class DirectionalLight extends Light implements LightSource {
   /** Light direction (normalized). */
   private final Vector _direction;

   /**
    * Creates a directional light.
    *
    * @param intensity light source intensity
    * @param direction light direction
    */
   public DirectionalLight(Color intensity, Vector direction) {
      super(intensity);
      _direction = direction.normalize();
   }

   @Override
   public Vector getL(Point ignored) {
      return _direction;
   }

   @Override
   public Color getIntensity(Point p) {
      return getIntensity();
   }

   @Override
   public double getDistance(Point point) {
      return Double.POSITIVE_INFINITY;
   }
}
