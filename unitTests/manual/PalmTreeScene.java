package manual;

import org.junit.jupiter.api.Test;

import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.PointLight;
import lighting.SpotLight;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import renderer.Camera;
import renderer.RayTracerType;
import scene.Scene;

import java.util.Random;

/**
 * Builds a single palm tree out of the available primitives. Both the trunk and
 * the fronds are built the same way: a chain of {@link Cylinder} segments whose
 * direction changes a little each step ({@link Sphere} joints hide the seams).
 * The trunk leans; each frond arches up-and-out then droops down.
 *
 * <p>Each frond gets a randomly chosen shade of green from a seeded
 * {@link Random}, so renders stay reproducible while every leaf differs.</p>
 *
 * <p>Colors are kept dim because the ray tracer adds {@code emission} at full
 * strength on top of lights and ambient, so bright emission clips to white. The
 * {@link #addPalmTree(Scene, Point)} helper is self-contained so a future beach
 * scene can call it several times.</p>
 */
public class PalmTreeScene {

    /** Bark brown for the trunk (dim — lights brighten it). */
    private static final Color TRUNK_COLOR = new Color(92, 58, 30);
    /** Coconut brown. */
    private static final Color COCONUT_COLOR = new Color(60, 38, 20);
    /**
     * Rendering strategy — the choice between the single-threaded baseline and
     * the two parallel methods is made here, from the test. {@code THREADS} and
     * {@code STREAM} both multithread; {@code SINGLE} is the baseline for timing.
     */
    private static final Camera.RenderMode RENDER_MODE = Camera.RenderMode.THREADS;

    @Test
    void renderPalmTree() {
        Scene scene = new Scene("Palm Tree")
                .setBackground(new Color(135, 206, 235))               // sky blue
                .setAmbientLight(new AmbientLight(new Color(12, 12, 14)));

        // Sand floor — dim emission so sun + ambient don't blow it out to white.
        Plane sand = new Plane(new Point(0, -60, 0), new Vector(0, 1, 0));
        sand.setEmission(new Color(150, 135, 95));
        sand.setMaterial(new Material().setKD(0.6).setKS(0.1).setShininess(20));
        scene.geometries.add(sand);

        // One palm tree rooted on the sand.
        addPalmTree(scene, new Point(0, -60, -120));

        // Warm sun from the upper-left + a soft cool fill so shadows aren't black.
        scene.lights.add(new SpotLight(new Color(230, 205, 160),
                new Point(-180, 220, 120), new Vector(1, -1, -1))
                .setKl(2E-4).setKq(1E-6));
        scene.lights.add(new PointLight(new Color(80, 90, 100),
                new Point(160, 120, 160)).setKl(3E-4).setKq(1.5E-6));

        Camera.getBuilder()
                .setLocation(new Point(0, 5, 150))
                .setVpDistance(300)
                .setVpSize(220, 220)
                .setDirection(new Point(0, -18, -120), Vector.AXIS_Y)
                .setResolution(800, 800)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setRenderMode(RENDER_MODE)
                .build()
                .renderImage()
                .writeToImage("palm_tree");

        System.out.println("Rendered palm_tree.png to images/");
    }

    /**
     * Adds a complete palm tree with a default color seed.
     *
     * @param scene scene to populate
     * @param base  point on the ground where the trunk starts
     */
    public static void addPalmTree(Scene scene, Point base) {
        addPalmTree(scene, base, new Random(42));
    }

    /**
     * Adds a complete palm tree to the scene with its base sitting at {@code base}.
     *
     * @param scene scene to populate
     * @param base  point on the ground where the trunk starts
     * @param rnd   source of randomness for per-frond color variation
     */
    public static void addPalmTree(Scene scene, Point base, Random rnd) {
        Material bark = new Material().setKD(0.7).setKS(0.2).setShininess(30);

        // --- Trunk: stacked cylinder segments, each leaning a touch more and a
        //     touch thinner. A bark sphere at every joint fills the wedge gap. ---
        int segments = 5;
        double segHeight = 13;
        double radius = 5;
        Point bottom = base;
        for (int i = 0; i < segments; i++) {
            Vector dir = new Vector(0.07 * i, 1, 0).normalize();   // gentle growing lean

            addJoint(scene, bottom, radius, TRUNK_COLOR, bark);
            addSegment(scene, bottom, dir, segHeight, radius, TRUNK_COLOR, bark);

            bottom = bottom.add(dir.scale(segHeight));
            radius *= 0.88;                                        // taper upward
        }

        Point crown = bottom;                                      // top of the trunk

        // Brown boss where every frond meets, to tidy the join.
        addJoint(scene, crown, 4, TRUNK_COLOR, bark);

        // --- Fronds: two rings of arched cylinder leaves, each a random green. ---
        // Upper ring: starts higher, droops less.        elev=0.55rad  droop=0.30
        addFrondRing(scene, crown, 8, 0.0, rnd, 5, 9, 0.55, 0.30);
        // Lower ring: starts flatter, droops more, staggered to fill the gaps.
        addFrondRing(scene, crown, 8, Math.PI / 8, rnd, 5, 8, 0.20, 0.45);

        // --- Coconuts: a small cluster of spheres hanging under the crown. ---
        Material coconutMat = new Material().setKD(0.6).setKS(0.3).setShininess(40);
        for (Vector off : new Vector[]{
                new Vector(4, -4, 3), new Vector(-5, -3, 2), new Vector(1, -5, -4)}) {
            Sphere coconut = new Sphere(crown.add(off), 3);
            coconut.setEmission(COCONUT_COLOR);
            coconut.setMaterial(coconutMat);
            scene.geometries.add(coconut);
        }
    }

