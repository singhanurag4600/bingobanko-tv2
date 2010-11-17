import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author MTO
 */
public class SystemConfiguration {
   public static final String ROOT_DIRECTORY;
   public static final String DATA_DIRECTORY;
   public static final String TRAINING_DIRECTORY;
   public static final String PATTERN;

   static {
      // Fetch the current directory, with a possible default option
      ROOT_DIRECTORY = System.getProperty("user.dir", "C:/bingobanko/");

      File patternFile = new File(ROOT_DIRECTORY, "pattern.txt");
      PATTERN = readPattern(patternFile);

      // Initialize data directory
      File boardDirectory = new File(ROOT_DIRECTORY, "data");
      boardDirectory.mkdirs();
      DATA_DIRECTORY = boardDirectory.getPath();
      // Training directory
      File trainingDirectory = new File(ROOT_DIRECTORY, "training");
      if(!trainingDirectory.exists()) {
         throw new RuntimeException("Training directory must exist!");
      }
      TRAINING_DIRECTORY = trainingDirectory.getPath();
   }

   private static String readPattern(File patternFile) {
      StringBuffer buf = new StringBuffer();
      try {
         int ch;
         FileInputStream inputStream = new FileInputStream(patternFile);
         while( (ch = inputStream.read()) != -1) {
           buf.append((char)ch);
         }
         inputStream.close();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      return buf.toString();
   }
}
