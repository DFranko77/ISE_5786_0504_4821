package manual;

import org.junit.jupiter.api.Test;

import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
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

import java.util.Random;

/**
 * Bonus showcase scene: a tropical beach that uses every implemented geometry
 * type ({@link Plane}, {@link Polygon}, {@link Sphere}, {@code Cylinder},
 * {@code Triangle} and {@link Tube}) and demonstrates every implemented effect —
 * Phong shading, reflection (sea + mirror sphere), refraction (glass orb),
 * shadows, and multiple light sources including a sun glint on the water.
 *
 * <p>Palm trees are built by {@link PalmTreeScene#addPalmTree(Scene, Point, Random)}.</p>
 */
public class BeachScene {

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
    void renderBeach() {
        Scene scene = new Scene("Beach")
                .setBackground(new Color(250, 168, 112))                // warm sunset sky
                .setAmbientLight(new AmbientLight(new Color(10, 8, 7)));   // low ambient → darker shadows

        addEnvironment(scene);   // sand, sea, sun
        addProps(scene);         // glass orb, mirror ball, beach ball, driftwood log
        addSailboat(scene, new Point(20, -58, -400));   // out on the open water
        addBackground(scene);                           // distant lighthouse & islets (DOF subjects)

        // Two palm trees with different color seeds so their leaves differ.
        PalmTreeScene.addPalmTree(scene, new Point(-95, -59, -80), new Random(7));
        PalmTreeScene.addPalmTree(scene, new Point(120, -59, -160), new Random(21));

        addLounger(scene, new Point(95, -60, -40), 0.7);   // striped lounger, right of the mirror ball
        addLounger(scene, new Point(118, -60, -72), 0.78);   // second lounger, out on the open sand to the right

        addLights(scene);

        // Reorganize the scene into a Bounding Volume Hierarchy so each ray skips
        // whole groups of geometry whose box it misses — same image, far faster.
        if (USE_BVH)
            scene.geometries.buildBVH();

        Camera.getBuilder()
                .setLocation(new Point(0, 35, 300))
                .setVpDistance(350)
                .setVpSize(280, 170)
                .setDirection(new Point(0, -18, -500), Vector.AXIS_Y)
                .setResolution(1200, 728)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setRenderMode(RENDER_MODE)
                .build()
                .renderImage()
                .writeToImage("beach");

        System.out.println("Rendered beach.png to images/");
    }

    /** Sand plane, reflective sea polygon, and the emissive sun sphere. */
    static void addEnvironment(Scene scene) {
        // Sand: infinite plane, matte and slightly speckled-warm.
        Plane sand = new Plane(new Point(0, -60, 0), new Vector(0, 1, 0));
        // Warm sand, but emission kept low so the sand leans on the sun for its
        // brightness rather than self-glowing — that contrast is what makes the
        // cast shadows visible (a brightly self-emissive sand looks the same lit
        // or shadowed). The boosted golden-hour sun (addLights) lifts the lit
        // sand back up to a warm, bright tone.
        sand.setEmission(new Color(90, 64, 42));
        sand.setMaterial(new Material().setKD(0.6).setKS(0.12).setShininess(20));
        scene.geometries.add(sand);

        // Sea: a large polygon just above the sand, so its near edge (z = -200)
        // is the shoreline, and its far edge runs out to the horizon (where it
        // meets the sky). Dim blue base + strong reflection (mirrors the sky and
        // trees) + a tight specular highlight for the sun glint.
        Polygon sea = new Polygon(
                new Point(-40000, -59.7, -200),
                new Point(40000, -59.7, -200),
                new Point(40000, -59.7, -40000),
                new Point(-40000, -59.7, -40000));
        sea.setEmission(new Color(20, 26, 40));       // dusk water
        sea.setMaterial(new Material()
                .setKD(0.12).setKS(0.7).setShininess(320)
                .setKR(new Double3(0.5, 0.5, 0.52)));   // reflects the warm sky + sun glint
        scene.geometries.add(sea);

        // Sun: a warm disk half-sunk at the horizon. Its centre sits right on the
        // sea surface (y = -60), so the opaque sea plane hides the lower hemisphere
        // and only the top half shows above the water. Pushed far out (z = -6000,
        // radius only partly scaled up) so it reads as a small, distant sun with
        // its waterline cut riding up near the true horizon. This near-horizon
        // placement is what the soft-shadow "sunset effect" will key off later.
        Sphere sun = new Sphere(new Point(60, -60, -6000), 300);
        sun.setEmission(new Color(255, 204, 132));
        sun.setMaterial(new Material().setKD(0).setKS(0).setShininess(1));
        scene.geometries.add(sun);
    }

