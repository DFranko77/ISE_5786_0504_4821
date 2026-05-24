package renderer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import lighting.DirectionalLight;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Unit tests for {@link DirectionalLight}.
 */
class DirectionalLightTests {
   private static final Color SOURCE_INTENSITY = new Color(30, 60, 90);
   private static final Vector LIGHT_DIRECTION = new Vector(0, 2, -2);
   private static final Point QUERY_POINT = new Point(1, 1, 1);
   private static final Vector EXPECTED_DIRECTION = new Vector(0, 1, -1).normalize();


   /**
    * Tests {@link DirectionalLight#getL(Point)}.
    */
   @Test
   void testGetL() {
      final int epTestCases = 1;

      DirectionalLight light = new DirectionalLight(new Color(120, 80, 40), LIGHT_DIRECTION);

      // ============ EP: Equivalence Partitions Tests ============
      assertAll("EP (" + epTestCases + " cases)",
         () -> {
            // TC EP01: Direction should be normalized and independent of the queried point.
            Vector l = light.getL(QUERY_POINT);
            assertEquals(EXPECTED_DIRECTION, l,
               "Directional light getL should return the normalized fixed direction");
         }
      );
   }

   /**
    * Tests {@link DirectionalLight#getIntensity(Point)}.
    */
   @Test
   void testGetIntensity() {
      final int epTestCases = 1;

      DirectionalLight light = new DirectionalLight(SOURCE_INTENSITY, new Vector(1, 2, 3));

      // ============ EP: Equivalence Partitions Tests ============
      assertAll("EP (" + epTestCases + " cases)",
         () -> {
            // TC EP01: Directional light intensity at a point should equal source intensity.
            Color intensity = light.getIntensity(new Point(1, 2, 3));
            assertEquals(SOURCE_INTENSITY, intensity, "Directional light should not attenuate by distance");
         }
      );
   }
}
