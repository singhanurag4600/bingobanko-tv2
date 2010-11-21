import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * @author michael@familientoft.net
 */
public class GameApp {
   private ArrayList<Plade> plader = new ArrayList<Plade>();
   private ArrayList<Integer> numbers;
   private GameOCRBusiness ocrBusiness = new GameOCRBusiness();

   public static void main(String[] args) throws Exception {
      GameApp gameRunner = new GameApp();
      gameRunner.start(SystemConfiguration.DATA_DIRECTORY);
      System.out.println("Spillet starter, indlæst " + gameRunner.plader.size() + " bingo plader");
      gameRunner.readNumbers();
      Thread.sleep(60000);      
   }

   public GameApp() {
      this.plader = new ArrayList<Plade>();
      this.numbers = new ArrayList<Integer>();
   }

   private void readNumbers() throws Exception {
      Scanner sc = new Scanner(System.in);
      GameState state = GameState.ONELINE;
      while (true) {
         System.out.print("Tast bingonummer> ");
         int i = sc.nextInt();
         if (!numbers.contains(i)) {
            numbers.add(i);
         }
         int maxSeen = 0;
         int seenOfMax = 0;

         Collections.shuffle(plader);
         ArrayList<Plade> matches = new ArrayList<Plade>();

         Plade.LineWinnerInfo seen = null;
         for (Plade plade : plader) {
            if(plade.isWinner(numbers)) {
               //Runtime.getRuntime().exec("mspaint " + plade.getFileName());
               System.out.print("\n\nVINDER: " + plade.getKontrolKode() + "\n\n");
               Runtime.getRuntime().exec("mspaint " + plade.getFileName());
               
               return;
            } else if(plade.isTwoLineWinner(numbers) && state.equals(GameState.TWOLINES)) {
               state = GameState.FULL;
               System.out.print("\n\nVINDER: " + plade.getKontrolKode() + "\n\n");
               Runtime.getRuntime().exec("mspaint " + plade.getFileName());

            } else if(plade.isSingleLineWinner(numbers) && state.equals(GameState.ONELINE)) {
               state = GameState.TWOLINES;
               System.out.print("\n\nVINDER: " + plade.getKontrolKode() + "\n\n");

               Runtime.getRuntime().exec("mspaint " + plade.getFileName());

            }
            Plade.LineWinnerInfo lineWinnerInfo = plade.getLineWinnerInfo(state, numbers);
            int numbers1 = lineWinnerInfo.getCountOfMatches();
            if(maxSeen<numbers1) {
               matches.clear();
               maxSeen = numbers1;
               seenOfMax = 1;
               matches.add(plade);
            } else if(maxSeen == numbers1) {
               seenOfMax ++;
               matches.add(plade);
            }
         }
         System.out.println(seenOfMax + " plade med " + maxSeen + " (af " + state.getMax() + ") rigtige");
         int pladeIdx = 0;
         for (Plade match : matches) {
            System.out.print((pladeIdx++) + ": " + match.getKontrolKode() + ", mangler : ");
            Plade.LineWinnerInfo lineWinnerInfo = match.getLineWinnerInfo(state, numbers);
            List<Integer> numbers1 = lineWinnerInfo.getMissingNumbers();
            for (Integer integer : numbers1) {
               System.out.print(integer + ",");
            }
            System.out.println();
            if(pladeIdx>4) {
               break;
            }
         }
         System.out.println();
        
      }
   }

   private void start(String path) throws Exception {
      System.out.println("Indlæser bingoplader, vent et øjeblik");
      File dir = new File(path);
      File[] files = dir.listFiles();
      int nr = 0;
      for (File file : files) {
         File numberFile = new File(file, "data.dat");
         if (numberFile.exists()) {
            FileInputStream inputStream = new FileInputStream(numberFile);
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            try {
               Plade plade = (Plade) ois.readObject();
               plader.add(plade);
            } catch (Exception e) {
               e.printStackTrace();
               System.out.println("Ignoring plade : " + file.getName());
            } finally {
               ois.close();
               inputStream.close();
            }
            if(nr%100==0) {
               System.out.print(".");
            }
            nr++;
         }
      }
      System.out.println();
   }
}