    /**
     * Foreground props at their default positions, used by the head-on
     * {@link BeachScene} view.
     *
     * @param scene scene to populate
     */
    static void addProps(Scene scene) {
        addProps(scene, new Point(5, -45, -25), new Point(62, -42, -40), new Point(-55, -40, -15));
    }

    /**
     * Foreground props that show off refraction, reflection, and the Tube.
     * <p>The three showcase spheres (glass / beach ball / mirror) are
     * parameterized so an individual view (e.g. {@link BeachScene2}) can
     * rearrange them — for instance to make their shadows overlap for the
     * soft-shadow demo, or to keep the glass orb in frame from an off-axis
     * camera — without disturbing the other view. The driftwood log and pebbles
     * are fixed and shared by all views.</p>
     *
     * @param scene     scene to populate
     * @param ballPos   centre of the (red) beach ball
     * @param mirrorPos centre of the mirror ball
     * @param glassPos  centre of the glass orb
     */
    static void addProps(Scene scene, Point ballPos, Point mirrorPos, Point glassPos) {
        // Glass orb — transparency / refraction. The trunk and sea bend through
        // it. Pure kT, no reflection.
        Sphere glass = new Sphere(glassPos, 18);
        glass.setEmission(new Color(6, 10, 12));
        glass.setMaterial(new Material()
                .setKD(0.04).setKS(0.35).setShininess(200)
                .setKT(new Double3(0.85, 0.9, 0.92)));
        scene.geometries.add(glass);

        // Mirror ball — pure reflection. Reflects the trees, sea, and sky.
        Sphere mirror = new Sphere(mirrorPos, 18);
        mirror.setEmission(new Color(6, 6, 8));
        mirror.setMaterial(new Material()
                .setKD(0.05).setKS(0.5).setShininess(300)
                .setKR(new Double3(0.85, 0.85, 0.9)));
        scene.geometries.add(mirror);

        // Beach ball — glossy and colorful, to anchor the foreground centre.
        Sphere ball = new Sphere(ballPos, 15);
        ball.setEmission(new Color(150, 40, 45));
        ball.setMaterial(new Material()
                .setKD(0.35).setKS(0.6).setShininess(150).setKR(0.12));
        scene.geometries.add(ball);

        // Driftwood log — an (infinite) Tube lying across the beach, so it runs
        // off both sides of the frame like a real beached log.
        Tube log = new Tube(6, new Ray(new Point(0, -57, -110), Vector.AXIS_X));
        log.setEmission(new Color(70, 50, 30));
        log.setMaterial(new Material().setKD(0.7).setKS(0.2).setShininess(25));
        scene.geometries.add(log);

        // A couple of small wet pebbles near the shoreline for extra shadows.
        Sphere pebble1 = new Sphere(new Point(8, -57, -75), 4);
        pebble1.setEmission(new Color(40, 40, 45));
        pebble1.setMaterial(new Material().setKD(0.5).setKS(0.4).setShininess(60).setKR(0.2));
        Sphere pebble2 = new Sphere(new Point(-22, -57, -62), 3);
        pebble2.setEmission(new Color(55, 45, 40));
        pebble2.setMaterial(new Material().setKD(0.6).setKS(0.3).setShininess(40));
        scene.geometries.add(pebble1, pebble2);
    }

