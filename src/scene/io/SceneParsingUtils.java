package scene.io;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import geometries.api.Geometry;
import geometries.api.Intersectable;
import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.LightSource;
import lighting.PointLight;
import lighting.SpotLight;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Shared conversion helpers used by XML/JSON scene parsers.
 */
final class SceneParsingUtils {
   private SceneParsingUtils() {}

   static Color toColor(Object value) {
      if (value == null) return Color.BLACK;
      if (value instanceof String text) {
         double[] parts = parseDoubleArray(text, 3, "color");
         return new Color(parts[0], parts[1], parts[2]);
      }
      if (value instanceof List<?> list && list.size() == 3) {
         return new Color(toDouble(list.get(0), "color[0]"), toDouble(list.get(1), "color[1]"), toDouble(list.get(2), "color[2]"));
      }
      if (value instanceof Map<?, ?> map) {
         Map<String, Object> normalized = normalizeMap(map, "color");
         Object wrapped = firstNonNull(firstNonNull(normalized.get("color"), normalized.get("intensity")), normalized.get("value"));
         if (wrapped != null) {
            return toColor(wrapped);
         }
         return new Color(
            toDouble(normalized.get("r"), "color.r"),
            toDouble(normalized.get("g"), "color.g"),
            toDouble(normalized.get("b"), "color.b")
         );
      }
      throw new IllegalArgumentException("Unsupported color value: " + value);
   }

   static AmbientLight toAmbientLight(Object value) {
      if (value == null) return AmbientLight.NONE;
      if (value instanceof String || value instanceof List<?> || value instanceof Map<?, ?>) {
         if (value instanceof Map<?, ?> map) {
            Map<String, Object> normalized = normalizeMap(map, "ambient-light");
            Object color = firstNonNull(normalized.get("color"), normalized.get("intensity"));
            if (color == null) color = normalized;
            return new AmbientLight(toColor(color));
         }
         return new AmbientLight(toColor(value));
      }
      throw new IllegalArgumentException("Unsupported ambient light value: " + value);
   }

   static Point toPoint(Object value, String label) {
      if (value instanceof String text) {
         double[] parts = parseDoubleArray(text, 3, label);
         return new Point(parts[0], parts[1], parts[2]);
      }
      if (value instanceof List<?> list && list.size() == 3) {
         return new Point(toDouble(list.get(0), label + "[0]"), toDouble(list.get(1), label + "[1]"), toDouble(list.get(2), label + "[2]"));
      }
      if (value instanceof Map<?, ?> map) {
         Map<String, Object> normalized = normalizeMap(map, label);
         return new Point(
            toDouble(normalized.get("x"), label + ".x"),
            toDouble(normalized.get("y"), label + ".y"),
            toDouble(normalized.get("z"), label + ".z")
         );
      }
      throw new IllegalArgumentException("Unsupported point value for " + label + ": " + value);
   }

   static Vector toVector(Object value, String label) {
      if (value instanceof String text) {
         double[] parts = parseDoubleArray(text, 3, label);
         return new Vector(parts[0], parts[1], parts[2]);
      }
      if (value instanceof List<?> list && list.size() == 3) {
         return new Vector(
            toDouble(list.get(0), label + "[0]"),
            toDouble(list.get(1), label + "[1]"),
            toDouble(list.get(2), label + "[2]")
         );
      }
      if (value instanceof Map<?, ?> map) {
         Map<String, Object> normalized = normalizeMap(map, label);
         return new Vector(
            toDouble(normalized.get("x"), label + ".x"),
            toDouble(normalized.get("y"), label + ".y"),
            toDouble(normalized.get("z"), label + ".z")
         );
      }
      throw new IllegalArgumentException("Unsupported vector value for " + label + ": " + value);
   }

   static Intersectable geometryFromMap(String geometryType, Map<String, Object> rawProps) {
      String type = geometryType.trim().toLowerCase();
      Map<String, Object> props = normalizeMap(rawProps, type);
      Geometry geometry = switch (type) {
         case "sphere" -> new Sphere(toPoint(require(props, "center", type), type + ".center"), toDouble(require(props, "radius", type), type + ".radius"));
         case "triangle" -> new Triangle(
            toPoint(require(props, "p0", type), type + ".p0"),
            toPoint(require(props, "p1", type), type + ".p1"),
            toPoint(require(props, "p2", type), type + ".p2")
         );
         case "plane" -> createPlane(props, type);
         case "polygon" -> createPolygon(props, type);
         case "tube" -> new Tube(toDouble(require(props, "radius", type), type + ".radius"), createAxisRay(props, type));
         case "cylinder" -> new Cylinder(
            toDouble(require(props, "radius", type), type + ".radius"),
            createAxisRay(props, type),
            toDouble(require(props, "height", type), type + ".height")
         );
         default -> throw new IllegalArgumentException("Unsupported geometry type: " + geometryType);
      };

      applyGeometryAppearance(geometry, props);
      return geometry;
   }

