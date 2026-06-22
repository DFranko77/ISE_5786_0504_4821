package manual;

import org.junit.jupiter.api.Test;

import lighting.AmbientLight;
import primitives.Color;
import primitives.Point;
import primitives.Vector;
import renderer.Camera;
import renderer.RayTracerType;
import scene.Scene;

import java.util.Random;

/**
 * Second view of the {@link BeachScene}: the exact same tropical beach (same
 * sand, sea, sun, props, sailboat and palm trees), rendered from a camera that
 * has been moved to the right and turned back toward the scene. This oblique,
 * off-axis viewpoint gives a more natural, three-dimensional perspective of the
 * beach than the head-on shot in {@code beach.png}.
 *
 * <p>The scene itself is built by the package-private helpers in
 * {@link BeachScene}, so the two images are guaranteed to show the identical
 * world — only the camera differs.</p>
 */
public class BeachScene2 {

    /**
     * Master on/off switch for soft shadows in this view. {@code true} makes the
     * low sun an area light (soft penumbrae); {@code false} renders with the
     * point-sized hard-shadow sun, identical to {@code beach.png}.
     */
    private static final boolean SOFT_SHADOWS = false;
    /** Sun disk radius used when {@link #SOFT_SHADOWS} is on — larger = softer, wider penumbra. */
    private static final double SUN_RADIUS = 200d;
    /** Shadow-ray samples used when {@link #SOFT_SHADOWS} is on — larger = less noise, slower. */
    private static final int SUN_RAYS = 100;
    /**
     * Master on/off switch for the BVH acceleration. The rendered image is
     * identical either way — this only changes speed, so flip it to {@code false}
     * to time the slow brute-force render and measure the speedup.
     */
    private static final boolean USE_BVH = true;

    /**
     * Rendering strategy — the choice between the single-threaded baseline and
     * the two parallel methods is made here, from the test. {@code THREADS} and
     * {@code STREAM} both multithread; {@code SINGLE} is the baseline for timing.
     */
    private static final Camera.RenderMode RENDER_MODE = Camera.RenderMode.THREADS;

    @Test
    void renderBeach2() {
        Scene scene = new Scene("Beach (angled)")
                .setBackground(new Color(250, 168, 112))                // warm sunset sky
                .setAmbientLight(new AmbientLight(new Color(10, 8, 7)));   // low ambient → darker shadows

        BeachScene.addEnvironment(scene);   // sand, sea, sun
        // Beach ball and mirror ball pulled closer together so their shadows
        // overlap — gives the soft-shadow demo a clear merged-penumbra region.
        // Glass orb brought in left of the beach ball so it stays in frame from
        // this off-axis camera, forming a refraction -> diffuse -> reflection
        // showcase across the three foreground spheres.
        BeachScene.addProps(scene,
                new Point(10, -45, -25),    // red beach ball (diffuse)
                new Point(45, -42, -35),    // mirror ball (reflection)
                new Point(-35, -42, 0));    // glass orb (refraction)
        BeachScene.addSailboat(scene, new Point(20, -58, -400));   // out on the open water
        BeachScene.addBackground(scene);                           // distant lighthouse & islets (DOF subjects)

        // Left palm brought in toward the centre so it reads clearly from this
        // off-axis (camera-right) viewpoint; right palm unchanged.
        PalmTreeScene.addPalmTree(scene, new Point(-40, -59, -95), new Random(7));
        PalmTreeScene.addPalmTree(scene, new Point(120, -59, -160), new Random(21));

        BeachScene.addLounger(scene, new Point(78, -60, -35), 0.7);   // striped lounger, right of the mirror ball
        BeachScene.addLounger(scene, new Point(110, -60, -70), 0.78);   // second lounger, out on the open sand to the right

        // Soft shadows: when SOFT_SHADOWS is on the low sun becomes an area
        // light, so the props' shadows gain wide penumbrae and merge in the
        // overlap region the props above were arranged for. Flip SOFT_SHADOWS to
        // false for the point-sized hard sun (identical to beach.png) — the two
        // views then double as a hard-vs-soft comparison.
        if (SOFT_SHADOWS)
            BeachScene.addLights(scene, SUN_RADIUS, SUN_RAYS);
        else
            BeachScene.addLights(scene);

        // Reorganize the scene into a Bounding Volume Hierarchy so each ray skips
        // whole groups of geometry whose box it misses — same image, far faster.
        if (USE_BVH)
            scene.geometries.buildBVH();

        // Camera moved to the right and rotated back toward the scene centre.
        // Compared with the original (location (0,35,300) looking straight down
        // -z), this off-axis viewpoint lets us see the sides of the props and
        // the depth of the shoreline, for a fuller 3D feel.
        Camera.getBuilder()
                .setLocation(new Point(185, 45, 285))
                .setVpDistance(350)
                .setVpSize(280, 170)
                .setDirection(new Point(-15, -20, -340), Vector.AXIS_Y)
                .setResolution(1200, 728)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setRenderMode(RENDER_MODE)
                .build()
                .renderImage()
                .writeToImage("beach2");

        System.out.println("Rendered beach2.png to images/");
    }
}
