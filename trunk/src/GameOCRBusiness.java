import net.sourceforge.javaocr.ocrPlugins.mseOCR.CharacterRange;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.OCRScanner;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.TrainingImage;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.TrainingImageLoader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author MTO
 */
public class GameOCRBusiness {
   private static OCRScanner ocrScanner = new OCRScanner();
   private JFrame frame;

   public GameOCRBusiness() {
      frame = new JFrame();
      frame.setVisible(false);
      initTraining(ocrScanner);
   }

   public Plade recognize(BufferedImage input) {
      BufferedImage resultImage = stripImageForColors(input);

      String text = ocrScanner.scan(resultImage, 0, 52, 0, 0, null);
      String[] lines = text.split("\n");
      Plade plade = new Plade();
      for (String line : lines) {
         ArrayList<Integer> numbers = new ArrayList<Integer>();
         String[] numberStrings = line.split(" ");
         for (String numberString : numberStrings) {
            String resultnumber = numberString.trim();
            if (resultnumber.length() != 0) {
               numbers.add(Integer.parseInt(resultnumber));
            }
         }
         if (numbers.size() != 0) {
            plade.addLine(numbers);
         }
      }

      return plade;
   }

   private static BufferedImage stripImageForColors(BufferedImage image) {
      BufferedImage resultImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
      for (int x = 0; x < image.getWidth(); x++) {
         for (int y = 0; y < image.getHeight(); y++) {
            int rgb = image.getRGB(x, y);
            if (rgb == Color.BLACK.getRGB()) {
               resultImage.setRGB(x, y, Color.BLACK.getRGB());
            } else {
               resultImage.setRGB(x, y, Color.WHITE.getRGB());
            }
         }
      }
      return resultImage;
   }


   private void initTraining(OCRScanner scanner) {
      try {
         TrainingImageLoader loader = new TrainingImageLoader();
         HashMap<Character, ArrayList<TrainingImage>> hashMap = new HashMap<Character, ArrayList<TrainingImage>>();
         for (int i = 0; i < 10; i++) {
            File file = new File(SystemConfiguration.TRAINING_DIRECTORY, "char" + i + ".png");
            loader.load(frame, file.getAbsolutePath(), new CharacterRange('0' + i, '0' + i), hashMap);
         }
         scanner.addTrainingImages(hashMap);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

}
