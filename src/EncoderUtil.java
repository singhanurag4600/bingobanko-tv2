/**
 * @author MTO
 */
public class EncoderUtil {
   public static String decode(String text) {
      if (text == null) {
         return null;
      }
      int begin = 0;
      int state = 0;
      StringBuffer out = new StringBuffer();
      char[] chs = text.toCharArray();
      for (int i = 0; i < chs.length; i++) {
         char c = chs[i];
         switch (state) {
            case 0:
               if (c == '&') {
                  begin = i;
                  state = 1;
               } else {
                  out.append(c);
               }
               break;
            case 1:
               if (c == '#') {
                  state = 2;
               } else if (Character.isLetter(c)) {
                  state = 3;
               } else {
                  out.append(text.substring(begin, i + 1));
                  state = 0;
               }
               break;
            case 2:
               if (c == ';') {
                  if (i - (begin + 2) > 0) {
                     try {
                        String id = text.substring(begin + 2, i);
                        out.append((char) Integer.parseInt(id));
                     } catch (Exception e) {
                        out.append(text.substring(begin, i + 1));
                     }
                  } else {
                     out.append(text.substring(begin, i + 1));
                  }
                  state = 0;
               } else if (!Character.isDigit(c)) {
                  out.append(text.substring(begin, i + 1));
                  state = 0;
               }
               break;
            case 3:
               if (c == ';') {
                  String id = text.substring(begin + 1, i);
                  out.append(text.substring(begin, i + 1));
                  state = 0;
               } else if (!Character.isLetter(c)) {
                  out.append(text.substring(begin, i + 1));
                  state = 0;
               }
               break;
         }
      }
      return out.toString();
   }
}
