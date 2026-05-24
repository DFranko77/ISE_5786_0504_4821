package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Point light emitted from a specific position.
 */
public class PointLight extends Light implements LightSource {
   /** Light source position. */
   protected final Point _position;
   /** Constant attenuation coefficient. */
   private double _kC = 1d;
   /** Linear attenuation coefficient. */
   private double _kL = 0d;
   /** Quadratic attenuation coefficient. */
   private double _kQ = 0d;

   /**
    * Creates a point light.
    *
    * @param intensity light source intensity
    * @param position light source position
    */
   public PointLight(Color intensity, Point position) {
      super(intensity);
      _position = position;
   }

   /**
    * Sets the constant attenuation coefficient.
    *
    * @param kC constant attenuation coefficient
    * @return this light for chaining
    */
   public PointLight setKc(double kC) {
      _kC = kC;
      return this;
   }

   /**
    * Sets the linear attenuation coefficient.
    *
    * @param kL linear attenuation coefficient
    * @return this light for chaining
    */
   public PointLight setKl(double kL) {
      _kL = kL;
      return this;
   }

   /**
    * Sets the quadratic attenuation coefficient.
    *
    * @param kQ quadratic attenuation coefficient
    * @return this light for chaining
    */
   public PointLight setKq(double kQ) {
      _kQ = kQ;
      return this;
   }

   @Override
   public Vector getL(Point p) {
      return p.subtract(_position).normalize();
   }

   @Override
   public Color getIntensity(Point p) {
      double d = p.distance(_position);
      double attenuation = _kC + _kL * d + _kQ * d * d;
      return getIntensity().scale(1d / attenuation);
   }
}
