package renderer;

import org.junit.jupiter.api.Test;

import geometries.api.Geometry;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Rendering tests that combine multiple external light sources.
 */
class MultiLightsTests {
   /** Constant for tests resolution. */
   private static final int RESOLUTION = 500;

   /** Shininess value copied from LightsTests. */
   private static final int SHININESS = 301;
   /** Diffusion attenuation factor copied from LightsTests. */
   private static final double KD = 0.5;
   /** Diffusion attenuation factor copied from LightsTests. */
   private static final Double3 KD3 = new Double3(0.2, 0.6, 0.4);
   /** Specular attenuation factor copied from LightsTests. */
   private static final double KS = 0.5;
   /** Specular attenuation factor copied from LightsTests. */
   private static final Double3 KS3 = new Double3(0.2, 0.4, 0.3);

   /** Light color copied from LightsTests for triangles. */
   private static final Color TRIANGLES_LIGHT_COLOR = new Color(800, 500, 250);
   /** Light color copied from LightsTests for sphere. */
   private static final Color SPHERE_LIGHT_COLOR = new Color(800, 500, 0);
   /** Sphere color copied from LightsTests. */
   private static final Color SPHERE_COLOR = new Color(java.awt.Color.BLUE).reduce(2);

   /** Center of the sphere copied from LightsTests. */
   private static final Point SPHERE_CENTER = new Point(0, 0, -50);
   /** Radius of the sphere copied from LightsTests. */
   private static final double SPHERE_RADIUS = 50D;

   /** Triangles vertices copied from LightsTests. */
   private static final Point[] VERTICES = {
      new Point(-110, -110, -150),
      new Point(95, 100, -150),
      new Point(110, -110, -150),
      new Point(-75, 78, 100)
   };

   /**
    * Produce a picture of two triangles lit by directional, point and spot lights.
    */
   @Test
   @SuppressWarnings("java:S109")
   void testTrianglesMultipleLights() {
      Scene scene = new Scene("Triangles multiple lights")
         .setAmbientLight(new AmbientLight(new Color(38, 38, 38)));

      scene.geometries.add(createTriangle1(), createTriangle2());
      scene.lights.add(new DirectionalLight(new Color(220, 170, 120), new Vector(-1, -0.5, -2)));
      scene.lights.add(new PointLight(TRIANGLES_LIGHT_COLOR.scale(0.55), new Point(60, -40, -80))
         .setKl(0.0012).setKq(0.00018));
      scene.lights.add(new SpotLight(new Color(650, 380, 220), new Point(-70, 50, 20), new Vector(2, -1, -3))
         .setKl(0.001).setKq(0.00008).setNarrowBeam(7));

      Camera.getBuilder()
         .setRayTracer(scene, RayTracerType.SIMPLE)
         .setLocation(new Point(0, 0, 1000))
         .setDirection(Point.ZERO, Vector.AXIS_Y)
         .setVpSize(200, 200).setVpDistance(1000)
         .setResolution(RESOLUTION, RESOLUTION)
         .build()
         .renderImage()
         .writeToImage("lightTrianglesMultiple");
   }

   /**
    * Produce a picture of a sphere lit by directional, point and spot lights.
    */
   @Test
   @SuppressWarnings("java:S109")
   void testSphereMultipleLights() {
      Scene scene = new Scene("Sphere multiple lights");

      scene.geometries.add(createSphere());
      scene.lights.add(new DirectionalLight(new Color(170, 140, 260), new Vector(1, -1, -1.2)));
      scene.lights.add(new PointLight(SPHERE_LIGHT_COLOR.scale(0.6), new Point(-90, -20, 80))
         .setKl(0.0015).setKq(0.00022));
      scene.lights.add(new SpotLight(new Color(700, 350, 120), new Point(40, 80, 120), new Vector(-1.2, -1.6, -2.5))
         .setKl(0.001).setKq(0.00006).setNarrowBeam(6));

      Camera.getBuilder()
         .setRayTracer(scene, RayTracerType.SIMPLE)
         .setLocation(new Point(0, 0, 1000))
         .setDirection(Point.ZERO, Vector.AXIS_Y)
         .setVpSize(150, 150).setVpDistance(1000)
         .setResolution(RESOLUTION, RESOLUTION)
         .build()
         .renderImage()
         .writeToImage("lightSphereMultiple");
   }

   /** Creates a fresh sphere instance with the original material settings. */
   private static Geometry createSphere() {
      return new Sphere(SPHERE_CENTER, SPHERE_RADIUS)
         .setEmission(SPHERE_COLOR)
         .setMaterial(new Material().setKD(KD).setKS(KS).setShininess(SHININESS));
   }

   /** Creates the first triangle with original geometry and material values. */
   private static Geometry createTriangle1() {
      return new Triangle(VERTICES[0], VERTICES[1], VERTICES[2])
         .setMaterial(new Material().setKD(KD3).setKS(KS3).setShininess(SHININESS));
   }

   /** Creates the second triangle with original geometry and material values. */
   private static Geometry createTriangle2() {
      return new Triangle(VERTICES[0], VERTICES[1], VERTICES[3])
         .setMaterial(new Material().setKD(KD3).setKS(KS3).setShininess(SHININESS));
   }
}