import java.io.*;
import java.net.Socket;

/**
 * Handles communication to/from the server for the editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author Franklin Ruan, Charlie Childress, Dartmouth CS 10, Fall 2021, finished PS6
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;		// to server
	private BufferedReader in;		// from server
	protected Editor editor;		// handling communication for

	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		}
		catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {
		try {
			// Handle messages
			// TODO: YOUR CODE HERE
			// reading messages from server
			String line;
			while ((line = in.readLine()) != null) {

				// Output what you read
				Sketch localWorld = editor.getSketch();

				// check if there is a delete command
				String[] deleteCheck = line.split(" ");
				if (deleteCheck[0].equals("DELETE")){ // if there is a delete command
					localWorld.getIdToShape().remove(Double.valueOf(deleteCheck[1])); // remove the ID
				}
				else{ // if there is no delete command update the shape or add a new shape
					// Relay everything from the server to the client
					IdAndShape idAndShape = StringParser.makeShapeWithId(line);

					Double id = idAndShape.getId();
					Shape shape = idAndShape.getShape();
					localWorld.put(id, shape);
				}
				editor.repaint();

			}

		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("server hung up");
		}
	}	

	
}
