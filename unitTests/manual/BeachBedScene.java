package manual;

import org.junit.jupiter.api.Test;

import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.PointLight;
import lighting.SpotLight;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import renderer.Camera;
import renderer.RayTracerType;
import scene.Scene;

/**
 * A standalone showcase scene — unrelated to {@link BeachScene} — of a single
 * piece of beach furniture: a wooden folding beach lounger ("beach bed") with
 * blue-and-white striped canvas, shaded by a big striped parasol, on warm
 * late-afternoon sand by a calm sea.
 *
 * <p>It is built entirely from this file's own geometry (no shared helpers), to
 * keep it a "completely different scene". The lounger's frame is a set of
 * {@link Cylinder} rods ({@link #rod}) and its sling is a row of striped
 * {@link Polygon} bands; the parasol pole is a {@link Cylinder} and its canopy a
 * fan of {@link Triangle} panels. The parasol stands behind the lounger so it
 * shades the bed and casts a soft shadow across it.</p>
 */
public class BeachBedScene {

    /**
     * Rendering strategy — the choice between the single-threaded baseline and
     * the two parallel methods is made here, from the test. {@code THREADS} and
     * {@code STREAM} both multithread; {@code SINGLE} is the baseline for timing.
     */
    private static final Camera.RenderMode RENDER_MODE = Camera.RenderMode.THREADS;

    @Test
    void renderBeachBed() {
        Scene scene = new Scene("Beach bed & parasol")
                .setBackground(new Color(250, 205, 160))               // warm hazy afternoon sky
                .setAmbientLight(new AmbientLight(new Color(18, 16, 14)));

        addEnvironment(scene);   // sand, sea, low sun
        addLounger(scene);
        addParasol(scene);
        addLights(scene);

        Camera.getBuilder()
                .setLocation(new Point(60, 18, 205))
                .setVpDistance(300)
                .setVpSize(280, 170)
                .setDirection(new Point(-4, -18, -26), Vector.AXIS_Y)   // 3/4 view, looking down a touch
                .setResolution(1200, 728)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setRenderMode(RENDER_MODE)
                .build()
                .renderImage()
                .writeToImage("beach_bed");

        System.out.println("Rendered beach_bed.png to images/");
    }

    /** Warm sand plane, a gently reflective sea, and a low emissive sun. */
    static void addEnvironment(Scene scene) {
        Plane sand = new Plane(new Point(0, -60, 0), new Vector(0, 1, 0));
        sand.setEmission(new Color(178, 150, 110));
        sand.setMaterial(new Material().setKD(0.62).setKS(0.1).setShininess(18));
        scene.geometries.add(sand);

        // Sea behind the furniture, reflecting the warm sky.
        Polygon sea = new Polygon(
                new Point(-40000, -59.6, -260),
                new Point(40000, -59.6, -260),
                new Point(40000, -59.6, -40000),
                new Point(-40000, -59.6, -40000));
        sea.setEmission(new Color(30, 46, 60));
        sea.setMaterial(new Material()
                .setKD(0.15).setKS(0.5).setShininess(220)
                .setKR(new Double3(0.45, 0.46, 0.44)));
        scene.geometries.add(sea);

        // Low afternoon sun, off to the right.
        Sphere sun = new Sphere(new Point(230, 95, -1200), 70);
        sun.setEmission(new Color(255, 226, 170));
        sun.setMaterial(new Material().setKD(0).setKS(0).setShininess(1));
        scene.geometries.add(sun);
    }

