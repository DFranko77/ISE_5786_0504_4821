package scene.io;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import geometries.impl.Geometries;
import scene.Scene;

/**
 * Scene parser for XML files.
 */
public class XmlSceneParser implements SceneParser {
   @Override
   public Scene parse(Path sourceFile, String sceneName) {
      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setIgnoringComments(true);
         factory.setIgnoringElementContentWhitespace(true);

         Document document = factory.newDocumentBuilder().parse(sourceFile.toFile());
         Element sceneElement = document.getDocumentElement();

         Scene scene = new Scene(sceneName);
         String background = sceneElement.getAttribute("background-color");
         if (!background.isBlank()) {
            scene.setBackground(SceneParsingUtils.toColor(background));
         }

         NodeList ambientNodes = sceneElement.getElementsByTagName("ambient-light");
         if (ambientNodes.getLength() > 0) {
            Element ambientElement = (Element) ambientNodes.item(0);
            scene.setAmbientLight(SceneParsingUtils.toAmbientLight(ambientElement.getAttribute("color")));
         }

         Geometries geometries = new Geometries();
         NodeList geometriesNodes = sceneElement.getElementsByTagName("geometries");
         if (geometriesNodes.getLength() > 0) {
            Element geometriesElement = (Element) geometriesNodes.item(0);
            NodeList children = geometriesElement.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
               Node node = children.item(i);
               if (node.getNodeType() != Node.ELEMENT_NODE) continue;
               Element geometryElement = (Element) node;

               String type = geometryElement.getTagName();
               Map<String, Object> props = attributesToMap(geometryElement);
               geometries.add(SceneParsingUtils.geometryFromMap(type, props));
            }
         }

         scene.setGeometries(geometries);
         return scene;
      } catch (Exception ex) {
         throw new IllegalArgumentException("Failed parsing XML scene from " + sourceFile, ex);
      }
   }

   private static Map<String, Object> attributesToMap(Element element) {
      Map<String, Object> attributes = new HashMap<>();
      for (int i = 0; i < element.getAttributes().getLength(); i++) {
         Node node = element.getAttributes().item(i);
         attributes.put(node.getNodeName(), node.getNodeValue());
      }
      return attributes;
   }
}
