import javax.swing.plaf.synth.Region;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Region growing algorithm: finds and holds regions in an image.
 * Each region is a list of contiguous points with colors similar to a target color.
 * Scaffold for PS-1, Dartmouth CS 10, Fall 2021
 *
 * @author Charlie Childress & Eric Leung, Dartmouth CS 10, October 1, 2021, based on material on cs10 website
 */



public class RegionFinder {
    private static final int maxColorDiff = 30; // how similar a pixel color must be to the target color, to belong to a region
    private static final int minRegion = 50;    // how many points in a region to be worth considering
    private BufferedImage image;    // the image in which to find regions
    private BufferedImage recoloredImage;   // the image with identified regions recolored
    private ArrayList<ArrayList<Point>> regions;    // a region is a list of points, so the identified regions are in a list of lists of points

    public RegionFinder() {
        this.image = null;
    }

    public RegionFinder(BufferedImage image) {
        this.image = image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public BufferedImage getRecoloredImage() {
        return this.recoloredImage;
    }

    /**
     * Sets regions to the flood-fill regions in the image, similar enough to the trackColor.
     */
    public void findRegions(Color targetColor) {
        // set local variables for width and height of image
        int width = this.image.getWidth();
        int height = this.image.getHeight();
        BufferedImage visited = new BufferedImage(width, height, 2);    //create new blank image
        ArrayList<Point> toVisit = new ArrayList();     //create new blank array list

        regions = new ArrayList<>();    // make region equal a new array list, so it is no longer null

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {       // loop over all pixels of the image
                Color check = new Color(this.image.getRGB(x, y));   // create new color at each pixel

                // check if the pixel color matches the targetColor and has not been visited yet
                if (colorMatch(check, targetColor) && visited.getRGB(x, y) == 0) {
                    // if it matches and is unvisited, start a new region and add the pixel to the list of pixels that need to be visited
                    ArrayList<Point> newRegion = new ArrayList();
                    newRegion.add(new Point(x, y));
                    toVisit.add(new Point(x, y));

                    // while pixels need to be visited, check the pixels neighbors, if the neighbor's colors matches and
                    // has not been visited yet, check that neighbor's neighbors until all pixels of interest have been
                    // checked
                    while (toVisit.size() >= 1) {
                        Point pixelOfInterest = toVisit.get(0);
                        for (int neighborY = pixelOfInterest.y - 1; neighborY <= pixelOfInterest.y + 1; ++neighborY) {
                            if (neighborY <= height - 1 && neighborY >= 0) { // check bc added this above
                                for (int neighborX = pixelOfInterest.x - 1; neighborX <= pixelOfInterest.x + 1; ++neighborX) {
                                    if ((neighborX <= width - 1 && neighborX >= 0) && (visited.getRGB(neighborX, neighborY) == 0)) {    // loops over all unvisited neighbors
                                        visited.setRGB(neighborX, neighborY, 1);
                                        Color newCheck = new Color(this.image.getRGB(neighborX, neighborY));
                                        if (colorMatch(newCheck, targetColor)) {
                                            newRegion.add(new Point(neighborX, neighborY));
                                            toVisit.add(new Point(neighborX, neighborY));
                                        }

                                    }
                                }
                            }
                        }
                        toVisit.remove(0);  // remove the pixel you just checked from the toVisit list
                    }
                    // if the new region is bigger than the minRegion requirement size, add it to the main regions list
                    if (newRegion.size() >= minRegion) {
                        regions.add(0, newRegion);
                    }
                }
            }
        }
    }

    /**
     * Tests whether the two colors are "similar enough"
     */
    private static boolean colorMatch(Color c1, Color c2) {
        boolean similarEnough = false;  //initialize to false
        // use Euclidean equation to equate the difference between colors
        int colorDiff = (c1.getRed() - c2.getRed()) * (c1.getRed() - c2.getRed()) + (c1.getGreen() - c2.getGreen()) * (c1.getGreen() - c2.getGreen()) + (c1.getBlue() - c2.getBlue()) * (c1.getBlue() - c2.getBlue());
        // if the difference is less than maxColorDiff requirement, then the colors are referred to as 'similar enough"
        if (Math.sqrt(colorDiff) <= maxColorDiff) {
            similarEnough = true;   //return true
        }
        return similarEnough;
    }

    /**
     * Returns the largest region detected (if any region has been detected)
     */
    public ArrayList<Point> largestRegion() {
        ArrayList<Point> largestChecker = new ArrayList();  // create new array list to hold the current largest region

        // for every region within regions, if the region is bigger than the current largest region, make that region the new largest region
        for (ArrayList<Point> region : regions) {
            if (region.size() > largestChecker.size()) {
                largestChecker = region;
            }
        }
        return largestChecker;  // return largest region

    }

    /**
     * Sets recoloredImage to be a copy of image,
     * but with each region a uniform random color,
     * so we can see where they are
     */
    public void recolorImage() {
        this.recoloredImage = new BufferedImage(this.image.getColorModel(), this.image.copyData((WritableRaster) null), this.image.getColorModel().isAlphaPremultiplied(), (Hashtable) null);

        // go through every region in regions list
        for (int i = 0; i < regions.size(); i++) {

            ArrayList<Point> recoloredRegion = regions.get(i);
            int randomColor = (int) (Math.random() * 16777216);     // create a new random color

            // go through every pixel in each region
            for (Point pixel : recoloredRegion) {
                this.recoloredImage.setRGB(pixel.x, pixel.y, randomColor);  // give all the pixels in the region a new random color
            }
        }
    }
}
