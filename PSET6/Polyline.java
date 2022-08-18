import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 * @author Franklin Ruan, Charlie Childress, Fall 2021, completed PSET-6
 */
public class Polyline implements Shape {
	private int x1, x2, y1, y2;
	private Color color;
	private ArrayList<Point> polylinePoints = new ArrayList<>();

	/**
	 * Initial 0-length polyline at a point
	 * use x1 and y1 as parameters so that they can be parsed easier, convert into points to add to point arraylist
	 */
	public Polyline(int x1, int y1, Color color){
		Point point = new Point(x1, y1);
		polylinePoints.add(point);
		this.color = color;
	}

	/**
	 * Update the end (last point) of the polyline
	 * Also use x1 and y1 for similarity in the StringParser class
	 */
	public void setEnd(int x1, int y1) {
		Point point = new Point(x1, y1);
		polylinePoints.add(point);
		this.x2 = point.x; this.y2 = point.y;
	}

	/**
	 * When moving line, move every point in the line by the specified amount (dx and dy)
	 * Use for loop to go over every point in the arraylist and replace the point with
	 * A new point at the current x + dx and the current y + dy
	 */
	@Override
	public void moveBy(int dx, int dy) {
		for(int i = 0; i < polylinePoints.size(); i++){
			Point temp = polylinePoints.get(i);
			polylinePoints.remove(i);
			polylinePoints.add(i, new Point(temp.x + dx, temp.y + dy));
		}
	}

	/**
	 * getters and setters for colors for recoloring
	 */
	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Test containment of a mouse press in a polyline
	 */
	@Override
	public boolean contains(int x, int y) {

		for (int i = 0; i < polylinePoints.size() - 1; i++) {
			// use Segment.pointToSegmentDistance, since each pair of points along a polyline is basically a segment.
			if (Segment.pointToSegmentDistance(x, y, polylinePoints.get(i).x, polylinePoints.get(i).y, polylinePoints.get(i + 1).x, polylinePoints.get(i + 1).y) <= 3){
				return true;
			}
		}
		return false;
	}

	/**
	 * draw the polyline line segment at a time
	 */
	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		for(int i = 0; i < polylinePoints.size() - 1; i++){
			g.drawLine(polylinePoints.get(i).x, polylinePoints.get(i).y, polylinePoints.get(i + 1).x, polylinePoints.get(i + 1).y);
		}
	}

	/**
	 * add each point's coordinates to a string and output the entire list of coordinates and color of the polyline
	 */
	@Override
	public String toString() {
		String output = "";
		for (Point point : polylinePoints){
			output += point.x + " " + point.y + " ";
		}
		return "polyline " + output + color.getRGB();
	}
}
