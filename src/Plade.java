import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author michael@familientoft.net
 */
public class Plade implements Serializable {
   private String fileName;
   private ArrayList<ArrayList<Integer>> lines;

   private static final long serialVersionUID = 678101492539211724L;

   public Plade(String fileName) {
      this.fileName = fileName;
      this.lines = new ArrayList<ArrayList<Integer>>();
   }

   public String getFileName() {
      return fileName;
   }

   public void addLine(ArrayList<Integer> numbers) {
      lines.add(numbers);
   }

   public ArrayList<Integer> getLine(int lineNr) {
      return lines.get(lineNr);
   }

   public int getLineCount() {
      return lines.size();
   }

   private boolean isLineWinner(ArrayList<Integer> source, List<Integer> numbers) {
      int matches = 0;
      for (Integer number : numbers) {
         if (source.contains(number)) {
            matches++;
         }
      }
      assert matches <= 5;
      return matches == 5;
   }

   public boolean isWinner(List<Integer> numbers) {
      boolean winner = true;
      for (ArrayList<Integer> line : lines) {
         winner = isLineWinner(line, numbers);
         if (!winner) {
            break;
         }
      }
      return winner;
   }

   public boolean isSingleLineWinner(ArrayList<Integer> numbers) {
      for (ArrayList<Integer> line : lines) {
         if (isLineWinner(line, numbers)) {
            return true;
         }
      }
      return false;
   }

   public static class LineWinnerInfo {
      private int countOfMatches;
      private List<Integer> missingNumbers;

      public LineWinnerInfo(int countOfMatches, ArrayList<Integer> missingNumbers) {
         this.countOfMatches = countOfMatches;
         this.missingNumbers = missingNumbers;
      }

      public int getCountOfMatches() {
         return countOfMatches;
      }

      public List<Integer> getMissingNumbers() {
         return missingNumbers;
      }

      public String toStringNumbers() {
         StringBuilder toStr = new StringBuilder();
         for (Integer missingNumber : missingNumbers) {
            toStr.append(",");
            toStr.append(missingNumber);
         }
         return toStr.substring(1);
      }
   }

   public LineWinnerInfo getLineWinnerInfo(GameState state, ArrayList<Integer> numbers) {
      LineWinnerInfo[] lineResults = new LineWinnerInfo[3];
      int idx = 0;
      for (ArrayList<Integer> line : lines) {
         ArrayList<Integer> copy = new ArrayList<Integer>(line);
         int matches = 0;
         for (Integer number : numbers) {
            if (line.contains(number)) {
               matches++;
               copy.remove(number);
            }
         }
         lineResults[idx++] = new LineWinnerInfo(matches, copy);
      }

      if (state.equals(GameState.ONELINE)) {
         LineWinnerInfo bestMatch = null;
         for (LineWinnerInfo lineResult : lineResults) {
            if (bestMatch == null) {
               bestMatch = lineResult;
            } else if (bestMatch.countOfMatches < lineResult.countOfMatches) {
               bestMatch = lineResult;
            }
         }
         return bestMatch;
      } else if (state.equals(GameState.TWOLINES)) {
         int c1 = lineResults[0].countOfMatches + lineResults[1].countOfMatches;
         int c2 = lineResults[0].countOfMatches + lineResults[2].countOfMatches;
         int c3 = lineResults[1].countOfMatches + lineResults[2].countOfMatches;

         if (c1 > c2 && c1 > c3) {
            ArrayList<Integer> integers = new ArrayList<Integer>();
            integers.addAll(lineResults[0].getMissingNumbers());
            integers.addAll(lineResults[1].getMissingNumbers());
            return new LineWinnerInfo(lineResults[0].countOfMatches + lineResults[1].countOfMatches, integers);
         } else if (c2 > c1 && c2 > c3) {
            ArrayList<Integer> integers = new ArrayList<Integer>();
            integers.addAll(lineResults[0].getMissingNumbers());
            integers.addAll(lineResults[2].getMissingNumbers());
            return new LineWinnerInfo(lineResults[0].countOfMatches + lineResults[2].countOfMatches, integers);
         } else if (c3 > c1 && c3 > c2) {
            ArrayList<Integer> integers = new ArrayList<Integer>();
            integers.addAll(lineResults[1].getMissingNumbers());
            integers.addAll(lineResults[2].getMissingNumbers());
            return new LineWinnerInfo(lineResults[1].countOfMatches + lineResults[2].countOfMatches, integers);
         } else {
            // Just picking 2
            ArrayList<Integer> integers = new ArrayList<Integer>();
            integers.addAll(lineResults[0].getMissingNumbers());
            integers.addAll(lineResults[1].getMissingNumbers());
            return new LineWinnerInfo(lineResults[0].countOfMatches + lineResults[1].countOfMatches, integers);
         }
      } else if (state.equals(GameState.FULL)) {
         ArrayList<Integer> missing = new ArrayList<Integer>();
         int totalMatches = 0;
         for (LineWinnerInfo lineResult : lineResults) {
            missing.addAll(lineResult.getMissingNumbers());
            totalMatches += lineResult.getCountOfMatches();
         }
         return new LineWinnerInfo(totalMatches, missing);
      } else {
         throw new RuntimeException("Unknown state");
      }
   }

   public boolean isTwoLineWinner(ArrayList<Integer> numbers) {
      int lineWinners = 0;
      for (ArrayList<Integer> line : lines) {
         if (isLineWinner(line, numbers)) {
            lineWinners++;
         }

         if (lineWinners >= 2) {
            return true;
         }
      }
      return false;
   }
}
