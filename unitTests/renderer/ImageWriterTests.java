package renderer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import primitives.Color;

/**
 * Learning tests for {@link ImageWriter}.
 */
class ImageWriterTests {
   /** Image width in pixels. */
   private static final int    IMAGE_WIDTH      = 800;
   /** Image height in pixels. */
   private static final int    IMAGE_HEIGHT     = 500;
   /** Grid cell size in pixels. */
   private static final int    GRID_INTERVAL    = 50;
   /** High-contrast background color. */
   private static final Color  BACKGROUND_COLOR = new Color(java.awt.Color.RED);
   /** High-contrast grid color. */
   private static final Color  GRID_COLOR       = new Color(java.awt.Color.YELLOW);
   /** Output file name (without extension). */
   private static final String OUTPUT_NAME      = "image-writer-grid-test";

   /**
	* Creates a basic two-color image: yellow grid over red background.
	*/
   @Test
   void testImageWriter() {
    assertDoesNotThrow(() -> {
     ImageWriter imageWriter = new ImageWriter(IMAGE_WIDTH, IMAGE_HEIGHT);

     for (int y = 0; y < IMAGE_HEIGHT; y++) {
      for (int x = 0; x < IMAGE_WIDTH; x++) {
         boolean isGridLine = x % GRID_INTERVAL == 0 || y % GRID_INTERVAL == 0;
         imageWriter.writePixel(x, y, isGridLine ? GRID_COLOR : BACKGROUND_COLOR);
      }
     }

     imageWriter.writeToImage(OUTPUT_NAME);
    }, "ImageWriter failed while generating the grid image");
   }
}

