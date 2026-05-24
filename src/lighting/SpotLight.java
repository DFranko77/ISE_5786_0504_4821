package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Spot light emitted from a point with a preferred direction.
 */
public class SpotLight extends PointLight {
   /** Spot direction (normalized). */
   private final Vector _direction;
   /** Beam concentration exponent (1 = regular spotlight). */
   private double _narrowBeam = 1d;

   /**
    * Creates a spot light.
    *
    * @param intensity light source intensity
    * @param position light source position
    * @param direction spot direction
    */
   public SpotLight(Color intensity, Point position, Vector direction) {
      super(intensity, position);
      _direction = direction.normalize();
   }

   @Override
   public SpotLight setKc(double kC) {
      return (SpotLight) super.setKc(kC);
   }

   @Override
   public SpotLight setKl(double kL) {
      return (SpotLight) super.setKl(kL);
   }

   @Override
   public SpotLight setKq(double kQ) {
      return (SpotLight) super.setKq(kQ);
   }

   /**
    * Sets the narrow-beam exponent for projector-like spotlight behavior.
    *
    * @param narrowBeam beam concentration exponent (> 0)
    * @return this spotlight for chaining
    */
   public SpotLight setNarrowBeam(double narrowBeam) {
      if (narrowBeam <= 0d) {
         throw new IllegalArgumentException("Narrow beam exponent must be greater than 0");
      }
      _narrowBeam = narrowBeam;
      return this;
   }

   @Override
   public Vector getL(Point p) {
      return super.getL(p);
   }

   @Override
   public Color getIntensity(Point p) {
      if (p.equals(_position)) {
         return super.getIntensity(p);
      }

      double dirFactor = _direction.dotProduct(getL(p));
      if (dirFactor <= 0d) {
         return Color.BLACK;
      }

      double focusedFactor = _narrowBeam == 1d ? dirFactor : Math.pow(dirFactor, _narrowBeam);
      return super.getIntensity(p).scale(focusedFactor);
   }
}