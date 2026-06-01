package manual;

import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.PointLight;
import lighting.SpotLight;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import renderer.Camera;
import renderer.RayTracerType;
import scene.Scene;

/**
 * Compact demonstration scene combining several bodies and effects:
 * - reflective metal cylinder
 * - transparent glass triangle (flat)
 * - diffuse colored sphere
 * - ground plane
 * Shows reflection, transparency, specular highlights and shadows.
 * NOTE: no closed object is both transparent and reflective to avoid trapped rays.
 */
public class CombinedEffectsScene {
    public static void main(String[] args) {
        Scene scene = new Scene("Combined effects")
            .setBackground(new Color(12, 12, 20))
            .setAmbientLight(new AmbientLight(new Color(6, 6, 8)));

        // Ground plane
        Plane ground = new Plane(new Point(0, -30, -260), new Vector(0, 1, 0));
        Material groundMat = new Material();
        groundMat.setKD(new Double3(0.6));
        groundMat.setKS(new Double3(0.1));
        groundMat.setShininess(20);
        ground.setEmission(new Color(30, 20, 18));
        ground.setMaterial(groundMat);

        // Reflective metal cylinder (reflective only)
        Cylinder metal = new Cylinder(30, new primitives.Ray(new Point(-40, -10, -200), Vector.AXIS_Z), 80);
        Material metalMat = new Material();
        metalMat.setKD(new Double3(0.08));
        metalMat.setKS(new Double3(0.9));
        metalMat.setKR(new Double3(0.8)); // strong reflection
        metalMat.setKT(Double3.ZERO); // explicitly no transparency
        metalMat.setShininess(250);
        metal.setMaterial(metalMat);
        metal.setEmission(new Color(20,20,22));

        // Transparent flat glass (triangle) - flat geometry so transparency is safe
        Triangle glass = new Triangle(new Point(20, 10, -180), new Point(90, 10, -240), new Point(60, 60, -220));
        Material glassMat = new Material();
        glassMat.setKD(new Double3(0.02));
        glassMat.setKS(new Double3(0.2));
        glassMat.setKT(new Double3(0.92, 0.94, 0.98)); // transparent glass-like
        glassMat.setKR(Double3.ZERO); // no reflection on the flat panel (avoids closed reflective/transparency)
        glassMat.setShininess(150);
        glass.setMaterial(glassMat);
        glass.setEmission(new Color(0,0,0));

        // Diffuse colored sphere with glossy specular highlights
        Sphere ball = new Sphere(new Point(40, -8, -220), 18);
        Material ballMat = new Material();
        ballMat.setKD(new Double3(0.85, 0.1, 0.1)); // reddish diffuse
        ballMat.setKS(new Double3(0.25));
        ballMat.setKR(Double3.ZERO);
        ballMat.setKT(Double3.ZERO);
        ballMat.setShininess(60);
        ball.setMaterial(ballMat);
        ball.setEmission(new Color(0,0,0));

        // Small decorative sphere (matte)
        Sphere pebble = new Sphere(new Point(-10, -12, -210), 6);
        pebble.setMaterial(new Material().setKD(0.9).setKS(0.05).setShininess(8));
        pebble.setEmission(new Color(90, 80, 30));

        // Lights: one warm spot for strong highlights and several low-intensity points to emulate a soft area light
        scene.lights.add(new SpotLight(new Color(220, 180, 140), new Point(-150, 150, 300), new Vector(1, -1, -1)).setKl(1E-5).setKq(1.5E-7));
        // soft, distributed fill lights for softer shadows
        scene.lights.add(new PointLight(new Color(90, 100, 120), new Point(50, 120, 220)).setKl(1E-6).setKq(1.2E-7));
        scene.lights.add(new PointLight(new Color(90, 100, 120), new Point(-50, 100, 220)).setKl(1E-6).setKq(1.2E-7));
        scene.lights.add(new PointLight(new Color(70, 80, 100), new Point(0, 120, 180)).setKl(1E-6).setKq(1.2E-7));

        // Small reflective tray to add dynamic reflections under the objects
        Cylinder tray = new Cylinder(35, new primitives.Ray(new Point(20, -30, -210), Vector.AXIS_Z), 4);
        Material trayMat = new Material();
        trayMat.setKD(new Double3(0.1));
        trayMat.setKS(new Double3(0.7));
        trayMat.setKR(new Double3(0.5));
        trayMat.setShininess(220);
        tray.setMaterial(trayMat);
        tray.setEmission(new Color(20,20,22));

        // Emissive lamp sphere (visible light source) to add warmth and a visible glow
        Sphere lamp = new Sphere(new Point(0, 80, -200), 6);
        lamp.setEmission(new Color(240, 180, 120));
        lamp.setMaterial(new Material().setKD(0.0).setKS(0.0).setShininess(1));

        scene.geometries.add(ground, metal, glass, ball, pebble, tray, lamp);

        // Camera configuration: slightly closer and lower for a more engaging perspective
        Camera.getBuilder()
            .setLocation(new Point(20, 30, 650))
            .setVpDistance(700)
            .setVpSize(320, 220)
            .setDirection(new Point(0, 0, -200), Vector.AXIS_Y)
            .setResolution(1000, 700)
            .setRayTracer(scene, RayTracerType.SIMPLE)
            .build()
            .renderImage()
            .writeToImage("combined_effects_lively");

        System.out.println("Rendered combined_effects_lively.png to images/");
    }
}
