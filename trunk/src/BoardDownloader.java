import net.sourceforge.javaocr.ocrPlugins.mseOCR.CharacterRange;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.OCRScanner;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.TrainingImage;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.TrainingImageLoader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * @author michael@familientoft.net
 */
public class BoardDownloader {
   private static GameOCRBusiness ocrScanner = new GameOCRBusiness();

   private static int boards = 0;
   private static int doubles = 0;

   public static void main(String[] args) throws Exception {
      Random random = new Random();

      BoardDownloader stripper = new BoardDownloader();
      while(true) {
         try {
            stripper.http();
            long millis = (long)(random.nextDouble() * 10000) + 5000;
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
      URL url = new URL("http://bingobanko.tv2.dk/print/");
      URLConnection urlConnection = url.openConnection();
      urlConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; {91560746-A747-4901-8E16-8372427624D2}; generic_01_01; .NET CLR 1.0.3705)");

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

      int nextStart = 0;
      while(true) {
         String searchConstant = "<img src=\"/board/";
         int startPosition = s.indexOf(searchConstant, nextStart);
         if(startPosition==-1) {
            break;
         }
         int endPosition = s.indexOf("\"", startPosition+searchConstant.length());

         int realStart = startPosition + searchConstant.length();
         String picName = s.substring(realStart, endPosition);

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

         nextStart = endPosition;
      }
   }

   private BufferedImage readImage(String picName) throws IOException {
      URL pictureURL = new URL("http://bingobanko.tv2.dk/board/" + picName);
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