import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

/*******************************************
 * 
 * @author Goirad
 *
 *
 *Utility class to modularize the images that I'm analyzing,
 *provides functionality for quickly reading color data and others.
 *
 *Also acts to store stuff like where the file currently is,
 *as well as a unique ID that doesn't change after filename changes.
 */


public class Image {
   private static int totalImages = 0; //total # of instances of this class
   
   private double percentR, totalR;    //data for each of red green and blue
   private double percentG, totalG;
   private double percentB, totalB;
   
   private double H;
   private double S;
   private double L;
   
   private int width, height;          //size of image in pixels
   private int ID;                     //unique ID
   
   private File file;                  //current location on disk
   
   private boolean hasAlpha;           //whether the image has alpha channels
   /*****************************************************
    * @param inputFile the location of the image
    * 
    * Constructor takes input file and tries to read it as an image, extracts data
    */
   public Image(File inputFile) {
      file = inputFile;
      BufferedImage img = null;
      try{
         img = ImageIO.read(file);
      } catch (Exception e) {
         e.printStackTrace();
      }
      get2D(img);
      
     
      
      ID = totalImages;                     //labels instance and increments counter
      totalImages++;
      img = null;                           //dumps potentially huge file
   }
   
   /*************************
    * Converts image to user readable string
    */
   public String toString(){
      return "R " + file + " " + percentR + " G" + percentG + " B" + percentB + " -" + ID + "." + getType();
   }
   
   /*************************
    * returns the type (jpg, png, etc)
    * @return the file type
    */
   public String getType(){
      return FilenameUtils.getExtension(file.getPath());
   }
   
   /*************************
    * returns the average brightness of the whole image, scaled for better quality
    * @return the average brightness of the whole image
    */
   public double getBrightness(){
      return (percentR*.21 + percentG*.71 + percentB*.08)/(2.55*getArea());
   }
   
   /*************************
    * @return the percent of the total image that is red
    */
   public double getPercentR() {
      return percentR;
   }
   
   /*************************
    * @return the percent of the total image that is green
    */
   public double getPercentG() {
      return percentG;
   }

   /*************************
    * @return the percent of the total image that is blye
    */
   public double getPercentB() {
      return percentB;
   }

   /*************************
    * @return the width of the image in pixels
    */
   public int getWidth() {
      return width;
   }
   
   /*************************
    * @return the height of the image in pixels
    */
   public int getHeight() {
      return height;
   }
   
   /*************************
    * @return the image's unique identifier
    */
   public int getID() {
      return ID;
   }
   
   /*************************
    * @return the current file the image is stored as
    */
   public File getFile() {
      return file;
   }
   
   /*************************
    * @return the total number of pixels in the image
    */
   public double getArea(){
      return width*height;
   }
   
   /*************************
    * This is where the magic happens, the image is processed and the color data extracted.
    */
   private void get2D(BufferedImage img) {
      final byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
      
      width = img.getWidth();
      height = img.getHeight();
      hasAlpha = img.getAlphaRaster() != null;
      if (hasAlpha) {
         final int pixelLength = 4;
         for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
            totalB += ((int) pixels[pixel + 1] & 0xff); // blue
            totalB += (((int) pixels[pixel + 2] & 0xff)); // green
            totalB += (((int) pixels[pixel + 3] & 0xff)); // red
         }
      } else {
         final int pixelLength = 3;
         for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
            totalB += ((int) pixels[pixel] & 0xff); // blue
            totalG += (((int) pixels[pixel + 1] & 0xff)); // green
            totalR += (((int) pixels[pixel + 2] & 0xff)); // red
         }
      }
      
      percentR = totalR/2.55/getArea();     //converts totals to percents
      percentG = totalG/2.55/getArea();
      percentB = totalB/2.55/getArea();
   }

   public double getH() {
      return H;
   }

   public double getS() {
      return S;
   }

   public double getL() {
      return L;
   }
}
