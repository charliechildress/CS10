import java.awt.*;

import javax.swing.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Using a quadtree for collision detection
 * 
 * @author Charlie Childress & Eric Leung, Dartmouth CS 10, Fall 2021, using material given on course webpage
 */
public class CollisionGUI extends DrawingGUI {
	private static final int width=800, height=600;		// size of the universe

	private List<Blob> blobs;						// all the blobs
	private List<Blob> colliders;					// the blobs who collided at this step
	private char blobType = 'b';						// what type of blob to create
	private char collisionHandler = 'c';				// when there's a collision, 'c'olor them, or 'd'estroy them
	private int delay = 100;							// timer control

	/**
	 * constructor for CollisionGUI
	 */
	public CollisionGUI() {
		super("super-collider", width, height);

		blobs = new ArrayList<Blob>();

		// Timer drives the animation.
		startTimer();
	}
	/**
	 * Adds an blob of the current blobType at the location
	 * @param x		x coordinate of current location
	 * @param y		y coordinate of current location
	 */
	private void add(int x, int y) {
		if (blobType=='b') {
			blobs.add(new Bouncer(x,y,width,height));
		}
		else if (blobType=='w') {
			blobs.add(new Wanderer(x,y));
		}
		else {
			System.err.println("Unknown blob type "+blobType);
		}
	}

	/**
	 * DrawingGUI method, here creating a new blob
	 * @param x     x coordinate of current mouse location
	 * @param y		y coordinate of current mouse location
	 */
	public void handleMousePress(int x, int y) {
		add(x,y);
		repaint();
	}

	/**
	 * DrawingGUI method
	 * @param k		the keyboard character pressed
	 */
	public void handleKeyPress(char k) {
		if (k == 'f') { // faster
			if (delay>1) delay /= 2;
			setTimerDelay(delay);
			System.out.println("delay:"+delay);
		}
		else if (k == 's') { // slower
			delay *= 2;
			setTimerDelay(delay);
			System.out.println("delay:"+delay);
		}
		else if (k == 'r') { // add some new blobs at random positions
			for (int i=0; i<10; i++) {
				add((int)(width*Math.random()), (int)(height*Math.random()));
				repaint();
			}			
		}
		else if (k == 'c' || k == 'd') { // control how collisions are handled
			collisionHandler = k;
			System.out.println("collision:"+k);
		}
		else { // set the type for new blobs
			blobType = k;			
		}
	}

	/**
	 * DrawingGUI method, here drawing all the blobs and then re-drawing the colliders in red
	 * @param g 	graphics object g that allows us to implement drawing-related processes
	 */
	public void draw(Graphics g) {
		// Ask all the blobs to draw themselves in black
		if (blobs != null) {
			for (Blob blob : this.blobs) {
				g.setColor(Color.black);
				blob.draw(g);
			}
		}
		// Ask the colliders to draw themselves in red.
		if (colliders != null) {
			for (Blob blob : this.colliders) {
				g.setColor(Color.red);
				blob.draw(g);
			}
		}
		// Reset colliders at the end of draw so no longer red if not colliding
		if (colliders != null){
			colliders.clear();
		}
	}


	/**
	 * Sets colliders to include all blobs in contact with another blob
	 */
	private void findColliders() {

		// Defines empty tree
		PointQuadtree<Blob> blobList = null;

		// initializing the tree blobList, and adding all blobs to the tree
		for (Blob blob: blobs){
			if (blobList == null){
				blobList = new PointQuadtree<Blob>(blob, 0, 0, width, height);
			}
			else{
				blobList.insert(blob);
			}
		}

		// Initialize an ArrayList of type Blob to keep track of colliders
		if (colliders == null){
			colliders = new ArrayList<Blob>();
		}
		else{
			for (Blob blob: blobs){
				// call findInCircle method to check if there the blob is colliding with any other blobs
				if (blobList.findInCircle(blob.x, blob.y, blob.r*2).size() > 1){
					colliders.add(blob);
				}
			}
		}
	}

	/**
	 * DrawingGUI method, here moving all the blobs and checking for collisions
	 */
	public void handleTimer() {
		// Ask all the blobs to move themselves.
		for (Blob blob : blobs) {
			blob.step();
		}
		// Check for collisions
		if (blobs.size() > 0) {
			findColliders();
			if (collisionHandler=='d') {
				blobs.removeAll(colliders);
				colliders = null;
			}
		}
		// Now update the drawing
		repaint();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CollisionGUI();
			}
		});
	}
}
