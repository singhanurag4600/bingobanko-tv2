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
import java.util.ArrayList;
import java.util.Random;

/**
 * @author michael@familientoft.net
 */
public class BoardDownloader {
   private static GameOCRBusiness ocrScanner = new GameOCRBusiness();

   private static int boards = 0;
   private static int doubles = 0;
   private static final int FILENAME_SIZE = 51;
   private static final String searchConstant = "/board/";

   public static void main(String[] args) throws Exception {
      Random random = new Random();

      String message = loadFromUrl("http://46.51.174.209/message3.txt");
      if(message!=null && message.length()!=0) {
         JOptionPane.showMessageDialog(new JFrame(), message);
         System.exit(0);
         return;
      }

      BoardDownloader stripper = new BoardDownloader();
      while(true) {
         try {
            stripper.http();
            long millis = (long)(random.nextDouble() * 10000) + 25000;
            System.out.println("Venter " + millis + " milliseconds, saa tv2 ikke bliver sure...");
            Thread.sleep(millis);
         } catch (Exception e) {
            System.out.println("Ignoring error from server : " + e.getMessage());
            Thread.sleep(60000);
         }                                                                                                   
      }
   }

   public BoardDownloader() {
   }

   private void http() throws Exception {
      File rootDir = new File(SystemConfiguration.DATA_DIRECTORY);
      String s = loadFromUrl("http://bingobanko.tv2.dk/print/");

      Parser parser = new Parser(s);

      OrFilter filter = new OrFilter(new TagNameFilter("IMG"), new TagNameFilter("script"));

      NodeList list = parser.extractAllNodesThatMatch(filter);
      SimpleNodeIterator simpleNodeIterator = list.elements();
      while(simpleNodeIterator.hasMoreNodes()) {
         Node node = simpleNodeIterator.nextNode();
         if(node instanceof ImageTag) {
            ImageTag img = (ImageTag)node;
            String attribute = img.getAttribute("src");
            String decoded = EncoderUtil.decode(attribute);
            if(decoded!=null && decoded.length()==FILENAME_SIZE) {
               String boardName = decoded.substring(7);
               fetchBoard(rootDir, boardName);
            }
         } else if(node instanceof ScriptTag) {
            ScriptTag tag = (ScriptTag)node;
            String scriptCode = tag.getScriptCode();
            if(scriptCode.indexOf(searchConstant)!=-1) {
               ArrayList<String> boardNames = getBoards(scriptCode);
               for (String boardName : boardNames) {
                  fetchBoard(rootDir, boardName);
               }
            }
         }
      }
   }

   private ArrayList<String> getBoards(String s) throws Exception {
      int nextStart = 0;
      ArrayList<String> boards = new ArrayList<String>();

      while(true) {
         int startPosition = s.indexOf(searchConstant, nextStart);
         if(startPosition==-1) {
            break;
         }
         int endPosition = s.indexOf("\"", startPosition+searchConstant.length());

         int realStart = startPosition + searchConstant.length();
         String picName = s.substring(realStart, endPosition);

         boards.add(picName);
         nextStart = endPosition;
      }
      return boards;
   }

   private void fetchBoard(File rootDir, String picName) throws IOException {
      BufferedImage image = readImage(picName);
      String kontrol = picName.substring(2,7);

      if(image!=null) {
         Plade plade = ocrScanner.recognize(image);
         plade.setKontrolKode(kontrol);
         String fileTitle = plade.getFileTitle();

         File targetDir = new File(rootDir, fileTitle);
         if(!targetDir.exists()) {
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
      System.out.println("Hentet plade " + (boards+doubles) + " (" + picName + ") - Fundet " + doubles + " dubletter, " + boards + " unikke");
   }

   private static String loadFromUrl(String pageUrl) throws IOException {
      try {
         URL url = new URL(pageUrl);
         URLConnection urlConnection = url.openConnection();
         urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.7 (KHTML, like Gecko) Chrome/7.0.517.44 Safari/534.7)");
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

   private BufferedImage readImage(String picName) throws IOException {
      URL pictureURL = new URL("http://bingobanko.tv2.dk" + searchConstant + picName);
      URLConnection connection = pictureURL.openConnection();
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

   private void writeBinaryData(Plade plade, File targetDir) throws IOException {
      File targetOcrDat = new File(targetDir, "data.dat");
      FileOutputStream fileOutputStream = new FileOutputStream(targetOcrDat);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
      objectOutputStream.writeObject(plade);
      objectOutputStream.close();
      fileOutputStream.close();
   }

   private void writeTextData(Plade plade, File targetDir) throws IOException {
      File targetOcr = new File(targetDir, "data.txt");
      FileWriter writer = new FileWriter(targetOcr, false);

      writer.append(plade.getKontrolKode() + "\r\n");

      for(int i=0;i<plade.getLineCount();i++) {
         ArrayList<Integer> list = plade.getLine(i);
         for (Integer integer : list) {
            writer.append(" " + integer);
         }
         writer.append("\r\n");
      }
      writer.close();
   }
}
