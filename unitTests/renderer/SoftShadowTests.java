package renderer;

import org.junit.jupiter.api.Test;

import geometries.impl.Plane;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.PointLight;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Minimal, fast-rendering sanity check for soft shadows: a single sphere resting
 * on a floor, lit by one area light, so the only thing in the picture is the
 * shadow the sphere casts. Renders quickly (one shape, small resolution), so you
 * can iterate on the area-light settings and actually <em>see</em> whether the
 * penumbra softens — without waiting on a full beach render.
 *
 * <p><b>The knobs you care about are the three constants below.</b> Change them,
 * re-run {@link #softShadowSphere()}, and open {@code images/softShadowTest.png}:</p>
 * <ul>
 *   <li>{@link #SOFT} — master on/off. {@code false} = single-ray hard shadow
 *       (the baseline to compare against); {@code true} = sampled area light.</li>
 *   <li>{@link #LIGHT_RADIUS} — the softness dial. Bigger disk → wider, softer
 *       penumbra. {@code 0} behaves like a hard shadow even when {@link #SOFT}.</li>
 *   <li>{@link #LIGHT_RAYS} — the quality dial. More rays → smoother penumbra,
 *       but a slower render. Too few → grainy/banded shadow.</li>
 * </ul>
 */
class SoftShadowTests {

   // ----------------------------------------------------------------------
   //  TWEAK THESE — then re-run softShadowSphere() and view the PNG.
   // ----------------------------------------------------------------------
   /** Master switch: {@code true} = soft (area light), {@code false} = hard (single ray). */
   private static final boolean SOFT = true;
   /** Light disk radius — the softness of the shadow edge. Bigger = softer. */
   private static final double LIGHT_RADIUS = 50d;
   /** Shadow-ray samples across the disk — smoothness vs. speed. More = smoother, slower. */
   private static final int LIGHT_RAYS = 80;
   // ----------------------------------------------------------------------

   /** Default constructor to satisfy the JavaDoc generator. */
   SoftShadowTests() { /* to satisfy JavaDoc generator */ }

   /**
    * Renders the one-sphere-on-a-floor scene with the settings above and writes
    * {@code images/softShadowTest.png}. With {@link #SOFT} off the shadow has a
    * crisp edge; with it on (and a positive {@link #LIGHT_RADIUS}) the edge fades
    * into a penumbra that widens away from the contact point.
    */
   @Test
   void softShadowSphere() {
      Scene scene = new Scene("Soft shadow test")
         .setBackground(new Color(40, 45, 55))           // muted slate, so the floor + shadow read clearly
         .setAmbientLight(new AmbientLight(new Color(18, 18, 20)));   // low: lets the shadow go dark

      // Floor: a horizontal matte plane the sphere sits on and casts onto.
      scene.geometries.add(new Plane(new Point(0, -50, 0), new Vector(0, 1, 0))
         .setEmission(new Color(45, 48, 55))
         .setMaterial(new Material().setKD(0.7).setKS(0.15).setShininess(20)));

      // The single occluder: a glossy sphere resting on the floor (bottom at y=-50),
      // so the shadow is sharp at the contact point and softens as it spreads.
      scene.geometries.add(new Sphere(new Point(0, -15, 0), 35d)
         .setEmission(new Color(150, 35, 35))
         .setMaterial(new Material().setKD(0.5).setKS(0.5).setShininess(100)));

      // One bright area light, up and to the front-left, so the shadow rakes
      // across the floor toward the lower-right where the camera can see it.
      PointLight light = new PointLight(new Color(700, 700, 700), new Point(-90, 130, 90))
         .setKl(2E-4).setKq(1E-6);
      if (SOFT && LIGHT_RADIUS > 0d) light.setSize(LIGHT_RADIUS).setNumOfRays(LIGHT_RAYS);
      scene.lights.add(light);

      // Camera looking down at the floor at an angle so the cast shadow is the
      // main subject of the frame.
      Camera.getBuilder()
         .setLocation(new Point(0, 150, 250))
         .setVpDistance(250)
         .setVpSize(250, 250)
         .setDirection(new Point(0, -50, -30), Vector.AXIS_Y)
         .setResolution(500, 500)
         .setRayTracer(scene, RayTracerType.SIMPLE)
         .build()
         .renderImage()
         .writeToImage("softShadowTest");
   }
}
