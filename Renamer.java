import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
/******************************************************************************
 * This class handles all major functionality, and depends on the Image class.
 * - optionSetup() handles the options text file, reads from it or creates if non existent
 * - initialRenaming() renames all images first to ensure that there are no later conflicts 
 *    (in particular if program is run more than once on a series of images)
 * - color() handles sorting the images by color using a naive approach to the Traveling Salesman Problem
 * - firstPass() does the initial scanning and reading of images to get their properties
 * - getLength(int[], ArrayList<Image>) finds the length of a certain path through images
 * - distance3D(Image, Image) finds the distance in 3D RGB color space between two images
 * - firstNonNeg(int[]) finds the first image ID in a path
 * - nonNeg(int[]) finds the total number of image IDs in a path
 * - naiveTry(int[]) produces a path through the images in 3D color space using a nearest naighbor approach
 */
public class Renamer {
   public static String modifier = ""; 			//added to prevent overwriting if program is stopped before finishing
   public static String option = "resolution";  //the default option if option file missing
   public static int finalCount = 0;            //the total number of images found, used to determine progress during runtime
   public static ArrayList<Image> imageList = new ArrayList<Image>(); 	//stores data on each image to be sorted
   public static double naiveTry = 0;           //the length achieved using the greedy algorithm
   
   public static void main(String[] args) throws IOException, InterruptedException {
      System.out.println("Working");
      optionSetup();
      initialRenaming();
      firstPass();         
   }
   /***************************************************************************
    * This method handles reading and writing the options.txt file, which handles 
    * reading the user specified options
    * 
    */
   public static void optionSetup() throws IOException{
      //file that we will be working with
      File optionFile = new File(".\\options.txt");
      
      //if it doesn't exist, create it
      if(!optionFile.exists()){
         PrintWriter pw = new PrintWriter(optionFile);
         InputStreamReader r = new InputStreamReader( Renamer.class.getClass().getResourceAsStream("/options.txt"));
         try (BufferedReader br = new BufferedReader(r)) {
            String line;
            while ((line = br.readLine()) != null) {
               pw.println(line);
            }
            br.close();
         }
         pw.close();
      }else{
         System.out.println("Found at " + optionFile.toPath());
         //if file does exist, read it
         List<String> optionsFile = new ArrayList<String>();
         List<String> options = new ArrayList<String>();
         try (BufferedReader br = new BufferedReader(new FileReader(optionFile))) {
            String line;
            while ((line = br.readLine()) != null) {
               optionsFile.add(line);
               //filters out lines that are comments
               if (!(line.charAt(0)=='#')){
                  options.add(line);
               }
            }
            br.close();
         }
         
         //sets the modifier according to the options file, which acts a data store that lasts from run to run
         //the purpose of the modifier is to prevent overwriting images if say there already exists an image called 1.jpg
         //and the first path method tries to rename another image to 1.jpg
         if(options.get(0).equals("false")){
            optionsFile.set(optionsFile.indexOf("false"), "true");
         }else if(options.get(0).equals("true")){
            modifier = "X";
            optionsFile.set(optionsFile.indexOf("true"), "false");
         }
         
         //what is the program to do, sort images by resolution, brightness, or color
         option = options.get(1);
         PrintWriter pw = new PrintWriter(optionFile);
         //overwrites options file with updated items
         for(String item:optionsFile){
            pw.println(item);
         }

         pw.close();
      }
   }
   
   /*****************************************************************************
    * Renames all the images to temporary names, to prevent accidental overwriting
    */
   public static void initialRenaming() throws IOException{
      File dir = new File(".\\images");
      if (!dir.exists()){
         dir.mkdir();
      }
      for (File file : dir.listFiles()){
         String type = FilenameUtils.getExtension(file.getPath());
         
         if(type.equals("jpeg")){
            type = "jpg";
         }
         String temp = modifier + finalCount + "." + type;
         Files.move(file.toPath(), file.toPath().resolveSibling(temp));
         finalCount++;
      }
   }

   /***************************************************************************
    * Handles case when option to be done is color
    * In short it finds a path through 3D color space using a greedy algorithm
    * that jumps from image to closest image until the whole folder is thus traversed
    */
   public static void color() throws IOException{
      //Path is an array with entries 1, 2, ... finalCount
      //which is in other words an array whose entries are all the image IDs
      int[] Path = new int[finalCount];
      for(int i = 0; i < Path.length; i++){
         Path[i] = i;
      }
      
      //Calculates a path through the given images
      Path = naiveTry(Path);
      
      //This loop handles renaming the images to their new names
      for(int i = 0; i < Path.length; i++){
         //the current location of the Image with ID Path[i]
         File file1 = imageList.get(Path[i]).getFile();
         //d is the distance to the previous image, 0 if it's the first
         double d = 0;
         if(i > 0){
             d = distance3D(imageList.get(Path[i]), imageList.get(Path[i-1]));
         }
         
         //The new name for the image:
         //C because its a color ordering
         //i the images place in the path, this is what your file explorer uses
         //to sort them alphabetically, and therefore by color
         //d is the aforementioned distance to the previous image
         //Path[i] is the image's ID
         //the last part is its filetype
         String name = "C" + i + " -" + d + " -" + Path[i] + "." + imageList.get(Path[i]).getType();
         //renames the file to the new name;
         file1.renameTo(new File (file1.toPath().resolveSibling(name).toString()) );

      }
   }
   
