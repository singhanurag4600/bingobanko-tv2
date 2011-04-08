import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * @author michael@familientoft.net
 */
public class BoardDownloader {
   private static GameOCRBusiness ocrScanner = new GameOCRBusiness();
   
   private static String[] userAgents = new String[] {
      "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Trident/5.0; SLCC1; .NET CLR 3.0.50727; Media Center PC 5.0; .NET CLR",
      "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; es-ES; rv:1.9.2.12) Gecko/20101026 Firefox/3.7.12",
      "Mozilla/5.0 (Windows; U; Windows NT 5.1; fr; rv:1.9.2.12) Gecko/20101026 Firefox/3.6.12 (.NET CLR 3.6.30729)",
      "Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/544.12 (KHTML, like Gecko) Chrome/9.0.587.0 Safari/634.12",
      "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)",
      "Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/574.12 (KHTML, like Gecko) Chrome/9.0.587.0 Safari/534.12",
      "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/5.0; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5",
      "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Trident/9.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR",
      "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/11.0; GTB6.6; InfoPath.1; .NET CLR 2.0.50727; .NET CLR 3.0.450",
      "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)",
      "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/30060127 Netscape/8.1",
      "Mozilla/4.0 (compatible; MSIE 6.0; MSIE 5.5; Windows NT 5.1) Opera 6.02 [en]",
      "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.2322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)",
      "Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US) AppleWebKit/531.0 (KHTML, like Gecko) Chrome/3.0.198.0 Safari/532.0",
      "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_7; en-US) AppleWebKit/431.0 (KHTML, like Gecko) Chrome/3.0.183 Safari/531.0"
   };

   private static int boards = 0;
   private static int doubles = 0;
   private static final String BINGOBANKO_URL = "bingobanko.tv2.dk";

   private static String currentUserAgent = null;

   public static void main(String[] args) throws Exception {
      Random random = new Random();

      BoardDownloader stripper = new BoardDownloader();
      while (true) {
         try {
            currentUserAgent = pickUserAgent();
            stripper.http();
            long millis = (long) (random.nextDouble() * 10000) + 5000;
        	System.out.println();
            System.out.println("Venter " + millis + " millisekunder, s† TV2 ikke bliver sure...");
			System.out.println();
            Thread.sleep(millis);
         } catch (Exception e) {
            System.out.println("Ignoring error from server : " + e.getMessage());
            e.printStackTrace();
            Thread.sleep(60000);
         }
      }
   }

   public BoardDownloader() {
   }

   private void http() throws Exception {
      File rootDir = new File(SystemConfiguration.DATA_DIRECTORY);
      String s = loadFromUrl("http://" + BINGOBANKO_URL + "/print/?boardCount=9");

      Parser parser = new Parser(s);

      OrFilter filter = new OrFilter(new TagNameFilter("IMG"), new TagNameFilter("script"));

      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Copenhagen"), new Locale("da", "DK"));
      int weekIdx = cal.get(Calendar.WEEK_OF_YEAR);
      int bingoIdx = 14 + weekIdx;

      NodeList list = parser.extractAllNodesThatMatch(filter);
      SimpleNodeIterator simpleNodeIterator = list.elements();
      while (simpleNodeIterator.hasMoreNodes()) {
         Node node = simpleNodeIterator.nextNode();
         if (node instanceof ImageTag) {
            ImageTag img = (ImageTag) node;
            String attribute = img.getAttribute("src");
            String decoded = EncoderUtil.decode(attribute);

            if (decoded != null) {
               int position = decoded.indexOf("/" + String.valueOf(bingoIdx));
               if (position != -1) {
                  String prefix = getPreviousBlock(decoded, position - 1);
                  String boardName = decoded.substring(position + 1);
                  fetchBoard(rootDir, prefix, boardName);
               }
            }
         }
      }
   }

   private String getPreviousBlock(String decoded, int position) {
      StringBuffer previous = new StringBuffer();
      for (int i = position; i >= 0; i--) {
         char ch = decoded.charAt(i);
         previous.append(ch);
         if (ch == '/') {
            break;
         }
      }
      return previous.reverse().toString();
   }


   private static void fetchBoard(File rootDir, String prefix, String picName) throws IOException {
      BufferedImage image = readImage(prefix, picName);
      if (image == null) {
         return;
      }
      String kontrol = picName.substring(2, 7);

      if (image != null) {
         processImage(rootDir, image, kontrol);
      }
      System.out.println("Hentet plade " + (boards + doubles) + " (" + picName + ") - Fundet " + doubles + " dubletter, " + boards + " unikke");
   }

   private static void processImage(File rootDir, BufferedImage image, String kontrol) throws IOException {
      Plade plade = ocrScanner.recognize(image);
      plade.setKontrolKode(kontrol);
      String fileTitle = plade.getFileTitle();

      File targetDir = new File(rootDir, fileTitle);
      if (!targetDir.exists()) {
         targetDir.mkdirs();

         File targetFile = new File(targetDir, fileTitle);
         ImageIO.write(image, "PNG", targetFile);

         writeTextData(plade, targetDir);

         writeBinaryData(plade, targetDir);
         boards++;
      } else {
         doubles++;
      }
   }

   private static String loadFromUrl(String pageUrl) throws IOException {
      try {
         URL url = new URL(pageUrl);
         URLConnection urlConnection = url.openConnection();
         urlConnection.setRequestProperty("User-Agent", currentUserAgent);
         InputStream urlIs = urlConnection.getInputStream();
         BufferedReader rd = new BufferedReader(new InputStreamReader(urlIs));
         StringBuffer sb = new StringBuffer();
         String line;
         while ((line = rd.readLine()) != null) {
            sb.append(line);
         }
         rd.close();
         urlIs.close();
         String s = sb.toString();
         return s;
      } catch (IOException e) {
         return null;
      }
   }

   private static BufferedImage readImage(String prefix, String picName) throws IOException {
      URL pictureURL = new URL("http://" + BINGOBANKO_URL + prefix + "/" + picName);
      URLConnection connection = pictureURL.openConnection();
      connection.setRequestProperty("User-Agent", currentUserAgent);
      BufferedImage image;
      try {
         InputStream inputStream = connection.getInputStream();
         image = ImageIO.read(inputStream);
         inputStream.close();
      } catch (IOException e) {
         image = null;
         // Ignore
      }
      return image;
   }

   private static void writeBinaryData(Plade plade, File targetDir) throws IOException {
      File targetOcrDat = new File(targetDir, "data.dat");
      FileOutputStream fileOutputStream = new FileOutputStream(targetOcrDat);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
      objectOutputStream.writeObject(plade);
      objectOutputStream.close();
      fileOutputStream.close();
   }

   private static void writeTextData(Plade plade, File targetDir) throws IOException {
      File targetOcr = new File(targetDir, "data.txt");
      FileWriter writer = new FileWriter(targetOcr, false);

      writer.append(plade.getKontrolKode() + "\r\n");

      for (int i = 0; i < plade.getLineCount(); i++) {
         ArrayList<Integer> list = plade.getLine(i);
         for (Integer integer : list) {
            writer.append(" " + integer);
         }
         writer.append("\r\n");
      }
      writer.close();
   }

   private static String pickUserAgent() {
      double v = Math.random() * userAgents.length;
      return userAgents[(int)v];
   }
}
