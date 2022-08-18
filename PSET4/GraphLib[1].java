import java.util.*;

/**
 * Library for graph analysis
 * 
 * @author Charlie Childress & Kirill Lanski, Dartmouth CS 10, Fall 2021, using material given on cs10 webpage
 * 
 */
public class GraphLib {
	/** Breadth First Search
	 *
	 * @param g -- graph to search
	 * @param source -- starting vertex
	 */
	public static <V, E> Graph<V, E> bfs(Graph<V, E> g, V source) {
		Graph<V, E> backTrack = new AdjacencyMapGraph<V, E>(); //initialize backTrack
		backTrack.insertVertex(source); //load source vertex
		//Set<V> visited = new HashSet<V>(); //Set to track which vertices have already been visited
		Queue<V> queue = new LinkedList<V>(); //queue to implement BFS

		queue.add(source); //enqueue source vertex
		//visited.add(source); //add source to visited Set
		while (!queue.isEmpty()) { //loop until no more vertices
			V u = queue.remove(); //dequeue
			for (V vertex : g.outNeighbors(u)) { //loop over all neighbors
				if (!backTrack.hasVertex(vertex)) { //if neighbor not visited, then neighbor is discovered from this vertex
					//visited.add(vertex); //add neighbor to visited Set
					queue.add(vertex); //enqueue neighbor
					backTrack.insertVertex(vertex); // add neighbor to map
					backTrack.insertDirected(vertex, u, g.getLabel(u, vertex)); //save that this vertex was discovered from prior vertex
				}
			}

		}

		return backTrack;
	}

	/**
	 * Find a path.  Start at end vertex and work backward using
	 * backTrack to start.  Assumes BFS has run and backTrack is filled.
	 * @param tree -- graph of vertices and their edges
	 * @param v -- ending vertex
	 * @return arraylist of nodes on path from start to end, empty if no path
	 */
	public static <V, E> List<V> getPath(Graph<V, E> tree, V v){
		//make sure vertex is in backTrack and that the tree has vertices
		if (tree.numVertices() == 0 || !tree.hasVertex(v) ) {
			System.out.println("Run BFS on " + v + " before trying to find a path");
			return new ArrayList<V>();
		}
		//start from end vertex and work backward to start vertex
		ArrayList<V> path = new ArrayList<V>(); //this will hold the path from start to end vertex
		V current = v; //start at end vertex
		path.add(current); // add the end vertex first
		//loop from end vertex back to start vertex

		while (tree.outDegree(current)!=0) {
			for( V vertex : tree.outNeighbors(current)) {
				current = vertex ; //get vertex that discovered this vertex
			}
			path.add(current); //add this vertex to front of arraylist path
		}

		return path;
	}

	/**
	 * return a set of vertices that are not in the shortest path, so BFS does not reach them
	 * @param graph -- main graph of vertices and their edges
	 * @param subgraph -- graph of the vertices and their edges in the shortest path
	 * @return -- set of vertices
	 */
	public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph){
		Set<V> vertices = new HashSet<V>();
		//check all vertices
		for(V vertex : graph.vertices()) {
			// if the vertex is not in the shortest path, add it to the set of missing vertices
			if (!subgraph.hasVertex(vertex)) {
				vertices.add(vertex);
			}
		}
		System.out.println("Missing vertices:");
		return vertices;
	}

	/**
	 * go through each vertex in the shortest path from the source vertex and find the average separation from source
	 * @param tree -- main graph of vertices and their edges
	 * @param root -- source vertex
	 * @return -- double representing the average separation of vertices from source
	 */
	public static <V,E> double averageSeparation(Graph<V,E> tree, V root){
		double verticesInShortestPath = 0;
		//check all vertices
		for (V vertex : tree.vertices()) {
			if (vertex != root) {
				List<V> pathList = getPath(tree, vertex); //create list of all vertices in the shortest path
				verticesInShortestPath += pathList.size()-1; //get the size to find the number for vertices in path
			}
		}
		//separation equals the vertices in shortest path divided by total vertices
		double averageSeparation = verticesInShortestPath/tree.numVertices();
		System.out.println("Average separation:");
		return averageSeparation;
	}

	public static <T> void main(String[] args) {
		Graph<String, Set<String>> testGraph = new AdjacencyMapGraph<>();

		String bacon = "Kevin Bacon";
		String a = "Alice";
		String c = "Charlie";
		String b = "Bob";
		String d = "Dartmouth";
		String nobody = "Nobody";
		String nobodysFriend = "Nobody's Friend";

		Set<String> BaconAndA = new HashSet<String>();
		BaconAndA.add("A Movie");
		BaconAndA.add("E Movie");

		Set<String> BaconAndB = new HashSet<String>();
		BaconAndB.add("A Movie");

		Set<String> AAndB = new HashSet<String>();
		AAndB.add("A Movie");

		Set<String> ac = new HashSet<String>();
		ac.add("D Movie");


		Set<String> bc = new HashSet<String>();
		bc.add("C Movie");

		Set<String> cd = new HashSet<String>();
		cd.add("B Movie");

		Set<String> nnf = new HashSet<String>();
		nnf.add("F Movie");

		testGraph.insertVertex(bacon);
		testGraph.insertVertex(a);
		testGraph.insertVertex(c);
		testGraph.insertVertex(b);
		testGraph.insertVertex(d);
		testGraph.insertVertex(nobody);
		testGraph.insertVertex(nobodysFriend);

		testGraph.insertUndirected(bacon, a, BaconAndA);
		testGraph.insertUndirected(bacon, b, BaconAndB);
		testGraph.insertUndirected(a, b, AAndB);
		testGraph.insertUndirected(a, c, ac);
		testGraph.insertUndirected(b, c, bc);
		testGraph.insertUndirected(c, d, cd);
		testGraph.insertUndirected(nobody, nobodysFriend, nnf);

		System.out.println((testGraph));
		System.out.println(bfs(testGraph, bacon));
		System.out.println(getPath(bfs(testGraph, bacon), c));
		System.out.println(missingVertices(testGraph, bfs(testGraph, bacon)));
		System.out.println(averageSeparation(bfs(testGraph, bacon), bacon));

	}

}