    /**
     * Adds a little sailboat resting on the water: a {@link Polygon} hull, a
     * {@link Cylinder} mast and boom, and a proper rig — a billowing mainsail
     * plus a smaller jib, each built from {@link Triangle}s — topped by a tiny
     * pennant. It reflects in the sea.
     *
     * <p>The boat runs bow-to-stern along the x-axis (bow at +x), so the
     * mainsail sits aft of the mast and the jib forward of it.</p>
     *
     * @param scene scene to populate
     * @param b     boat centre, on the water surface
     */
    static void addSailboat(Scene scene, Point b) {
        // Hull: an open-top rowing-boat / dinghy. A raised gunwale rim slants
        // down and inward to a narrower flat bottom, pointed at bow (+x) and
        // stern (-x), so the boat has real volume (a bowl) instead of a flat
        // deck. The walls are Triangles (always planar, unlike a slanted quad);
        // the bottom is a flat hexagonal Polygon and sits below the waterline,
        // so the opaque sea hides the submerged part.
        Material wood = new Material().setKD(0.6).setKS(0.3).setShininess(40).setKR(0.1);
        Color hullColor = new Color(95, 60, 35);

        // Gunwale rim, ~4 units above the waterline at b.
        Point rBow   = b.add(new Vector( 32,  4,   0));
        Point rStarF = b.add(new Vector( 14,  4,  14));
        Point rStarA = b.add(new Vector(-20,  4,  14));
        Point rStern = b.add(new Vector(-30,  4,   0));
        Point rPortA = b.add(new Vector(-20,  4, -14));
        Point rPortF = b.add(new Vector( 14,  4, -14));
        // Narrower bottom, ~5 units below the waterline.
        Point fBow   = b.add(new Vector( 24, -5,   0));
        Point fStarF = b.add(new Vector( 11, -5,   6));
        Point fStarA = b.add(new Vector(-16, -5,   6));
        Point fStern = b.add(new Vector(-22, -5,   0));
        Point fPortA = b.add(new Vector(-16, -5,  -6));
        Point fPortF = b.add(new Vector( 11, -5,  -6));

        // Six hull-wall segments around the rim (bow -> starboard -> stern ->
        // port -> bow), each a quad split into two triangles (rim edge -> bottom
        // edge).
        Point[][] segments = {
                {rBow,   rStarF, fStarF, fBow},
                {rStarF, rStarA, fStarA, fStarF},
                {rStarA, rStern, fStern, fStarA},
                {rStern, rPortA, fPortA, fStern},
                {rPortA, rPortF, fPortF, fPortA},
                {rPortF, rBow,   fBow,   fPortF},
        };
        for (Point[] s : segments) {
            for (Triangle wall : new Triangle[]{
                    new Triangle(s[0], s[1], s[2]),
                    new Triangle(s[0], s[2], s[3])}) {
                wall.setEmission(hullColor);
                wall.setMaterial(wood);
                scene.geometries.add(wall);
            }
        }

        // Flat hexagonal bottom closing the hull.
        Polygon bottom = new Polygon(fBow, fStarF, fStarA, fStern, fPortA, fPortF);
        bottom.setEmission(hullColor);
        bottom.setMaterial(wood);
        scene.geometries.add(bottom);

        // Mast: a thin cylinder rising from the deck.
        Cylinder mast = new Cylinder(1.2, new Ray(b, Vector.AXIS_Y), 64);
        mast.setEmission(new Color(80, 55, 35));
        mast.setMaterial(new Material().setKD(0.6).setKS(0.3).setShininess(40));
        scene.geometries.add(mast);

        // Boom: a short cylinder along the foot of the mainsail, mast -> clew.
        Cylinder boom = new Cylinder(0.9,
                new Ray(b.add(new Vector(2, 4, 1)), new Vector(-32, 2, 2).normalize()), 32);
        boom.setEmission(new Color(80, 55, 35));
        boom.setMaterial(new Material().setKD(0.6).setKS(0.3).setShininess(40));
        scene.geometries.add(boom);

        // Mainsail: aft of the mast, bellied toward +z so it looks wind-filled.
        addSail(scene,
                b.add(new Vector(2, 60, 1)),     // head, near masthead
                b.add(new Vector(2, 4, 1)),      // tack, at the mast foot
                b.add(new Vector(-30, 6, 3)),    // clew, aft end of the boom
                b.add(new Vector(-13, 30, 9)),   // belly, pushed out toward camera
                new Color(214, 209, 198), new Color(170, 45, 50));   // cream cloth, red regatta band

        // Jib: smaller foresail forward of the mast, bellied the same way.
        addSail(scene,
                b.add(new Vector(3, 42, 1)),     // head, partway up the mast
                b.add(new Vector(30, 5, 1)),     // tack, at the bow
                b.add(new Vector(3, 5, 1)),      // clew, by the mast foot
                b.add(new Vector(14, 22, 7)),    // belly
                new Color(204, 199, 191), new Color(40, 70, 120));   // cream cloth, navy band

        // Pennant: a tiny red flag streaming from the masthead.
        Triangle pennant = new Triangle(
                b.add(new Vector(2, 63.5, 1)),
                b.add(new Vector(2, 59, 1)),
                b.add(new Vector(15, 61.5, 1)));
        pennant.setEmission(new Color(170, 45, 50));
        pennant.setMaterial(new Material().setKD(0.7).setKS(0.2).setShininess(30));
        scene.geometries.add(pennant);
    }

