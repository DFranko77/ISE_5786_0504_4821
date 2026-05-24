package renderer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import lighting.PointLight;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Unit tests for {@link PointLight}.
 */
class PointLightTests {
   private static final Color SOURCE_INTENSITY = new Color(90, 60, 30);
   private static final Point LIGHT_POSITION = new Point(0, 0, 0);


   /**
    * Tests {@link PointLight#getL(Point)}.
    */
   @Test
   void testGetL() {
      final int epTestCases = 1;
      final int bvTestCases = 1;

      Point lightPosition = new Point(1, 1, 1);
      PointLight light = new PointLight(new Color(200, 100, 50), lightPosition);

      // ============ EP: Equivalence Partitions Tests ============
      assertAll("EP (" + epTestCases + " cases)",
         () -> {
            // TC EP01: getL should return a normalized vector from the light position to the point.
            Point p = new Point(1, 1, 5);
            Vector l = light.getL(p);

            assertNotNull(l, "getL must return a direction vector");
            assertEquals(new Vector(0, 0, 1), l, "getL returned an incorrect direction vector");
         }
      );

      // ============ BVA: Boundary Value Analysis Tests ============
      assertAll("BVA (" + bvTestCases + " cases)",
         () -> {
            // TC BV01: Querying getL at the light position should fail (undefined zero direction).
            assertThrows(IllegalArgumentException.class,
               () -> light.getL(lightPosition),
               "getL should reject a point equal to the light position");
         }
      );
   }

   /**
    * Tests {@link PointLight#getIntensity(Point)}.
    */
   @Test
   void testGetIntensity() {
      final int epTestCases = 1;
      final int bvTestCases = 1;

      PointLight light = new PointLight(SOURCE_INTENSITY, LIGHT_POSITION);
      setAttenuation(light, 1d, 0.5d, 0.25d);

      // ============ EP: Equivalence Partitions Tests ============
      assertAll("EP (" + epTestCases + " cases)",
         () -> {
            // TC EP01: Intensity should be attenuated by KC + KL*d + KQ*d^2.
            Point p = new Point(0, 0, 2);
            double d = 2d;
            double attenuation = 1d + 0.5d * d + 0.25d * d * d;
            Color expected = SOURCE_INTENSITY.scale(1d / attenuation);
            Color intensity = light.getIntensity(p);

            assertEquals(expected, intensity, "Point light attenuation formula is incorrect");
         }
      );

      // ============ BVA: Boundary Value Analysis Tests ============
      assertAll("BVA (" + bvTestCases + " cases)",
         () -> {
            // TC BV01: At zero distance (light position), intensity should equal source intensity.
            Color atSource = light.getIntensity(LIGHT_POSITION);

            assertEquals(SOURCE_INTENSITY, atSource, "Intensity at the source position should equal the source intensity");
         }
      );
   }

   /**
    * Sets PointLight attenuation coefficients through reflection for TDD-level
    * unit tests.
    */
   private static void setAttenuation(PointLight light, double kC, double kL, double kQ) {
      setDoubleField(light, "_kC", kC);
      setDoubleField(light, "_kL", kL);
      setDoubleField(light, "_kQ", kQ);
   }

   private static void setDoubleField(PointLight light, String fieldName, double value) {
      try {
         Field field = PointLight.class.getDeclaredField(fieldName);
         field.setAccessible(true);
         field.setDouble(light, value);
      } catch (ReflectiveOperationException ex) {
         fail("PointLight is missing expected attenuation field " + fieldName + ": " + ex.getMessage());
      }
   }
}
