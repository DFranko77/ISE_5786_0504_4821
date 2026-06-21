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
   /** Radius of the emitting area; {@code 0} keeps the source point-sized (hard shadows). */
   private double _size = 0d;
   /** Number of shadow rays sampled across the area when {@code _size > 0}. */
   private int _numOfRays = 1;

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

   /**
    * Sets the radius of the light's emitting area. {@code 0} (the default) keeps
    * the source point-sized — the existing hard-shadow behavior. A positive
    * radius turns this into an area light: combined with {@link #setNumOfRays}
    * (&gt; 1) it produces soft shadows.
    *
    * @param size area radius (&ge; 0)
    * @return this light for chaining
    */
   public PointLight setSize(double size) {
      _size = size;
      return this;
   }

   /**
    * Sets how many shadow rays are sampled across the light's area. Has no
    * effect unless {@link #setSize} is also positive; {@code 1} (the default)
    * keeps the single-ray hard shadow.
    *
    * @param numOfRays number of soft-shadow sample rays (&ge; 1)
    * @return this light for chaining
    */
   public PointLight setNumOfRays(int numOfRays) {
      _numOfRays = numOfRays;
      return this;
   }

   @Override
   public double getRadius() {
      return _size;
   }

   @Override
   public int getNumOfRays() {
      return _numOfRays;
   }

   @Override
   public Point getPosition() {
      return _position;
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

   @Override
   public double getDistance(Point point) {
      return _position.distance(point);
   }
}
