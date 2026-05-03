package scene;

import geometries.impl.Geometries;
import lighting.AmbientLight;
import primitives.Color;

/**
 * Plain data structure describing a render scene.
 */
public class Scene {
   /** Scene name. */
   public final String    name;

   /** Scene background color. */
   public Color           background   = Color.BLACK;

   /** Ambient light contribution. */
   public AmbientLight    ambientLight = AmbientLight.NONE;

   /** Collection of scene geometries. */
   public Geometries      geometries   = new Geometries();

   /**
	* Creates a scene with the given name.
	*
	* @param name scene name
	*/
   public Scene(String name) {
	  if (name == null)
		 throw new IllegalArgumentException("Scene name must not be null");
	  this.name = name;
   }

   /**
	* Sets the scene background color.
	*
	* @param background background color
	* @return this scene for chaining
	*/
   public Scene setBackground(Color background) {
	  if (background == null)
		 throw new IllegalArgumentException("Background must not be null");
	  this.background = background;
	  return this;
   }

   /**
	* Sets the scene ambient light.
	*
	* @param ambientLight ambient light
	* @return this scene for chaining
	*/
   public Scene setAmbientLight(AmbientLight ambientLight) {
	  if (ambientLight == null)
		 throw new IllegalArgumentException("Ambient light must not be null");
	  this.ambientLight = ambientLight;
	  return this;
   }

   /**
	* Sets the scene geometries container.
	*
	* @param geometries geometries collection
	* @return this scene for chaining
	*/
   public Scene setGeometries(Geometries geometries) {
	  if (geometries == null)
		 throw new IllegalArgumentException("Geometries must not be null");
	  this.geometries = geometries;
	  return this;
   }
}
