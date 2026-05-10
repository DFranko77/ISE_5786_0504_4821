package scene.io;

import java.nio.file.Path;

import scene.Scene;

/**
 * Parses a scene definition file into a {@link Scene} object.
 */
public interface SceneParser {
   /**
    * Parses the given file into a scene.
    *
    * @param sourceFile source file path
    * @param sceneName scene name to assign
    * @return parsed scene
    */
   Scene parse(Path sourceFile, String sceneName);
}
