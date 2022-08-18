import java.awt.*;
import java.awt.event.*;
import java.util.TreeMap;

import javax.swing.*;

/**
 * Client-server graphical editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 * @author Franklin Ruan, Charlie Childress, Fall 2021, completed PSET-6
 */
public class Editor extends JFrame {	
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address
	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape shape = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private Double movingId = -1.0;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged


	// Communication
	private EditorCommunicator comm;			// communication with the sketch server


	/**
	 * Constructor
	 */
	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};
		
		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});		

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});
		
		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// TODO: YOUR CODE HERE

		// Draw all the shapes in the world
		TreeMap<Double, Shape> world = sketch.getIdToShape();
		for (Double id: world.keySet()){ // for all the sketches
			world.get(id).draw(g);
		}

		// Draw the shape currently in the sketch (only if it exists and it's not being moved)
		if (shape != null && movingId == -1){
			shape.draw(g);
		}

	}

	// Helpers for event handlers
	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		// TODO: YOUR CODE HERE
		// load the world
		TreeMap<Double, Shape> world = sketch.getIdToShape();

		// switch through modes
		switch(mode) {
			case DRAW:
				// find the shape type, then begin tra
				if (shapeType.equals("ellipse")){
					shape = new Ellipse(p.x, p.y, color);
					drawFrom = p;
				}
				if (shapeType.equals("rectangle")){
					shape = new Rectangle(p.x, p.y, color);
					drawFrom = p;
				}
				if (shapeType.equals("segment")){
					shape = new Segment(p.x, p.y, color);
					drawFrom = p;
				}
				if (shapeType.equals("freehand")){
					shape = new Polyline(p.x, p.y, color);
					drawFrom = p;

				}

				break;
			case MOVE:
				for (Double id: world.descendingKeySet()) { // for all the sketches
					// look for a match
					if (world.get(id).contains(p.x, p.y)) {
						movingId = id;
						drawFrom = p;
						break;
					}
				}

				break;
			case RECOLOR:
				for (Double id: world.descendingKeySet()) { // for all the sketches
					// look for a match
					if (world.get(id).contains(p.x, p.y)) { // world.get(id) is the shape
						world.get(id).setColor(color);
						comm.send(world.get(id) +" "+id);
						break;
					}
				}
				break;
			case DELETE:
				for (Double id: world.descendingKeySet()) { // for all the sketches
					// look for a match
					if (world.get(id).contains(p.x, p.y)) {
						world.remove(id);
						comm.send("DELETE "+id);
						break;

					}
				}
				repaint();
				break;

		}




	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		// TODO: YOUR CODE HERE
		switch(mode){
			case DRAW:
				if (shapeType.equals("ellipse")){
					// cast it in to ellipse to make adjustments
					Ellipse adjust = (Ellipse) shape;
					adjust.setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
					// re-save it as the instance variable
					shape = adjust;
				}
				if (shapeType.equals("rectangle")){
					// cast it to a rectangle to make adjustments
					Rectangle adjust = (Rectangle) shape;
					adjust.setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
					// re-save it as the instance variable
					shape = adjust;

				}
				if (shapeType.equals("segment")){
					// cast it to a segment to make adjustments
					Segment adjust = (Segment) shape;
					adjust.setEnd(p.x, p.y);
					// re-save it as the instance variable
					shape = adjust;
				}

				if (shapeType.equals("freehand")){
					// case it to a polyline to make adjustments
					Polyline adjust = (Polyline) shape;
					adjust.setEnd(p.x, p.y);
					// re-save it as the instance variable
					shape = adjust;
				}

				break;

			case MOVE:

				if (movingId != -1.0) { // if there is a chosen shape to move
					moveFrom = p;
					int xInc = moveFrom.x - drawFrom.x;
					int yInc = moveFrom.y - drawFrom.y;
					drawFrom = moveFrom;

					// find the shape and then move it appropriately
					TreeMap<Double, Shape> localWorld = sketch.getIdToShape();
					shape = localWorld.get(movingId);
					shape.moveBy(xInc, yInc);
					comm.send(shape.toString()+" "+movingId);
				}
			break;
		}
		repaint();

	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it
	 */
	private void handleRelease() {
		// TODO: YOUR CODE HERE
		switch (mode){
			case DRAW:
				if (shape != null) { // if there is a shape we're drawing
					comm.send(shape.toString() + " " + "-1"); // sends with an id o f -1 (which signals that it's a new shape
					shape = null; // then we should stop saving the image locally.
				}

			case MOVE:
				if (movingId != -1){ // if there is the shape we're moving
					comm.send(shape.toString()+" "+movingId);
					shape = null; // reset the shape we're working with
					movingId = -1.0; // reset the id we're working with
				}



		}


	}

	/**
	 * Main
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});	
	}
}