    /**
     * Builds one wind-filled sail as a billowed triangle mesh.
     * <p>The sail is a triangular sheet (head at the top, tack and clew at the
     * foot). It is tessellated into a grid of small triangles and every interior
     * vertex is pushed out along the belly direction by a smooth
     * {@code sin(pi*u)*sin(pi*v)} dome, so the cloth bulges most at its centre
     * and lies flat along the spars — a far rounder billow than the old
     * 3-triangle fan. One horizontal band is painted with {@code stripe} for a
     * regatta look, and the cloth is slightly translucent ({@code kT}) so the
     * low sun glows through it.</p>
     *
     * @param scene  scene to populate
     * @param head   top corner (by the masthead)
     * @param tack   lower forward corner
     * @param clew   lower aft / outboard corner
     * @param belly  centre point pushed off the sail plane to set the billow
     * @param base   main sailcloth colour
     * @param stripe colour of the single horizontal band
     */
    private static void addSail(Scene scene, Point head, Point tack, Point clew,
                                Point belly, Color base, Color stripe) {
        final int rows = 4, cols = 4, stripeRow = 1;
        Material cloth = new Material().setKD(0.5).setKS(0.2).setShininess(30)
                .setKT(new Double3(0.22, 0.22, 0.22));
        // Billow offset: how far the belly sits off the flat sail's centroid.
        Point centroid = tack.add(head.subtract(tack).scale(1d / 3))
                .add(clew.subtract(tack).scale(1d / 3));
        Vector bulge = belly.subtract(centroid);

        for (int r = 0; r < rows; r++) {
            double vB = (double) r / rows;
            double vT = (double) (r + 1) / rows;
            Color c = r == stripeRow ? stripe : base;
            if (r == rows - 1) {                       // top row: fan up to the head
                for (int col = 0; col < cols; col++) {
                    Point a = sailPoint(head, tack, clew, bulge, (double) col / cols, vB);
                    Point d = sailPoint(head, tack, clew, bulge, (double) (col + 1) / cols, vB);
                    addCloth(scene, a, d, head, c, cloth);
                }
            } else {                                   // interior rows: quad band
                for (int col = 0; col < cols; col++) {
                    double uL = (double) col / cols, uR = (double) (col + 1) / cols;
                    Point a = sailPoint(head, tack, clew, bulge, uL, vB);
                    Point bb = sailPoint(head, tack, clew, bulge, uR, vB);
                    Point cc = sailPoint(head, tack, clew, bulge, uR, vT);
                    Point d = sailPoint(head, tack, clew, bulge, uL, vT);
                    addCloth(scene, a, bb, cc, c, cloth);
                    addCloth(scene, a, cc, d, c, cloth);
                }
            }
        }
    }