   static LightSource lightFromMap(String lightType, Map<String, Object> rawProps) {
      String type = lightType.trim().toLowerCase();
      Map<String, Object> props = normalizeMap(rawProps, type + "-light");

      Object colorObj = firstNonNull(firstNonNull(props.get("color"), props.get("intensity")), props.get("value"));
      Color intensity = colorObj == null ? Color.BLACK : toColor(colorObj);

      return switch (type) {
         case "directional", "directional-light" -> {
            Object direction = firstNonNull(props.get("direction"), props.get("dir"));
            if (direction == null) {
               throw new IllegalArgumentException("Missing direction for directional light");
            }
            yield new DirectionalLight(intensity, toVector(direction, type + ".direction"));
         }
         case "point", "point-light" -> {
            Object position = firstNonNull(firstNonNull(props.get("position"), props.get("point")), props.get("p"));
            if (position == null) {
               throw new IllegalArgumentException("Missing position for point light");
            }
            PointLight light = new PointLight(intensity, toPoint(position, type + ".position"));
            applyPointLightCoefficients(light, props, type);
            yield light;
         }
         case "spot", "spot-light", "spotlight" -> {
            Object position = firstNonNull(firstNonNull(props.get("position"), props.get("point")), props.get("p"));
            Object direction = firstNonNull(props.get("direction"), props.get("dir"));
            if (position == null || direction == null) {
               throw new IllegalArgumentException("Missing position/direction for spot light");
            }

            SpotLight light = new SpotLight(
               intensity,
               toPoint(position, type + ".position"),
               toVector(direction, type + ".direction")
            );
            applyPointLightCoefficients(light, props, type);

            Object narrowBeam = firstNonNull(
               firstNonNull(firstNonNull(props.get("narrowBeam"), props.get("narrow-beam")), props.get("narrow_beam")),
               props.get("beam")
            );
            if (narrowBeam != null) {
               light.setNarrowBeam(toDouble(narrowBeam, type + ".narrowBeam"));
            }
            yield light;
         }
         default -> throw new IllegalArgumentException("Unsupported light type: " + lightType);
      };
   }

   static Double3 toDouble3(Object value, String label) {
      if (value instanceof Number n) return new Double3(n.doubleValue());
      if (value instanceof String s) {
         String[] parts = s.trim().split("\\s+");
         if (parts.length == 1) {
            return new Double3(Double.parseDouble(parts[0]));
         }
         double[] triad = parseDoubleArray(s, 3, label);
         return new Double3(triad[0], triad[1], triad[2]);
      }
      if (value instanceof List<?> list) {
         if (list.size() == 1) {
            return new Double3(toDouble(list.get(0), label + "[0]"));
         }
         if (list.size() == 3) {
            return new Double3(
               toDouble(list.get(0), label + "[0]"),
               toDouble(list.get(1), label + "[1]"),
               toDouble(list.get(2), label + "[2]")
            );
         }
      }
      if (value instanceof Map<?, ?> map) {
         Map<String, Object> normalized = normalizeMap(map, label);
         if (normalized.containsKey("x") || normalized.containsKey("y") || normalized.containsKey("z")) {
            return new Double3(
               toDouble(require(normalized, "x", label), label + ".x"),
               toDouble(require(normalized, "y", label), label + ".y"),
               toDouble(require(normalized, "z", label), label + ".z")
            );
         }
         if (normalized.containsKey("r") || normalized.containsKey("g") || normalized.containsKey("b")) {
            return new Double3(
               toDouble(require(normalized, "r", label), label + ".r"),
               toDouble(require(normalized, "g", label), label + ".g"),
               toDouble(require(normalized, "b", label), label + ".b")
            );
         }
         if (normalized.containsKey("value")) {
            return new Double3(toDouble(normalized.get("value"), label + ".value"));
         }
      }

      throw new IllegalArgumentException("Unsupported Double3 value for " + label + ": " + value);
   }

   static double toDouble(Object value, String label) {
      if (value instanceof Number n) return n.doubleValue();
      if (value instanceof String s) return Double.parseDouble(s.trim());
      throw new IllegalArgumentException("Expected numeric value for " + label + ", got: " + value);
   }

   static Map<String, Object> normalizeMap(Map<?, ?> source, String label) {
      try {
         @SuppressWarnings("unchecked")
         Map<String, Object> casted = (Map<String, Object>) source;
         return casted;
      } catch (ClassCastException ex) {
         throw new IllegalArgumentException("Expected string-key map for " + label, ex);
      }
   }

   static Object require(Map<String, Object> props, String key, String typeLabel) {
      if (!props.containsKey(key)) {
         throw new IllegalArgumentException("Missing property '" + key + "' for " + typeLabel);
      }
      return props.get(key);
   }

   static Object firstNonNull(Object first, Object second) {
      return first != null ? first : second;
   }

