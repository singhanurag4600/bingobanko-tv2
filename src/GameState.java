/**
 * @author michael@familientoft.net
 */
public enum GameState {
   ONELINE(5),
   TWOLINES(10),
   FULL(15);
   private int max;

   GameState(int max) {
      this.max = max;
   }

   public int getMax() {
      return max;
   }
}