    /**
     * A wooden folding beach lounger: two long side rails leaning from the sand
     * up to a raised head, propped by a pair of shorter back legs (the classic
     * deck-chair cross), tied together by cross dowels, with a striped canvas
     * sling stretched head-to-foot. The sling runs head (raised, toward the sea)
     * down to the foot (lower, toward the camera).
     *
     * @param scene scene to populate
     */
    static void addLounger(Scene scene) {
        Color teak = new Color(150, 100, 58);
        Material wood = new Material().setKD(0.55).setKS(0.3).setShininess(50).setKR(0.05);
        final double sideX = 21, halfW = 20;

        // Sling edges (the canvas spans between these two parallel lines):
        //   head edge — raised, toward the sea (-z);  foot edge — lower, toward camera (+z).
        final double yHead = -38, zHead = -8;
        final double yFoot = -50, zFoot = 30;

        for (double sx : new double[]{-sideX, sideX}) {
            // Long main rail: front foot on the sand up through the sling to the
            // raised head-top (slightly above/behind the head edge).
            rod(scene, new Point(sx, -60, 58), new Point(sx, -34, -14), 1.4, teak, wood);
            // Short back leg propping the head — crosses the main rail.
            rod(scene, new Point(sx, -60, -16), new Point(sx, yHead, zHead), 1.4, teak, wood);
        }
        // Cross dowels tying the two side frames together.
        rod(scene, new Point(-sideX, yHead, zHead), new Point(sideX, yHead, zHead), 1.2, teak, wood);   // head bar
        rod(scene, new Point(-sideX, yFoot, zFoot), new Point(sideX, yFoot, zFoot), 1.2, teak, wood);   // foot bar
        rod(scene, new Point(-sideX, -60, 58), new Point(sideX, -60, 58), 1.2, teak, wood);             // front ground bar
        rod(scene, new Point(-sideX, -60, -16), new Point(sideX, -60, -16), 1.2, teak, wood);           // back ground bar

        // Striped canvas sling: vertical bands (along the slope) tiled across the
        // width, sitting just above the head/foot dowels.
        Material canvas = new Material().setKD(0.62).setKS(0.12).setShininess(16);
        Color stripeA = new Color(38, 122, 150), stripeB = new Color(236, 233, 224);
        final int bands = 8;
        for (int i = 0; i < bands; i++) {
            double xL = -halfW + 2 * halfW * i / bands;
            double xR = -halfW + 2 * halfW * (i + 1) / bands;
            Polygon band = new Polygon(
                    new Point(xL, yHead + 1.2, zHead),
                    new Point(xR, yHead + 1.2, zHead),
                    new Point(xR, yFoot + 1.2, zFoot),
                    new Point(xL, yFoot + 1.2, zFoot));
            band.setEmission(i % 2 == 0 ? stripeA : stripeB);
            band.setMaterial(canvas);
            scene.geometries.add(band);
        }
    }

    /**
     * A beach parasol standing behind the lounger: a tall thin {@link Cylinder}
     * pole topped by a domed canopy of eight alternating red/white triangular
     * panels, with a small gold finial. Positioned and sized so the canopy
     * overhangs the lounger and shades it.
     *
     * @param scene scene to populate
     */
    static void addParasol(Scene scene) {
        final double cx = -5, cz = -32;             // pole axis (behind the lounger)
        final double apexY = 47, rimY = 26, radius = 52;
        final int panels = 8;

        Cylinder pole = new Cylinder(1.1, new Ray(new Point(cx, -60, cz), Vector.AXIS_Y), 107);
        pole.setEmission(new Color(95, 70, 50));
        pole.setMaterial(new Material().setKD(0.5).setKS(0.35).setShininess(60));
        scene.geometries.add(pole);

        Point apex = new Point(cx, apexY, cz);
        Material cloth = new Material().setKD(0.55).setKS(0.18).setShininess(30);
        Color red = new Color(196, 58, 56), white = new Color(232, 226, 214);

        // Pre-compute the rim ring, then fan triangles from the apex.
        Point[] rim = new Point[panels];
        for (int i = 0; i < panels; i++) {
            double a = 2 * Math.PI * i / panels;
            rim[i] = new Point(cx + radius * Math.cos(a), rimY, cz + radius * Math.sin(a));
        }
        for (int i = 0; i < panels; i++) {
            Triangle panel = new Triangle(apex, rim[i], rim[(i + 1) % panels]);
            panel.setEmission(i % 2 == 0 ? red : white);
            panel.setMaterial(cloth);
            scene.geometries.add(panel);
        }

        Sphere finial = new Sphere(new Point(cx, apexY + 2.5, cz), 2.2);
        finial.setEmission(new Color(210, 175, 90));
        finial.setMaterial(new Material().setKD(0.3).setKS(0.6).setShininess(120).setKR(0.2));
        scene.geometries.add(finial);
    }

    /** A warm low sun (casts the parasol's shadow) plus a cool sky fill. */
    static void addLights(Scene scene) {
        scene.lights.add(new SpotLight(new Color(255, 220, 165),
                new Point(230, 95, -1200), new Vector(-0.22, -0.32, 1))
                .setKl(1E-5).setKq(2E-8));
        scene.lights.add(new PointLight(new Color(95, 100, 120),
                new Point(-220, 160, 200)).setKl(2E-4).setKq(2E-7));
        scene.lights.add(new PointLight(new Color(150, 120, 80),
                new Point(40, 110, 180)).setKl(3E-4).setKq(3E-7));
    }

    /**
     * Adds a cylindrical rod (a {@link Cylinder}) spanning two points.
     *
     * @param scene    scene to populate
     * @param p        one end of the rod (cylinder base)
     * @param q        the other end
     * @param r        rod radius
     * @param emission rod colour
     * @param mat      rod material
     */
    static void rod(Scene scene, Point p, Point q, double r, Color emission, Material mat) {
        Vector dir = q.subtract(p);
        Cylinder c = new Cylinder(r, new Ray(p, dir.normalize()), dir.length());
        c.setEmission(emission);
        c.setMaterial(mat);
        scene.geometries.add(c);
    }
}