    /**
     * Maps sail parameters {@code (u,v)} to a 3D point on the billowed sheet:
     * {@code v} runs foot (0) to head (1), {@code u} runs luff (0) to leech (1).
     *
     * @param head  top corner
     * @param tack  lower forward corner
     * @param clew  lower aft corner
     * @param bulge belly offset vector applied via the dome weight
     * @param u     across-sail parameter, luff to leech
     * @param v     up-sail parameter, foot to head
     * @return the displaced sail-surface point
     */
    private static Point sailPoint(Point head, Point tack, Point clew, Vector bulge,
                                   double u, double v) {
        Point luff = lerp(tack, head, v);
        Point leech = lerp(clew, head, v);
        Point flat = lerp(luff, leech, u);
        double w = Math.sin(Math.PI * u) * Math.sin(Math.PI * v);
        return w > 1e-6 ? flat.add(bulge.scale(w)) : flat;
    }

    /**
     * Linearly interpolates between two distinct points.
     *
     * @param a start point (t = 0)
     * @param b end point (t = 1)
     * @param t interpolation factor
     * @return the interpolated point
     */
    private static Point lerp(Point a, Point b, double t) {
        if (t <= 1e-12) return a;          // scale(0) would build a zero vector
        if (t >= 1 - 1e-12) return b;
        return a.add(b.subtract(a).scale(t));
    }

    /**
     * Adds one sail triangle with the given colour and cloth material.
     *
     * @param scene scene to populate
     * @param a     first corner
     * @param b     second corner
     * @param c     third corner
     * @param color emission colour
     * @param cloth sail material
     */
    private static void addCloth(Scene scene, Point a, Point b, Point c, Color color, Material cloth) {
        Triangle t = new Triangle(a, b, c);
        t.setEmission(color);
        t.setMaterial(cloth);
        scene.geometries.add(t);
    }

    /**
     * Warm sun spotlight, a cool sky fill, and a glint light over the water,
     * with the sun kept point-sized (hard shadows) — the head-on
     * {@link BeachScene} reference look.
     *
     * @param scene scene to populate
     */
    static void addLights(Scene scene) {
        addLights(scene, 0d, 1);
    }

    /**
     * Same three lights as {@link #addLights(Scene)}, but lets a caller turn the
     * low sun into an area light for soft shadows. With {@code sunRadius == 0}
     * (or {@code sunRays <= 1}) the sun stays point-sized and the result is
     * identical to {@link #addLights(Scene)}; a positive radius gives the sun a
     * disk that is sampled with {@code sunRays} shadow rays, widening every
     * shadow it casts into a soft penumbra. The dim fill and bounce lights are
     * left point-sized so shadows keep a defined core.
     *
     * @param scene     scene to populate
     * @param sunRadius radius of the sun's emitting disk (0 = hard shadows)
     * @param sunRays   shadow-ray samples across that disk (more = smoother, slower)
     */
    static void addLights(Scene scene, double sunRadius, int sunRays) {
        // Key light — the warm sun. NOTE: this is the invisible shadow-casting
        // light, separate from the visible sun disk on the horizon (addEnvironment).
        // It used to graze the sand at ~5°, which barely lit the flat ground, so
        // cast shadows were faint. Raised to a ~25° "golden hour" angle: still a
        // low, warm, long-shadow rake (the sunset mood holds), but now it actually
        // lights the sand from above, so blocked areas drop and every cast shadow
        // reads deep and — with the area-light radius — soft. Boosted 50% so the
        // scene's brightness comes from the sun, not the fills.
        SpotLight sun = new SpotLight(new Color(255, 176, 104).scale(1.5),
                new Point(40, 320, -900), new Vector(-0.05, -0.46, 1))
                .setKl(1E-5).setKq(2E-8);
        if (sunRadius > 0d) sun.setSize(sunRadius).setNumOfRays(sunRays);
        scene.lights.add(sun);
        // Cool dusk fill from the opposite side so shadows read blue, not pure
        // black. Kept low — just enough tint, not enough to wash out the cast
        // shadows the boosted sun throws.
        scene.lights.add(new PointLight(new Color(16, 20, 32),
                new Point(-220, 150, 220)).setKl(2E-4).setKq(2E-7));
        // A nearer warm bounce, kept modest: it gives the shaded sides of the
        // props/trees some form (so they don't go flat black) but is too weak to
        // refill the shadows they cast on the sand.
        scene.lights.add(new PointLight(new Color(50, 33, 20),
                new Point(60, 85, 160)).setKl(3E-4).setKq(3E-7));
    }

