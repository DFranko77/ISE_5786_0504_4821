package scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

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

   private static void verifyBasicTwoColorsScene(Scene scene) {
      assertEquals(new Color(75, 127, 190), scene.background, "Background should be loaded from file");
      assertEquals(new Color(255, 191, 191), scene.ambientLight.getIntensity(), "Ambient light should be loaded from file");

      List<Point> intersections = scene.geometries.findIntersections(new Ray(Point.ZERO, new Vector(0, 0, -1)));
      assertNotNull(intersections, "Loaded scene should include geometries");
      assertEquals(2, intersections.size(), "Expected the center ray to intersect the central sphere twice");
   }
}
