package renderer;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Vector;
import scene.Scene;
import scene.io.SceneLoader;

/**
 * Stage 7 rendering tests where scenes are loaded from XML/JSON files.
 */
class LightsFromFilesTests {
   /** Constant for tests resolution. */
   private static final int RESOLUTION = 500;

   /** Produce a picture of a sphere lighted by a directional light from JSON. */
   @Test
   void testSphereDirectionalFromJson() {
      render("stage7/lightSphereDirectional.json", 150, 150, "lightSphereDirectional_file");
   }

   /** Produce a picture of a sphere lighted by a point light from XML. */
   @Test
   void testSpherePointFromXml() {
      render("stage7/lightSpherePoint.xml", 150, 150, "lightSpherePoint_file");
   }

   /** Produce a picture of a sphere lighted by a spotlight from JSON. */
   @Test
   void testSphereSpotFromJson() {
      render("stage7/lightSphereSpot.json", 150, 150, "lightSphereSpot_file");
   }

   /** Produce a picture of two triangles lighted by a directional light from XML. */
   @Test
   void testTrianglesDirectionalFromXml() {
      render("stage7/lightTrianglesDirectional.xml", 200, 200, "lightTrianglesDirectional_file");
   }

   /** Produce a picture of two triangles lighted by a point light from JSON. */
   @Test
   void testTrianglesPointFromJson() {
      render("stage7/lightTrianglesPoint.json", 200, 200, "lightTrianglesPoint_file");
   }

   /** Produce a picture of two triangles lighted by a spotlight from XML. */
   @Test
   void testTrianglesSpotFromXml() {
      render("stage7/lightTrianglesSpot.xml", 200, 200, "lightTrianglesSpot_file");
   }

   /** Produce a picture of a sphere lighted by a narrow spotlight from XML. */
   @Test
   void testSphereSpotSharpFromXml() {
      render("stage7/lightSphereSpotSharp.xml", 150, 150, "lightSphereSpotSharp_file");
   }

   /** Produce a picture of two triangles lighted by a narrow spotlight from JSON. */
   @Test
   void testTrianglesSpotSharpFromJson() {
      render("stage7/lightTrianglesSpotSharp.json", 200, 200, "lightTrianglesSpotSharp_file");
   }

   private static void render(String sceneFile, double vpWidth, double vpHeight, String imageName) {
      Scene scene = SceneLoader.load(sceneFile);

      Camera.getBuilder()
         .setRayTracer(scene, RayTracerType.SIMPLE)
         .setLocation(new Point(0, 0, 1000))
         .setDirection(Point.ZERO, Vector.AXIS_Y)
         .setVpSize(vpWidth, vpHeight)
         .setVpDistance(1000)
         .setResolution(RESOLUTION, RESOLUTION)
         .build()
         .renderImage()
         .writeToImage(imageName);
   }
}