   /***************************************************************************
    * This method has the simple task of going through all images in the directory to
    * get their info and then do one of the three available things, either sort them
    * by color, resolution, or brightness
    */
   
   public static void firstPass() throws IOException, InterruptedException{
      //the directory that contains the images
      File dir = new File(".\\images");
      
      //cycles through all images in the folder
      for(File file:	dir.listFiles()){
         //creates an image object
         Image img = new Image(file);
         
         if(option.equals("brightness")){
            //no sorting to be done, just rename immediately according to brightness
            DecimalFormat f = new DecimalFormat("##.0000");
            String name = "B" + f.format(img.getBrightness()) + " -" + img.getID() + "." + img.getType();
            file.renameTo(new File (file.toPath().resolveSibling(name).toString()) );
         
         }else if(option.equals("color")){
            //sorting and renaming done later, for now just add images to the array of images
            imageList.add(img);
            
         }else if(option.equals("resolution")){
            //again there's no sorting to be done, just immediately rename as appropriate
            String name = img.getWidth() + " x " + img.getHeight() + " -" + img.getID() + "." + img.getType();
            file.renameTo(new File (file.toPath().resolveSibling(name).toString()) );
         
         }   
         
         //prints a string to indicate progress, the initial reading may take a while
         if(img.getID()%10 == 0){
            System.out.println("Working on " + file);
         }
      }
      
      //if images are to be sorted by color, do so
      if (option.equals("color")){
         color();
      }
   }
   
   /***************************************************************************
    * This utility method calculates the 3D distance between two given Image objects
    * 
    * @return the distance
    */
   private static double distance3D(Image first, Image second){
      double a = first.getPercentR() - second.getPercentR();
      double b = first.getPercentG() - second.getPercentG();
      double c = first.getPercentB() - second.getPercentB();
      return Math.sqrt(a*a + b*b + c*c);
   }

   /***************************************************************************
    * Because image lists are stored in arrays of fixed length, "empty" places are labeled
    * with a -1, and this method simply finds the first entry that isn't negative
    */
   private static int firstNonNeg(int[] array){
      for (int i = 0; i < array.length; i++){
         if(array[i] != -1){
            return i;
         }
      }
      //if no such non negative number found
      return -1;
   }
   
   
   /***************************************************************************
    * This method implements a greedy or nearest neighbor algorithm for finding a
    * path through the given images. In short, it takes the first image, adds it
    * to the path, jumps to the next closest and adds it to the path, and so on 
    * until all images are traversed
    * 
    * @param images, the array of image IDs to be traversed
    * @return the path, an array of image IDs
    */
   private static int[] naiveTry(int[] images){
      //if there are nontrivial paths
      if(images.length > 2 ){
         //creates a local copy of the given images
         int[] localImg = new int[finalCount];
         System.arraycopy(images, 0, localImg, 0, finalCount);
               
         //path to be populated and returned
         int[] path = new int[finalCount];
         //the -1s act to clear the array
         for (int i = 0; i < path.length; i++){
            path[i] = -1;
         }
         
         //first image added as root to the path
         path[0] = localImg[0];
         //first image removed from list of available images
         localImg[0] = -1;
         
         //cycles through each position in the path
         for(int index = 0; index < finalCount-1; index++){
            double D = Double.MAX_VALUE;
            //location of winning image ID initialized to the first possible image
            int winIndex = firstNonNeg(localImg);
            //the actual winning ID
            int win = localImg[winIndex];
            
            //cycles through the available images to find the closest
            for(int i = 0; i < localImg.length; i++){
               //if there is a valid ID at i
               if(localImg[i] != -1){
                  double d = distance3D(imageList.get(path[index]), imageList.get(localImg[i]));
                  if(d < D){
                     win = localImg[i];
                     winIndex = i;
                     D = d;
                  }
               }
            }
            //updates path and image list
            path[index+1] = win;
            localImg[winIndex] = -1;
         }
         return path;
      }else{
         //if there are less than two images to begin with, there's only one path,
         //the order that the images were sent in, up to reversal
         return images;
      }
   }
}