    /**
     * Adds distant background scenery far out at sea — a striped lighthouse on a
     * rocky islet plus a couple of bare rock islets. These sit near the horizon
     * and are intended as out-of-focus subjects for a depth-of-field shot.
     *
     * @param scene scene to populate
     */
    static void addBackground(Scene scene) {
        addLighthouse(scene, new Point(-330, -55, -1150));
        rockIslet(scene, new Point(240, -60, -1450), 80);
        rockIslet(scene, new Point(-60, -62, -820), 26);
    }

    /**
     * A bare rock islet: a {@link Sphere} with its centre below the waterline so
     * only a cap shows above the sea.
     *
     * @param scene scene to populate
     * @param base  point on the sea surface at the islet's centre
     * @param r     islet radius
     */
    private static void rockIslet(Scene scene, Point base, double r) {
        Sphere rock = new Sphere(base.add(new Vector(0, -r * 0.7, 0)), r);
        rock.setEmission(new Color(48, 46, 44));
        rock.setMaterial(new Material().setKD(0.6).setKS(0.15).setShininess(20));
        scene.geometries.add(rock);
    }

    /**
     * A classic red-and-white striped lighthouse: a rock base, a stack of
     * alternating-colour cylinder courses, a glowing emissive lantern room, and
     * a conical red roof, with a faint warm light at the lantern.
     *
     * @param scene scene to populate
     * @param base  point at the foot of the tower, near the waterline
     */
    static void addLighthouse(Scene scene, Point base) {
        rockIslet(scene, base.add(new Vector(0, -4, 0)), 70);

        Material stone = new Material().setKD(0.6).setKS(0.2).setShininess(30);
        Color white = new Color(225, 222, 214), red = new Color(170, 55, 50);
        final double r = 17, segH = 22;
        final int courses = 6;
        for (int i = 0; i < courses; i++) {
            Point courseBase = i == 0 ? base : base.add(new Vector(0, i * segH, 0));
            Cylinder course = new Cylinder(r,
                    new Ray(courseBase, Vector.AXIS_Y), segH);
            course.setEmission(i % 2 == 0 ? white : red);
            course.setMaterial(stone);
            scene.geometries.add(course);
        }
        double topY = courses * segH;

        // Gallery deck and the emissive lantern room (the light itself).
        Cylinder gallery = new Cylinder(r + 3,
                new Ray(base.add(new Vector(0, topY, 0)), Vector.AXIS_Y), 5);
        gallery.setEmission(new Color(60, 60, 65));
        gallery.setMaterial(stone);
        scene.geometries.add(gallery);

        Cylinder lantern = new Cylinder(r - 4,
                new Ray(base.add(new Vector(0, topY + 5, 0)), Vector.AXIS_Y), 16);
        lantern.setEmission(new Color(255, 240, 175));
        lantern.setMaterial(new Material().setKD(0).setKS(0).setShininess(1));
        scene.geometries.add(lantern);

        // Conical red roof: a fan of triangles from an apex down to a rim ring.
        double roofY = topY + 21, roofR = r - 1;
        Point apex = base.add(new Vector(0, roofY + 16, 0));
        Material roofMat = new Material().setKD(0.5).setKS(0.3).setShininess(40);
        final int seg = 10;
        Point[] rim = new Point[seg];
        for (int i = 0; i < seg; i++) {
            double a = 2 * Math.PI * i / seg;
            rim[i] = base.add(new Vector(roofR * Math.cos(a), roofY, roofR * Math.sin(a)));
        }
        for (int i = 0; i < seg; i++) {
            Triangle t = new Triangle(apex, rim[i], rim[(i + 1) % seg]);
            t.setEmission(new Color(150, 45, 45));
            t.setMaterial(roofMat);
            scene.geometries.add(t);
        }

        scene.lights.add(new PointLight(new Color(120, 110, 70),
                base.add(new Vector(0, topY + 13, 0))).setKl(1E-4).setKq(1E-6));
    }