    /**
     * Adds one ring of arched fronds radiating from the crown, each a random green.
     *
     * @param scene       scene to populate
     * @param crown       top of the trunk where fronds attach
     * @param count       number of fronds in the ring
     * @param angleOffset starting angle so rings can be staggered
     * @param rnd         source of randomness for per-frond color
     * @param segments    cylinder segments per frond (more = smoother arch)
     * @param segLen      length of each frond segment
     * @param startElev   initial upward tilt of the frond, in radians
     * @param droopPerSeg how much the frond bends downward each segment
     */
    private static void addFrondRing(Scene scene, Point crown, int count, double angleOffset,
                                     Random rnd, int segments, double segLen,
                                     double startElev, double droopPerSeg) {
        for (int i = 0; i < count; i++) {
            double angle = angleOffset + 2 * Math.PI * i / count;
            addFrond(scene, crown, angle, segments, segLen, startElev, droopPerSeg, randomGreen(rnd));
        }
    }

    /**
     * Adds a single arched frond: a central rib built as a chain of tapering
     * cylinder segments (starts angled up-and-out, then bends down each segment,
     * the same technique as the leaning trunk), lined on both sides with
     * {@link Triangle} leaflets so it reads as a feathered palm leaf.
     *
     * @param scene       scene to populate
     * @param crown       attachment point at the top of the trunk
     * @param angle       horizontal direction of the frond, in radians
     * @param segments    number of cylinder segments
     * @param segLen      length of each segment
     * @param startElev   initial upward tilt, in radians
     * @param droopPerSeg downward bend added to the direction each segment
     * @param color       this frond's green shade
     */
    private static void addFrond(Scene scene, Point crown, double angle, int segments,
                                 double segLen, double startElev, double droopPerSeg, Color color) {
        Material leaf = new Material().setKD(0.8).setKS(0.12).setShininess(20);

        double ce = Math.cos(startElev), se = Math.sin(startElev);
        Vector dir = new Vector(Math.cos(angle) * ce, se, Math.sin(angle) * ce).normalize();

        Point p = crown;
        double radius = 1.8;
        double leafLen = 9;                                         // leaflet length, tapers along the rib
        for (int s = 0; s < segments; s++) {
            addJoint(scene, p, radius, color, leaf);                // hide the seam / kink
            addSegment(scene, p, dir, segLen, radius, color, leaf);

            Point end = p.add(dir.scale(segLen));
            // Leaflet blades: base runs along the rib (p -> end), tip fans out to
            // the side, angled forward toward the rib tip and drooping slightly.
            Vector side = dir.crossProduct(Vector.AXIS_Y).normalize();
            Vector forward = dir.scale(segLen * 0.6);
            Vector droopLeaf = new Vector(0, -leafLen * 0.25, 0);
            Point tipLeft = p.add(side.scale(leafLen)).add(forward).add(droopLeaf);
            Point tipRight = p.add(side.scale(-leafLen)).add(forward).add(droopLeaf);
            addLeaflet(scene, p, end, tipLeft, color, leaf);
            addLeaflet(scene, p, end, tipRight, color, leaf);

            p = end;
            dir = dir.add(new Vector(0, -droopPerSeg, 0)).normalize();  // curve downward
            radius *= 0.72;                                         // taper toward the tip
            leafLen *= 0.8;                                         // leaflets shrink toward the tip
        }
        addJoint(scene, p, radius, color, leaf);                    // round the tip
    }

    /** Adds one triangular leaflet blade with the given color and material. */
    private static void addLeaflet(Scene scene, Point a, Point b, Point tip,
                                   Color color, Material material) {
        Triangle leaflet = new Triangle(a, b, tip);
        leaflet.setEmission(color);
        leaflet.setMaterial(material);
        scene.geometries.add(leaflet);
    }

    /** Adds one cylinder segment with the given emission color and material. */
    private static void addSegment(Scene scene, Point start, Vector dir, double length,
                                   double radius, Color color, Material material) {
        Cylinder seg = new Cylinder(radius, new Ray(start, dir), length);
        seg.setEmission(color);
        seg.setMaterial(material);
        scene.geometries.add(seg);
    }

    /** Adds a sphere that fills the joint between two segments (hides seams). */
    private static void addJoint(Scene scene, Point center, double radius,
                                 Color color, Material material) {
        Sphere joint = new Sphere(center, radius);
        joint.setEmission(color);
        joint.setMaterial(material);
        scene.geometries.add(joint);
    }

    /**
     * Picks a random shade of green: a dominant green channel with low red and
     * blue, kept dim so emission + lighting stays in range.
     *
     * @param rnd source of randomness
     * @return a green color
     */
    private static Color randomGreen(Random rnd) {
        int g = 55 + rnd.nextInt(55);   // 55..109 — dominant, darker
        int r = 10 + rnd.nextInt(28);   // 10..37
        int b = 18 + rnd.nextInt(26);   // 18..43
        return new Color(r, g, b);
    }
}
