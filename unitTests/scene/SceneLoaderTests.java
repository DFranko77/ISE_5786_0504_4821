package scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import geometries.api.Intersectable.Intersection;
import primitives.Double3;
import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.io.SceneLoader;

/**
 * Focused tests for external scene loading (XML/JSON).
 */
class SceneLoaderTests {

   @Test
   void loadXmlScene() {
      Scene scene = SceneLoader.loadFromXml("basicRenderTestTwoColors");
      verifyBasicTwoColorsScene(scene);
   }

   @Test
   void loadJsonScene() {
      Scene scene = SceneLoader.loadFromJson("basicRenderTestTwoColors");
      verifyBasicTwoColorsScene(scene);
   }

   @Test
   void loadStage7XmlWithLightsAndPhongMaterial() {
      Scene scene = SceneLoader.load("stage7/lightSpherePoint.xml");
      verifyStage7Scene(scene, 2, 1, new Double3(0.5), new Double3(0.5), 301);
   }

   @Test
   void loadStage7JsonWithLightsAndPhongMaterial() {
      Scene scene = SceneLoader.load("stage7/lightSphereSpot.json");
      verifyStage7Scene(scene, 2, 1, new Double3(0.5), new Double3(0.5), 301);
   }

   private static void verifyBasicTwoColorsScene(Scene scene) {
      assertEquals(new Color(75, 127, 190), scene.background, "Background should be loaded from file");
      assertEquals(new Color(255, 191, 191), scene.ambientLight.getIntensity(), "Ambient light should be loaded from file");

      List<Point> intersections = scene.geometries.findIntersections(new Ray(Point.ZERO, new Vector(0, 0, -1)));
      assertNotNull(intersections, "Loaded scene should include geometries");
      assertEquals(2, intersections.size(), "Expected the center ray to intersect the central sphere twice");
   }

   private static void verifyStage7Scene(Scene scene, int expectedGeometryHits, int expectedLights, Double3 expectedKD, Double3 expectedKS,
         int expectedShininess) {
      assertEquals(expectedLights, scene.lights.size(), "Stage 7 scene should include external lights from file");

      List<Intersection> intersections = scene.geometries.calcIntersections(new Ray(new Point(0, 0, 1000), new Vector(0, 0, -1)));
      assertNotNull(intersections, "Stage 7 scene should include geometry intersections");
      assertEquals(expectedGeometryHits, intersections.size(), "Unexpected number of center ray intersections");

      Intersection firstHit = intersections.get(0);
      assertEquals(expectedKD, firstHit.material.kD, "kD should be loaded from scene file");
      assertEquals(expectedKS, firstHit.material.kS, "kS should be loaded from scene file");
      assertEquals(expectedShininess, firstHit.material.nShininess, "Shininess should be loaded from scene file");
   }
}
