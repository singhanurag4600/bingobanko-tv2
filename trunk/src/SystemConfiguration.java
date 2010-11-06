import java.io.File;

/**
 * @author MTO
 */
public class SystemConfiguration {
   public static final String ROOT_DIRECTORY;
   public static final String DATA_DIRECTORY;
   public static final String TRAINING_DIRECTORY;

   static {
      // Fetch the current directory, with a possible default option
      ROOT_DIRECTORY = System.getProperty("user.dir", "C:/bingobanko/");
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
}
