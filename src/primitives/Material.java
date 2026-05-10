package primitives;

/**
 * Plain data structure for geometry material coefficients.
 */
public class Material {
   /** Ambient attenuation coefficient. */
   public Double3 kA = Double3.ONE;

   /**
    * Sets the ambient attenuation coefficient.
    *
    * @param kA ambient coefficient per channel
    * @return this material for chaining
    */
   public Material setKA(Double3 kA) {
      this.kA = kA;
      return this;
   }

   /**
    * Sets a uniform ambient attenuation coefficient for all channels.
    *
    * @param kA ambient coefficient value
    * @return this material for chaining
    */
   public Material setKA(double kA) {
      this.kA = new Double3(kA);
      return this;
   }
}
