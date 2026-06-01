package manual;

import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Tube;
import geometries.impl.Triangle;
import geometries.impl.Polygon;
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
 * Stage 8 demonstration: one of each concrete geometry implementation.
 * Shows good shading, lighting, shadows, reflections and transparency.
 */
public class Stage8AllGeometriesScene {
    public static void main(String[] args) {
        Scene scene = new Scene("Stage8 - All Geometries")
            .setBackground(new Color(10, 10, 20))
            .setAmbientLight(new AmbientLight(new Color(6, 6, 8)));

        // Ground plane (neutral)
        Plane ground = new Plane(new Point(0, -60, -220), new Vector(0, 1, 0));
        ground.setEmission(new Color(40, 40, 36));
        ground.setMaterial(new Material().setKD(0.6).setKS(0.12).setShininess(30));

        // Arrange geometries in a circle with rainbow colors
        Point center = new Point(0, -20, -220);
        int n = 7;
        double radius = 60;
        Color[] colors = new Color[] {
            new Color(255, 0, 0),     // Red
            new Color(255, 127, 0),   // Orange
            new Color(255, 215, 0),   // Yellow
            new Color(0, 180, 60),    // Green
            new Color(30, 144, 255),  // Blue
            new Color(75, 0, 130),    // Indigo
            new Color(128, 0, 128)    // Violet/Purple
        };

        // Cylinder (index 0) - red, reflective metal
        double ang0 = 0 * 2 * Math.PI / n;
        Point p0 = center.add(new Vector(radius * Math.cos(ang0), -20, radius * Math.sin(ang0)));
        Cylinder metal = new Cylinder(18, new Ray(p0, Vector.AXIS_Z), 70);
        metal.setEmission(colors[0]);
        metal.setMaterial(new Material().setKD(0.06).setKS(0.9).setKR(0.7).setShininess(220));

        // Tube (index 1) - orange
        double ang1 = 1 * 2 * Math.PI / n;
        Point p1 = center.add(new Vector(radius * Math.cos(ang1), -20, radius * Math.sin(ang1)));
        Tube pipe = new Tube(8, new Ray(p1, Vector.AXIS_Y));
        pipe.setEmission(colors[1]);
        pipe.setMaterial(new Material().setKD(0.2).setKS(0.6).setKR(0.25).setShininess(120));

        // Polygon board (index 2) - yellow
        double ang2 = 2 * 2 * Math.PI / n;
        Point pc2 = center.add(new Vector(radius * Math.cos(ang2), 10, radius * Math.sin(ang2)));
        Polygon board = new Polygon(
            pc2.add(new Vector(-12, 0, -6)),
            pc2.add(new Vector(12, 0, -6)),
            pc2.add(new Vector(18, 18, 6)),
            pc2.add(new Vector(-18, 18, 6))
        );
        board.setEmission(colors[2]);
        board.setMaterial(new Material().setKD(0.9).setKS(0.12).setShininess(15));

        // Triangle glass (index 3) - green tint
        double ang3 = 3 * 2 * Math.PI / n;
        Point pc3 = center.add(new Vector(radius * Math.cos(ang3), 10, radius * Math.sin(ang3)));
        Triangle glass = new Triangle(
            pc3.add(new Vector(-10, 0, -6)),
            pc3.add(new Vector(20, 6, 0)),
            pc3.add(new Vector(0, 28, 6))
        );
        glass.setMaterial(new Material().setKD(0.03).setKS(0.35).setKT(new Double3(0.6, 0.75, 0.6)).setShininess(140));
        glass.setEmission(colors[3]);

        // Sphere orb (index 4) - blue transparent
        double ang4 = 4 * 2 * Math.PI / n;
        Point p4 = center.add(new Vector(radius * Math.cos(ang4), 8, radius * Math.sin(ang4)));
        Sphere orb = new Sphere(p4, 16);
        orb.setMaterial(new Material().setKD(0.04).setKS(0.3).setKT(new Double3(0.9, 0.9, 1.0)).setKR(Double3.ZERO).setShininess(160));
        orb.setEmission(colors[4]);

        // Small matte sphere pebble (index 5) - indigo
        double ang5 = 5 * 2 * Math.PI / n;
        Point p5 = center.add(new Vector(radius * Math.cos(ang5), -16, radius * Math.sin(ang5)));
        Sphere pebble = new Sphere(p5, 5);
        pebble.setEmission(colors[5]);
        pebble.setMaterial(new Material().setKD(0.95).setKS(0.06).setShininess(10));

        // Lamp (index 6) - purple/violet emissive
        double ang6 = 6 * 2 * Math.PI / n;
        Point p6 = center.add(new Vector(radius * Math.cos(ang6), 80, radius * Math.sin(ang6)));
        Sphere lamp = new Sphere(p6, 6);
        lamp.setEmission(colors[6]);
        lamp.setMaterial(new Material().setKD(0.0).setKS(0.0).setShininess(1));

        // Lights: strong spot + soft fill points
        scene.lights.add(new SpotLight(new Color(220, 200, 160), new Point(-160, 140, 300), new Vector(1, -1, -1)).setKl(1E-5).setKq(1.5E-7));
        scene.lights.add(new PointLight(new Color(90, 100, 120), new Point(120, 80, 220)).setKl(1E-6).setKq(1.2E-7));
        scene.lights.add(new PointLight(new Color(70, 80, 100), new Point(-40, 100, 200)).setKl(1E-6).setKq(1.2E-7));

        // Additional bodies to meet bonus (no object both reflective and transparent)
        // Reflective tray at center
        Cylinder tray = new Cylinder(40, new Ray(center.add(new Vector(0, -44, 0)), Vector.AXIS_Z), 4);
        tray.setEmission(new Color(40,40,45));
        tray.setMaterial(new Material().setKD(0.08).setKS(0.8).setKR(0.6).setShininess(200));

        // Extra glossy sphere (yellow) between indices 1 and 2
        double angExtra1 = 2.5 * 2 * Math.PI / n;
        Point pExtra1 = center.add(new Vector(radius * Math.cos(angExtra1), -8, radius * Math.sin(angExtra1)));
        Sphere glossy = new Sphere(pExtra1, 8);
        glossy.setEmission(new Color(255, 215, 0));
        glossy.setMaterial(new Material().setKD(0.3).setKS(0.7).setKR(Double3.ZERO).setShininess(180));

        // Small transparent bubble (cyan) placed near orb
        double angExtra2 = 4.5 * 2 * Math.PI / n;
        Point pExtra2 = center.add(new Vector((radius-18) * Math.cos(angExtra2), -6, (radius-18) * Math.sin(angExtra2)));
        Sphere bubble = new Sphere(pExtra2, 6);
        bubble.setMaterial(new Material().setKD(0.02).setKS(0.25).setKT(new Double3(0.85,0.9,0.95)).setKR(Double3.ZERO).setShininess(140));
        bubble.setEmission(new Color(160,200,220));

        // Another small triangle (opaque purple) for variety
        double angExtra3 = 1.5 * 2 * Math.PI / n;
        Point pcE3 = center.add(new Vector(radius * Math.cos(angExtra3), 0, radius * Math.sin(angExtra3)));
        Triangle tri2 = new Triangle(
            pcE3.add(new Vector(-6, 0, -4)),
            pcE3.add(new Vector(12, 6, 2)),
            pcE3.add(new Vector(-2, 18, 6))
        );
        tri2.setEmission(new Color(180, 90, 200));
        tri2.setMaterial(new Material().setKD(0.6).setKS(0.2).setShininess(40));

        // Add everything — now >= 12 bodies
        scene.geometries.add(ground, metal, pipe, board, glass, orb, pebble, lamp, tray, glossy, bubble, tri2);

        // Camera: three-quarter angle to show depth and interactions
        Camera.getBuilder()
            .setLocation(new Point(120, 40, 700))
            .setVpDistance(800)
            .setVpSize(420, 280)
            .setDirection(new Point(0, 0, -220), Vector.AXIS_Y)
            .setResolution(1200, 800)
            .setRayTracer(scene, RayTracerType.SIMPLE)
            .build()
            .renderImage()
            .writeToImage("stage8_all_geometries");

        System.out.println("Rendered stage8_all_geometries.png to images/");
    }
}
