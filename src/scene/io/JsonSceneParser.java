package scene.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import geometries.impl.Geometries;
import scene.Scene;

/**
 * Scene parser for JSON files.
 */
public class JsonSceneParser implements SceneParser {
   @Override
   public Scene parse(Path sourceFile, String sceneName) {
      try {
         String text = Files.readString(sourceFile);
         Object root = new SimpleJsonParser(text).parse();
         if (!(root instanceof Map<?, ?> rootMapRaw)) {
            throw new IllegalArgumentException("JSON root must be an object");
         }

         Map<String, Object> rootMap = SceneParsingUtils.normalizeMap(rootMapRaw, "json-root");
         Object sceneNode = rootMap.containsKey("scene") ? rootMap.get("scene") : rootMap;
         if (!(sceneNode instanceof Map<?, ?> sceneMapRaw)) {
            throw new IllegalArgumentException("JSON scene must be an object");
         }

         Map<String, Object> sceneMap = SceneParsingUtils.normalizeMap(sceneMapRaw, "scene");
         Scene scene = new Scene(sceneName);

         Object background = firstNonNull(sceneMap.get("background-color"), sceneMap.get("backgroundColor"));
         if (background == null) background = sceneMap.get("background");
         if (background != null) {
            scene.setBackground(SceneParsingUtils.toColor(background));
         }

         Object ambient = firstNonNull(sceneMap.get("ambient-light"), sceneMap.get("ambientLight"));
         if (ambient != null) {
            scene.setAmbientLight(SceneParsingUtils.toAmbientLight(ambient));
         }

         Geometries geometries = new Geometries();
         Object geometriesNode = sceneMap.get("geometries");
         if (geometriesNode != null) {
            parseGeometries(geometriesNode, geometries);
         }

         Object lightsNode = firstNonNull(sceneMap.get("lights"), sceneMap.get("light-sources"));
         if (lightsNode != null) {
            parseLights(lightsNode, scene);
         }

         scene.setGeometries(geometries);
         return scene;
      } catch (IOException ex) {
         throw new IllegalArgumentException("Failed reading JSON scene from " + sourceFile, ex);
      }
   }

   private static void parseGeometries(Object geometriesNode, Geometries geometries) {
      if (geometriesNode instanceof List<?> list) {
         for (Object item : list) {
            addGeometryFromItem(item, geometries);
         }
         return;
      }

      if (geometriesNode instanceof Map<?, ?> mapRaw) {
         Map<String, Object> map = SceneParsingUtils.normalizeMap(mapRaw, "geometries");
         for (Map.Entry<String, Object> entry : map.entrySet()) {
            addGeometryByType(entry.getKey(), entry.getValue(), geometries);
         }
         return;
      }

      throw new IllegalArgumentException("Unsupported geometries JSON node: " + geometriesNode);
   }

   private static void addGeometryFromItem(Object item, Geometries geometries) {
      if (!(item instanceof Map<?, ?> mapRaw)) {
         throw new IllegalArgumentException("Geometry item must be object: " + item);
      }

      Map<String, Object> map = SceneParsingUtils.normalizeMap(mapRaw, "geometry-item");
      if (map.containsKey("type")) {
         String type = String.valueOf(map.get("type"));
         Map<String, Object> props = new LinkedHashMap<>(map);
         props.remove("type");
         geometries.add(SceneParsingUtils.geometryFromMap(type, props));
         return;
      }

      if (map.size() != 1) {
         throw new IllegalArgumentException("Geometry item must define either 'type' or single-type object");
      }

      Map.Entry<String, Object> entry = map.entrySet().iterator().next();
      addGeometryByType(entry.getKey(), entry.getValue(), geometries);
   }

   private static void addGeometryByType(String type, Object value, Geometries geometries) {
      if (value instanceof List<?> list) {
         for (Object item : list) {
            if (!(item instanceof Map<?, ?> mapRaw)) {
               throw new IllegalArgumentException("Expected object geometry in list for type " + type);
            }
            geometries.add(SceneParsingUtils.geometryFromMap(type, SceneParsingUtils.normalizeMap(mapRaw, type)));
         }
         return;
      }

      if (value instanceof Map<?, ?> mapRaw) {
         geometries.add(SceneParsingUtils.geometryFromMap(type, SceneParsingUtils.normalizeMap(mapRaw, type)));
         return;
      }

      throw new IllegalArgumentException("Unsupported geometry value for type " + type + ": " + value);
   }

   private static void parseLights(Object lightsNode, Scene scene) {
      if (lightsNode instanceof List<?> list) {
         for (Object item : list) {
            addLightFromItem(item, scene);
         }
         return;
      }

      if (lightsNode instanceof Map<?, ?> mapRaw) {
         Map<String, Object> map = SceneParsingUtils.normalizeMap(mapRaw, "lights");
         for (Map.Entry<String, Object> entry : map.entrySet()) {
            addLightByType(entry.getKey(), entry.getValue(), scene);
         }
         return;
      }

      throw new IllegalArgumentException("Unsupported lights JSON node: " + lightsNode);
   }

