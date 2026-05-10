package scene.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import scene.Scene;

/**
 * Entry point for scene loading from XML/JSON files.
 */
public final class SceneLoader {
   private SceneLoader() {}

   public static Scene loadFromXml(String sceneName) {
      Path source = resolveSceneFile(sceneName, "xml");
      return new XmlSceneParser().parse(source, nameWithoutExtension(source));
   }

   public static Scene loadFromJson(String sceneName) {
      Path source = resolveSceneFile(sceneName, "json");
      return new JsonSceneParser().parse(source, nameWithoutExtension(source));
   }

   public static Scene load(String filePathOrName) {
      Path source = resolveGenericPath(filePathOrName);
      if (!Files.exists(source)) {
         throw new IllegalArgumentException("Scene file was not found: " + source);
      }

      String fileName = source.getFileName().toString();
      String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() : "";
      String sceneName = nameWithoutExtension(source);

      return switch (extension) {
         case "xml" -> new XmlSceneParser().parse(source, sceneName);
         case "json" -> new JsonSceneParser().parse(source, sceneName);
         default -> throw new IllegalArgumentException("Unsupported scene file extension: " + extension);
      };
   }

   private static Path resolveSceneFile(String sceneName, String extension) {
      String fileName = sceneName.endsWith("." + extension) ? sceneName : sceneName + "." + extension;
      Path source = resolveGenericPath(fileName);
      if (!Files.exists(source)) {
         throw new IllegalArgumentException("Scene file was not found: " + source);
      }
      return source;
   }

   private static Path resolveGenericPath(String filePathOrName) {
      Path direct = Paths.get(filePathOrName);
      if (Files.exists(direct)) return direct;

      Path underSrc = Paths.get("src", filePathOrName);
      if (Files.exists(underSrc)) return underSrc;

      return underSrc;
   }

   private static String nameWithoutExtension(Path file) {
      String fileName = file.getFileName().toString();
      return fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
   }
}
