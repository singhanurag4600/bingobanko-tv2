import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

/**
 * @author michael@familientoft.net
 */
public class GameApp {
   private ArrayList<Plade> plader = new ArrayList<Plade>();
   private ArrayList<Integer> numbers;

   public static void main(String[] args) throws Exception {

      GameApp gameRunner = new GameApp();
      gameRunner.start(SystemConfiguration.DATA_DIRECTORY);
      System.out.println("Spillet starter, indlæst " + gameRunner.plader.size() + " bingo plader");
      gameRunner.readNumbers();
   }

   private static void createDirs() {
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

         Plade.LineWinnerInfo seen = null;
         for (Plade plade : plader) {
            if(plade.isWinner(numbers)) {
               System.out.println("*******************************************************");
               System.out.println("*******************************************************");
               System.out.println("*******************************************************");
               System.out.println("PLADE VINDER!!!\n" + plade.getFileName());
               System.out.println("*******************************************************");
               System.out.println("*******************************************************");
               System.out.println("*******************************************************");
               Runtime.getRuntime().exec("mspaint " + plade.getFileName());
               return;
            } else if(plade.isTwoLineWinner(numbers) && state.equals(GameState.TWOLINES)) {
               state = GameState.FULL;
               System.out.println("*******************************************************");
               System.out.println("***** 2 LINIE VINDER PLADE *****\n" + plade.getFileName());
               System.out.println("*******************************************************");
               Runtime.getRuntime().exec("mspaint " + plade.getFileName());
            } else if(plade.isSingleLineWinner(numbers) && state.equals(GameState.ONELINE)) {
               state = GameState.TWOLINES;
               System.out.println("***** LINIE VINDER PLADE *****\n" + plade.getFileName());
               Runtime.getRuntime().exec("mspaint " + plade.getFileName());
            }
            Plade.LineWinnerInfo lineWinnerInfo = plade.getLineWinnerInfo(state, numbers);
            int numbers1 = lineWinnerInfo.getCountOfMatches();
            if(maxSeen<numbers1) {
               maxSeen = numbers1;
               seenOfMax = 1;
               seen = lineWinnerInfo;
            } else if(maxSeen == numbers1) {
               seenOfMax ++;
            }
         }
         if(seen!=null) {
            System.out.println(seenOfMax + " linier med " + maxSeen + " (af " + state.getMax() + ") rigtige - mangler nummer: " + seen.toStringNumbers());
         }
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
