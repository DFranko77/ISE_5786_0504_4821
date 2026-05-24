package renderer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import lighting.PointLight;
import lighting.SpotLight;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Unit tests for {@link SpotLight}.
 */
class SpotLightTests {
   private static final Color SOURCE_INTENSITY = new Color(200, 120, 60);
   private static final Point LIGHT_POSITION = new Point(0, 0, 0);
   private static final Vector SPOT_DIRECTION = new Vector(0, 0, 1);


   /**
    * Tests {@link SpotLight#getL(Point)}.
    */
   @Test
   void testGetL() {
      final int epTestCases = 1;
      final int bvTestCases = 1;

      Point lightPosition = new Point(0, 0, 0);
      SpotLight light = new SpotLight(new Color(255, 255, 255), lightPosition, new Vector(0, 0, -1));

      // ============ EP: Equivalence Partitions Tests ============
      assertAll("EP (" + epTestCases + " cases)",
         () -> {
            // TC EP01: getL should return a normalized vector from light position to point.
            Point p = new Point(0, 3, 4);
            Vector l = light.getL(p);

            assertNotNull(l, "getL must return a direction vector");
            assertEquals(new Vector(0, 0.6, 0.8), l, "getL returned an incorrect normalized direction");
         }
      );

      // ============ BVA: Boundary Value Analysis Tests ============
      assertAll("BVA (" + bvTestCases + " cases)",
         () -> {
            // TC BV01: Querying getL at the source position should fail (undefined zero direction).
            assertThrows(IllegalArgumentException.class,
               () -> light.getL(lightPosition),
               "getL should reject a point equal to the light position");
         }
      );
   }

   /**
    * Tests {@link SpotLight#getIntensity(Point)}.
    */
   @Test
   void testGetIntensity() {
      final int epTestCases = 2;
      final int bvTestCases = 2;

      SpotLight light = new SpotLight(SOURCE_INTENSITY, LIGHT_POSITION, SPOT_DIRECTION);
      setAttenuation(light, 1d, 0.5d, 0d);

      // ============ EP: Equivalence Partitions Tests ============
      assertAll("EP (" + epTestCases + " cases)",
         () -> {
            // TC EP01: Point in front of the spotlight should be illuminated.
            Point p = new Point(0, 0, 2);
            double d = 2d;
            Color expected = SOURCE_INTENSITY.scale(1d / (1d + 0.5d * d));
            Color intensity = light.getIntensity(p);

            assertEquals(expected, intensity,
               "Spotlight intensity in front of the source should include attenuation and direction factor");
         },
         () -> {
            // TC EP02: Point behind the spotlight direction should receive no light.
            Color intensity = light.getIntensity(new Point(0, 0, -2));
            assertEquals(Color.BLACK, intensity, "Point behind spotlight should receive zero intensity");
         }
      );

      // ============ BVA: Boundary Value Analysis Tests ============
      assertAll("BVA (" + bvTestCases + " cases)",
         () -> {
            // TC BV01: At source position, intensity should equal original source intensity.
            Color intensity = light.getIntensity(LIGHT_POSITION);
            assertEquals(SOURCE_INTENSITY, intensity, "Intensity at spotlight source should equal source intensity");
         },
         () -> {
            // TC BV02: Exactly 90 degrees to spotlight direction should yield zero factor.
            Color intensity = light.getIntensity(new Point(1, 0, 0));
            assertEquals(Color.BLACK, intensity,
               "Point at 90 degrees to spotlight direction should receive zero intensity");
         }
      );
   }

   /**
    * Sets inherited PointLight attenuation coefficients through reflection.
    */
   private static void setAttenuation(SpotLight light, double kC, double kL, double kQ) {
      setDoubleField("_kC", light, kC);
      setDoubleField("_kL", light, kL);
      setDoubleField("_kQ", light, kQ);
   }

   private static void setDoubleField(String fieldName, SpotLight light, double value) {
      try {
         Field field = PointLight.class.getDeclaredField(fieldName);
         field.setAccessible(true);
         field.setDouble(light, value);
      } catch (ReflectiveOperationException ex) {
         fail("PointLight is missing expected attenuation field " + fieldName + ": " + ex.getMessage());
      }
   }
}
