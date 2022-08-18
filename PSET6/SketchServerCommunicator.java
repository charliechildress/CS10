import java.io.*;
import java.net.Socket;
import java.util.TreeMap;

/**
 * Handles communication between the server and one client, for SketchServer
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 * @author Franklin Ruan, Charlie Childress, CS 10, Fall 2021, finished PS6
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for
	private static double counter = 0;

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;

	}
	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Synchronize handling clients
	 */
	public synchronized void handleMsg() throws IOException {
		// set up state of the world
		Sketch w = server.getSketch();
		TreeMap<Double, Shape> world = w.getIdToShape();

		String line;
		while ((line = in.readLine()) != null) {

			String[] deleteCheck = line.split(" ");

			if (deleteCheck[0].equals("DELETE")){
				world.remove(Double.valueOf(deleteCheck[1])); // remove the ID
			}
			else {
				IdAndShape newInfo = StringParser.makeShapeWithId(line);
				// get id and shape
				Double id = newInfo.getId();
				Shape newShape = newInfo.getShape();
				if (!world.containsKey(id)) { // If the world doesn't contain the id, already add it as a new one.
					// id is decided by order in which item is placed.
					id = (double) counter++;
					world.put(id, newShape);

					// take out the negative one
					line = line.substring(0, line.length() - 3);
					line += (" " + id);
				} else {
					world.put(id, newShape);
				}
			}
			// broadcast
			server.broadcast(line);
		}
	}

	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");
			
			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			// TODO: YOUR CODE HERE
			Sketch w = server.getSketch();
			TreeMap<Double, Shape> world = w.getIdToShape();
			for(Double id: world.keySet()){ // tell client all the states of the world
				send(world.get(id) + " " + id); // return the string
			}


			// Keep getting and handling messages from the client
			// TODO: YOUR CODE HERE
			handleMsg(); // handle messages synchronously

			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
