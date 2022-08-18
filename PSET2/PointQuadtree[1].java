import java.util.ArrayList;
import java.util.List;

/**
 * A point quadtree: stores an element at a 2D position, 
 * with children at the subdivided quadrants.
 * 
 * @author Charlie Childress and Eric Leung, Dartmouth CS 10, Fall 2021, using material given on course webpage
 * 
 */
public class PointQuadtree<E extends Point2D> {
	private E point;							// the point anchoring this node
	private int x1, y1;							// upper-left corner of the region
	private int x2, y2;							// bottom-right corner of the region
	private PointQuadtree<E> c1, c2, c3, c4;	// children

	/**
	 * Initializes a leaf quadtree, holding the point in the rectangle
	 */
	public PointQuadtree(E point, int x1, int y1, int x2, int y2) {
		this.point = point;
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
	}

	// Getters
	
	public E getPoint() {
		return point;
	}

	public int getX1() {
		return x1;
	}

	public int getY1() {
		return y1;
	}

	public int getX2() {
		return x2;
	}

	public int getY2() {
		return y2;
	}

	/**
	 * Returns the child (if any) at the given quadrant, 1-4
	 * @param quadrant	1 through 4
	 */
	public PointQuadtree<E> getChild(int quadrant) {
		if (quadrant==1) return c1;
		if (quadrant==2) return c2;
		if (quadrant==3) return c3;
		if (quadrant==4) return c4;
		return null;
	}

	/**
	 * Returns whether or not there is a child at the given quadrant, 1-4
	 * @param quadrant 1 through 4
	 */
	public boolean hasChild(int quadrant) {
		return (quadrant==1 && c1!=null) || (quadrant==2 && c2!=null) || (quadrant==3 && c3!=null) || (quadrant==4 && c4!=null);
	}

	/**
	 * Inserts the point into the tree
	 * First establish the parameters of each quadrant
	 * If the point is in the quadrant insert it into the tree
	 * @param p2 new point
	 */
	public void insert(E p2) {
		// Establish quadrants in terms of the p2 parameters and point instance variable so that these quadrants can change
		boolean quadrant1 = p2.getX() >= this.point.getX() && p2.getY() <= this.point.getY();	// top right
		boolean quadrant2 = p2.getX() <= this.point.getX() && p2.getY() <= this.point.getY();	// top left
		boolean quadrant3 = p2.getX() <= this.point.getX() && p2.getY() >= this.point.getY();	// bottom left

		if(quadrant1) {
			if (hasChild(1)) {
				c1.insert(p2);
			}
			else {	// if a tree doesn't already exist, create a new one starting at this point
				c1 = new PointQuadtree<E>(p2, (int) point.getX(), y1, x2, (int) point.getY());
			}
		}
		else if(quadrant2) {
			if (hasChild(2)) {
				c2.insert(p2);
			}
			else {
				c2 = new PointQuadtree<E>(p2, x1, y1, (int) point.getX(), (int) point.getY());
			}
		}
		else if(quadrant3) {
			if (hasChild(3)) {
				c3.insert(p2);
			}
			else {
				c3 = new PointQuadtree<E>(p2, x1, (int) point.getY(), (int) point.getX(), y2);
			}
		}
		else {	// can just use else as all quadrants but this one has been established
			if (hasChild(4)) { // bottom right
				c4.insert(p2);
			}
			else {
				c4 = new PointQuadtree<E>(p2, (int) point.getX(), (int) point.getY(), x2, y2);
			}
		}
	}
	
	/**
	 * Finds the number of points in the quadtree (including its descendants)
	 * Use recursion to add a child to the size and then all children in each of the initial child's 4 quadrants
	 * Number increases by one each time a quadrant has a child
	 * Then checks all of that child's children
	 */
	public int size() {
		int num = 1;
		if(hasChild(1)) {
			num += this.c1.size();
		}
		if(hasChild(2)) {
			num += this.c2.size();
		}
		if(hasChild(3)) {
			num += this.c3.size();
		}
		if(hasChild(4)) {
			num += this.c4.size();
		}
		return num;	// return the number of all the nodes
	}
	
	/**
	 * Builds a list of all the points in the quadtree (including its descendants)
	 */
	public List<E> allPoints() {
		ArrayList<E> allQuadtree = new ArrayList<E>();
		addPoints(allQuadtree);	// call on a helper function to add points to the list
		return allQuadtree;	// return the list of all points in quadtree
	}	

	/**
	 * Uses the quadtree to find all points within the circle
	 * @param cx	circle center x
	 * @param cy  	circle center y
	 * @param cr  	circle radius
	 * @return    	the points in the circle (and the qt's rectangle)
	 */
	public List<E> findInCircle(double cx, double cy, double cr) {
		ArrayList<E> pointsInCircle = new ArrayList<E>();
		hits(pointsInCircle, cx, cy, cr);	// call on a helper function to add the hits in each circle
		return pointsInCircle;
	}

	/**
	 * Use recursion to add all points in the quadtree to a list, then look at its descendants
	 * Adds a child to a list, then recursively checks all of that child's quadrants for more children
	 * Creates a list of all nodes in the quadtree
	 * @param pointsList   list of all points in quadtree
	 */
	public void addPoints(List<E> pointsList) {
		pointsList.add(this.point);
		if (hasChild(1)) {
			c1.addPoints(pointsList);
		}
		if (hasChild(2)) {
			c2.addPoints(pointsList);
		}
		if (hasChild(3)) {
			c3.addPoints(pointsList);
		}
		if (hasChild(4)) {
			c4.addPoints(pointsList);
		}
	}

	/**
	 * Use the Geometry class to determine whether or not the circle intersects the rectangle
	 * Then use Geometry class to also determine whether or not the point is within the circle
	 * If both are true, add the point to a list of hit points
	 * Recursively check all children to see if they are hit and then if their children are hit
	 * @param cx    circle center x
	 * @param cy  	circle center y
	 * @param cr  	circle radius
	 */
	public void hits(List<E> hitList, double cx, double cy, double cr) {
		if (Geometry.circleIntersectsRectangle(cx, cy, cr, x1, y1, x2, y2)) {
			if (Geometry.pointInCircle(this.point.getX(), this.point.getY(), cx, cy, cr)) {
				hitList.add(this.point);
			}
			if (hasChild(1)) {
				c1.hits(hitList, cx, cy, cr);
			}
			if (hasChild(2)) {
				c2.hits(hitList, cx, cy, cr);
			}
			if (hasChild(3)) {
				c3.hits(hitList, cx, cy, cr);
			}
			if (hasChild(4)) {
				c4.hits(hitList, cx, cy, cr);
			}
		}
	}
}
