package scene.io;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import geometries.api.Intersectable;
import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import lighting.AmbientLight;
import primitives.Color;
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
      return switch (type) {
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
