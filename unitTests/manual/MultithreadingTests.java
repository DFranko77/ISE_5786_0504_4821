package manual;

import org.junit.jupiter.api.Test;

import lighting.AmbientLight;
import primitives.Color;
import primitives.Point;
import primitives.Vector;
import renderer.Camera;
import renderer.Camera.RenderMode;
import renderer.RayTracerType;
import scene.Scene;

import java.util.Random;

/**
 * Benchmark that demonstrates the two parallel rendering methods and lets the
 * choice between them be made (and measured) from a test.
 *
 * <p>It builds the full {@link BeachScene} (834 geometries, two palm trees),
 * then renders it three times — once single-threaded, once with each parallel
 * method — and prints the render times and the speedup over the single-threaded
 * baseline, isolating the multithreading speedup as the mini-project requires.</p>
 *
 * <p>Run {@link #compareRenderModes()} and read the console. On a multi-core
 * machine the two parallel methods should come in well under the single-threaded
 * time (a 2.3x+ improvement is typical, though it depends on the machine, the
 * scene and the background load).</p>
 */
class MultithreadingTests {

   /** Render resolution for the benchmark — high enough that the single-threaded
    * run is long enough to measure meaningfully, but smaller than the final
    * {@code beach.png} so the three back-to-back renders don't take too long. */
   private static final int NX = 800;
   /** Vertical benchmark resolution (keeps the 1200:728 beach aspect ratio). */
   private static final int NY = 486;

   /** Default constructor to satisfy the JavaDoc generator. */
   MultithreadingTests() { /* to satisfy JavaDoc generator */ }

   /**
    * Builds the full beach scene (same geometry as {@code beach.png}) with hard
    * shadows, so every render mode does the identical work.
    *
    * @return the populated scene
    */
   private static Scene buildBeach() {
      Scene scene = new Scene("Beach (benchmark)")
         .setBackground(new Color(250, 168, 112))
         .setAmbientLight(new AmbientLight(new Color(10, 8, 7)));

      BeachScene.addEnvironment(scene);
      BeachScene.addProps(scene);
      BeachScene.addSailboat(scene, new Point(20, -58, -400));
      BeachScene.addBackground(scene);
      PalmTreeScene.addPalmTree(scene, new Point(-95, -59, -80), new Random(7));
      PalmTreeScene.addPalmTree(scene, new Point(120, -59, -160), new Random(21));
      BeachScene.addLounger(scene, new Point(95, -60, -40), 0.7);
      BeachScene.addLounger(scene, new Point(118, -60, -72), 0.78);
      BeachScene.addLights(scene);

      return scene;
   }

   /**
    * Renders the beach once in the given mode and returns the wall-clock time.
    *
    * @param mode rendering strategy to use
    * @return render time in milliseconds
    */
   private static long render(RenderMode mode) {
      Scene scene = buildBeach();
      Camera camera = Camera.getBuilder()
         .setLocation(new Point(0, 35, 300))
         .setVpDistance(350)
         .setVpSize(280, 170)
         .setDirection(new Point(0, -18, -500), Vector.AXIS_Y)
         .setResolution(NX, NY)
         .setRayTracer(scene, RayTracerType.SIMPLE)
         .setRenderMode(mode)
         .build();

      long start = System.nanoTime();
      camera.renderImage();
      return (System.nanoTime() - start) / 1_000_000;
   }

   /**
    * Renders the beach in all three modes and prints the timings and the speedup
    * of each parallel method over the single-threaded baseline.
    */
   @Test
   void compareRenderModes() {
      long single = render(RenderMode.SINGLE);
      long stream = render(RenderMode.STREAM);
      long threads = render(RenderMode.THREADS);

      System.out.printf("Cores available : %d%n", Runtime.getRuntime().availableProcessors());
      System.out.printf("SINGLE  : %6d ms%n", single);
      System.out.printf("STREAM  : %6d ms   (%.2fx faster)%n", stream, single / (double) stream);
      System.out.printf("THREADS : %6d ms   (%.2fx faster)%n", threads, single / (double) threads);
   }
}
