package primitives;

/**
 * Plain data structure for geometry material coefficients.
 */
public class Material {
   /** Ambient attenuation coefficient. */
   public Double3 kA = Double3.ONE;
   /** Diffuse attenuation coefficient. */
   public Double3 kD = Double3.ZERO;
   /** Specular attenuation coefficient. */
   public Double3 kS = Double3.ZERO;
   /** Shininess factor for Phong specular reflection. */
   public int nShininess = 0;
   /** Transparency attenuation factor (0.0 = opaque, 1.0 = fully transparent). */
   public Double3 kT = Double3.ZERO;
   /** Reflection attenuation factor (0.0 = no reflection, 1.0 = perfect mirror). */
   public Double3 kR = Double3.ZERO;

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

   /**
    * Sets the diffuse attenuation coefficient.
    *
    * @param kD diffuse coefficient per channel
    * @return this material for chaining
    */
   public Material setKD(Double3 kD) {
      this.kD = kD;
      return this;
   }

   /**
    * Sets a uniform diffuse attenuation coefficient for all channels.
    *
    * @param kD diffuse coefficient value
    * @return this material for chaining
    */
   public Material setKD(double kD) {
      this.kD = new Double3(kD);
      return this;
   }

   /**
    * Sets the specular attenuation coefficient.
    *
    * @param kS specular coefficient per channel
    * @return this material for chaining
    */
   public Material setKS(Double3 kS) {
      this.kS = kS;
      return this;
   }

   /**
    * Sets a uniform specular attenuation coefficient for all channels.
    *
    * @param kS specular coefficient value
    * @return this material for chaining
    */
   public Material setKS(double kS) {
      this.kS = new Double3(kS);
      return this;
   }

   /**
    * Sets the shininess factor.
    *
    * @param nShininess shininess factor
    * @return this material for chaining
    */
   public Material setShininess(int nShininess) {
      this.nShininess = nShininess;
      return this;
   }

   /**
    * Sets the transparency attenuation coefficient.
    *
    * @param kT transparency coefficient per channel
    * @return this material for chaining
    */
   public Material setKT(Double3 kT) {
      this.kT = kT;
      return this;
   }

   /**
    * Sets a uniform transparency attenuation coefficient for all channels.
    *
    * @param kT transparency coefficient value
    * @return this material for chaining
    */
   public Material setKT(double kT) {
      this.kT = new Double3(kT);
      return this;
   }

   /**
    * Sets the reflection attenuation coefficient.
    *
    * @param kR reflection coefficient per channel
    * @return this material for chaining
    */
   public Material setKR(Double3 kR) {
      this.kR = kR;
      return this;
   }

   /**
    * Sets a uniform reflection attenuation coefficient for all channels.
    *
    * @param kR reflection coefficient value
    * @return this material for chaining
    */
   public Material setKR(double kR) {
      this.kR = new Double3(kR);
      return this;
   }
}