   private static void applyGeometryAppearance(Geometry geometry, Map<String, Object> props) {
      Object emission = firstNonNull(
         firstNonNull(props.get("emission"), props.get("emission-color")),
         props.get("emissionColor")
      );
      if (emission != null) {
         geometry.setEmission(toColor(emission));
      }

      Material material = new Material();
      boolean hasMaterial = false;

      Object materialObj = props.get("material");
      Map<String, Object> materialMap = materialObj instanceof Map<?, ?> map ? normalizeMap(map, "material") : Map.of();

      Object kA = firstNonNull(findFirst(props, "kA", "ka", "k-a", "k_a"), findFirst(materialMap, "kA", "ka", "k-a", "k_a"));
      Object kD = firstNonNull(findFirst(props, "kD", "kd", "k-d", "k_d"), findFirst(materialMap, "kD", "kd", "k-d", "k_d"));
      Object kS = firstNonNull(findFirst(props, "kS", "ks", "k-s", "k_s"), findFirst(materialMap, "kS", "ks", "k-s", "k_s"));
      Object shininess = firstNonNull(
         findFirst(props, "nShininess", "n-shininess", "n_shininess", "shininess", "n"),
         findFirst(materialMap, "nShininess", "n-shininess", "n_shininess", "shininess", "n")
      );

      if (kA != null) {
         material.setKA(toDouble3(kA, "material.kA"));
         hasMaterial = true;
      }
      if (kD != null) {
         material.setKD(toDouble3(kD, "material.kD"));
         hasMaterial = true;
      }
      if (kS != null) {
         material.setKS(toDouble3(kS, "material.kS"));
         hasMaterial = true;
      }
      if (shininess != null) {
         material.setShininess((int) Math.round(toDouble(shininess, "material.shininess")));
         hasMaterial = true;
      }

      if (hasMaterial) {
         geometry.setMaterial(material);
      }
   }

   private static Object findFirst(Map<String, Object> map, String... keys) {
      for (String key : keys) {
         if (map.containsKey(key)) {
            return map.get(key);
         }
      }
      return null;
   }

   private static void applyPointLightCoefficients(PointLight light, Map<String, Object> props, String type) {
      Object kC = findFirst(props, "kC", "kc", "k-c", "k_c");
      Object kL = findFirst(props, "kL", "kl", "k-l", "k_l");
      Object kQ = findFirst(props, "kQ", "kq", "k-q", "k_q");

      if (kC != null) light.setKc(toDouble(kC, type + ".kC"));
      if (kL != null) light.setKl(toDouble(kL, type + ".kL"));
      if (kQ != null) light.setKq(toDouble(kQ, type + ".kQ"));
   }

   private static Plane createPlane(Map<String, Object> props, String type) {
      Object p = props.get("p");
      Object normal = props.get("normal");
      if (p != null && normal != null) {
         return new Plane(toPoint(p, type + ".p"), toVector(normal, type + ".normal"));
      }
      return new Plane(
         toPoint(require(props, "p0", type), type + ".p0"),
         toPoint(require(props, "p1", type), type + ".p1"),
         toPoint(require(props, "p2", type), type + ".p2")
      );
   }

   private static Polygon createPolygon(Map<String, Object> props, String type) {
      List<Point> points = new ArrayList<>();
      Object pointsObj = props.get("points");
      if (pointsObj instanceof List<?> list) {
         for (Object value : list) {
            points.add(toPoint(value, type + ".points"));
         }
      } else {
         props.entrySet().stream()
            .filter(e -> e.getKey().matches("p\\d+"))
            .sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey().substring(1))))
            .forEach(e -> points.add(toPoint(e.getValue(), type + "." + e.getKey())));
      }

      if (points.size() < 3) {
         throw new IllegalArgumentException("Polygon requires at least 3 points");
      }
      return new Polygon(points.toArray(new Point[0]));
   }

   private static Ray createAxisRay(Map<String, Object> props, String type) {
      Object origin = firstNonNull(props.get("axis-origin"), props.get("axisOrigin"));
      Object direction = firstNonNull(props.get("axis-direction"), props.get("axisDirection"));
      if (origin == null || direction == null) {
         Object axisObj = props.get("axis");
         if (axisObj instanceof Map<?, ?> map) {
            Map<String, Object> axis = normalizeMap(map, type + ".axis");
            origin = firstNonNull(axis.get("origin"), axis.get("p0"));
            direction = firstNonNull(axis.get("direction"), axis.get("dir"));
         }
      }
      if (origin == null || direction == null) {
         throw new IllegalArgumentException("Missing axis definition for " + type);
      }
      return new Ray(toPoint(origin, type + ".axis.origin"), toVector(direction, type + ".axis.direction"));
   }

   private static double[] parseDoubleArray(String raw, int expectedCount, String label) {
      String[] parts = raw.trim().split("\\s+");
      if (parts.length != expectedCount) {
         throw new IllegalArgumentException("Expected " + expectedCount + " values for " + label + ", got: " + raw);
      }

      double[] values = new double[expectedCount];
      for (int i = 0; i < expectedCount; i++) {
         values[i] = Double.parseDouble(parts[i]);
      }
      return values;
   }
}