   private static void addLightFromItem(Object item, Scene scene) {
      if (!(item instanceof Map<?, ?> mapRaw)) {
         throw new IllegalArgumentException("Light item must be object: " + item);
      }

      Map<String, Object> map = SceneParsingUtils.normalizeMap(mapRaw, "light-item");
      if (map.containsKey("type")) {
         String type = String.valueOf(map.get("type"));
         Map<String, Object> props = new LinkedHashMap<>(map);
         props.remove("type");
         scene.lights.add(SceneParsingUtils.lightFromMap(type, props));
         return;
      }

      if (map.size() != 1) {
         throw new IllegalArgumentException("Light item must define either 'type' or single-type object");
      }

      Map.Entry<String, Object> entry = map.entrySet().iterator().next();
      addLightByType(entry.getKey(), entry.getValue(), scene);
   }

   private static void addLightByType(String type, Object value, Scene scene) {
      if (value instanceof List<?> list) {
         for (Object item : list) {
            if (!(item instanceof Map<?, ?> mapRaw)) {
               throw new IllegalArgumentException("Expected object light in list for type " + type);
            }
            scene.lights.add(SceneParsingUtils.lightFromMap(type, SceneParsingUtils.normalizeMap(mapRaw, type)));
         }
         return;
      }

      if (value instanceof Map<?, ?> mapRaw) {
         scene.lights.add(SceneParsingUtils.lightFromMap(type, SceneParsingUtils.normalizeMap(mapRaw, type)));
         return;
      }

      throw new IllegalArgumentException("Unsupported light value for type " + type + ": " + value);
   }

   private static Object firstNonNull(Object first, Object second) {
      return first != null ? first : second;
   }

   /**
    * Minimal JSON parser with object/array/string/number/boolean/null support.
    */
   static final class SimpleJsonParser {
      private final String text;
      private int index;

      SimpleJsonParser(String text) {
         this.text = text;
      }

      Object parse() {
         skipWhitespace();
         Object value = parseValue();
         skipWhitespace();
         if (index != text.length()) {
            throw error("Trailing content");
         }
         return value;
      }

      private Object parseValue() {
         skipWhitespace();
         if (index >= text.length()) throw error("Unexpected end of JSON");

         char c = text.charAt(index);
         return switch (c) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 't' -> parseLiteral("true", Boolean.TRUE);
            case 'f' -> parseLiteral("false", Boolean.FALSE);
            case 'n' -> parseLiteral("null", null);
            default -> parseNumber();
         };
      }

      private Map<String, Object> parseObject() {
         expect('{');
         Map<String, Object> object = new LinkedHashMap<>();
         skipWhitespace();
         if (peek('}')) {
            expect('}');
            return object;
         }

         while (true) {
            String key = parseString();
            skipWhitespace();
            expect(':');
            Object value = parseValue();
            object.put(key, value);
            skipWhitespace();
            if (peek('}')) {
               expect('}');
               return object;
            }
            expect(',');
            skipWhitespace();
         }
      }

      private List<Object> parseArray() {
         expect('[');
         List<Object> array = new ArrayList<>();
         skipWhitespace();
         if (peek(']')) {
            expect(']');
            return array;
         }

         while (true) {
            array.add(parseValue());
            skipWhitespace();
            if (peek(']')) {
               expect(']');
               return array;
            }
            expect(',');
            skipWhitespace();
         }
      }

      private String parseString() {
         expect('"');
         StringBuilder builder = new StringBuilder();
         while (index < text.length()) {
            char c = text.charAt(index++);
            if (c == '"') return builder.toString();
            if (c != '\\') {
               builder.append(c);
               continue;
            }

            if (index >= text.length()) throw error("Invalid escape sequence");
            char esc = text.charAt(index++);
            switch (esc) {
               case '"' -> builder.append('"');
               case '\\' -> builder.append('\\');
               case '/' -> builder.append('/');
               case 'b' -> builder.append('\b');
               case 'f' -> builder.append('\f');
               case 'n' -> builder.append('\n');
               case 'r' -> builder.append('\r');
               case 't' -> builder.append('\t');
               case 'u' -> builder.append(parseUnicode());
               default -> throw error("Unsupported escape: \\" + esc);
            }
         }
         throw error("Unterminated string");
      }

      private char parseUnicode() {
         if (index + 4 > text.length()) throw error("Invalid unicode escape");
         String hex = text.substring(index, index + 4);
         index += 4;
         return (char) Integer.parseInt(hex, 16);
      }

      private Object parseNumber() {
         int start = index;
         if (peek('-')) index++;
         consumeDigits();

         if (peek('.')) {
            index++;
            consumeDigits();
         }

         if (peek('e') || peek('E')) {
            index++;
            if (peek('+') || peek('-')) index++;
            consumeDigits();
         }

         String number = text.substring(start, index);
         try {
            return Double.parseDouble(number);
         } catch (NumberFormatException ex) {
            throw error("Invalid number: " + number);
         }
      }

      private Object parseLiteral(String literal, Object value) {
         if (!text.startsWith(literal, index)) {
            throw error("Expected literal: " + literal);
         }
         index += literal.length();
         return value;
      }

      private void consumeDigits() {
         int start = index;
         while (index < text.length() && Character.isDigit(text.charAt(index))) {
            index++;
         }
         if (start == index) throw error("Expected digits");
      }

      private void expect(char expected) {
         if (index >= text.length() || text.charAt(index) != expected) {
            throw error("Expected '" + expected + "'");
         }
         index++;
      }

      private boolean peek(char c) {
         return index < text.length() && text.charAt(index) == c;
      }

      private void skipWhitespace() {
         while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
         }
      }

      private IllegalArgumentException error(String message) {
         return new IllegalArgumentException(message + " at index " + index);
      }
   }
}