    /**
     * Adds a wooden folding beach lounger with blue-and-white striped canvas:
     * two long side rails leaning from the sand up to a raised head, propped by
     * shorter back legs (the classic deck-chair cross), tied by cross dowels,
     * with a striped sling stretched head-to-foot. Built relative to a base
     * point on the sand and uniformly scaled. The foot faces +z (toward the
     * head-on camera).
     *
     * @param scene scene to populate
     * @param b     point on the sand at the lounger's local origin (head end)
     * @param s     uniform scale factor
     */
    static void addLounger(Scene scene, Point b, double s) {
        Color teak = new Color(150, 100, 58);
        Material wood = new Material().setKD(0.55).setKS(0.3).setShininess(50).setKR(0.05);
        final double sideX = 21, halfW = 20;
        final double yHead = 22, zHead = -8;     // sling head edge (raised, toward sea)
        final double yFoot = 10, zFoot = 30;     // sling foot edge (lower, toward camera)

        for (double sx : new double[]{-sideX, sideX}) {
            rod(scene, lp(b, s, sx, 0, 58), lp(b, s, sx, 26, -14), 1.4 * s, teak, wood);     // main rail
            rod(scene, lp(b, s, sx, 0, -16), lp(b, s, sx, yHead, zHead), 1.4 * s, teak, wood); // back leg
        }
        rod(scene, lp(b, s, -sideX, yHead, zHead), lp(b, s, sideX, yHead, zHead), 1.2 * s, teak, wood);
        rod(scene, lp(b, s, -sideX, yFoot, zFoot), lp(b, s, sideX, yFoot, zFoot), 1.2 * s, teak, wood);
        rod(scene, lp(b, s, -sideX, 0, 58), lp(b, s, sideX, 0, 58), 1.2 * s, teak, wood);
        rod(scene, lp(b, s, -sideX, 0, -16), lp(b, s, sideX, 0, -16), 1.2 * s, teak, wood);

        Material canvas = new Material().setKD(0.62).setKS(0.12).setShininess(16);
        Color stripeA = new Color(38, 122, 150), stripeB = new Color(236, 233, 224);
        final int bands = 8;
        for (int i = 0; i < bands; i++) {
            double xL = -halfW + 2 * halfW * i / bands;
            double xR = -halfW + 2 * halfW * (i + 1) / bands;
            Polygon band = new Polygon(
                    lp(b, s, xL, yHead + 1.2, zHead),
                    lp(b, s, xR, yHead + 1.2, zHead),
                    lp(b, s, xR, yFoot + 1.2, zFoot),
                    lp(b, s, xL, yFoot + 1.2, zFoot));
            band.setEmission(i % 2 == 0 ? stripeA : stripeB);
            band.setMaterial(canvas);
            scene.geometries.add(band);
        }
    }

    /** Local lounger point: base {@code b} plus a scaled {@code (x,y,z)} offset. */
    private static Point lp(Point b, double s, double x, double y, double z) {
        return b.add(new Vector(x * s, y * s, z * s));
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
