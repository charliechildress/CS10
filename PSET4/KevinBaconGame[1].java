import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Devise the interface for playing the Kevin Bacon game
 *
 * @author Charlie Childress & Kirill Lanski, Dartmouth CS 10, Fall 2021, using material given on cs10 webpage
 */

public class KevinBaconGame {
    public Map<String, String> actorsIDs;
    public Map<String, String> moviesIDs;
    public Map<String, ArrayList<String>> moviesAndActors;
    public static Graph<String, Set<String>> actorsGraph;
    public static Graph<String, Set<String>> pathTree;
    public static String center;

    /**
     * constructor to build the necessary maps and graph
     */
    public KevinBaconGame() {
        try {
            this.actorsIDs = loadActorsOrMovies("PS4/actors.txt"); // Map where actors ID is key and name is value
            this.moviesIDs = loadActorsOrMovies("PS4/movies.txt"); // Map where movies ID is key and name is value
            this.moviesAndActors = loadMovieActors("PS4/movie-actors.txt"); // Map where movies ID is key and set of actors in movie is value
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.actorsGraph = new AdjacencyMapGraph<>(); // instantiate new graph
        // for all actorsIDs in the actorIDS map, get their names and create a vertex for each actors name
        for (String actorID : actorsIDs.keySet()) {
            actorsGraph.insertVertex(actorsIDs.get(actorID));
        }
        // for each movieID, get the list of actors and the movies name and create an edge between each actor in the movie using the movies name
        for (String movieID : moviesAndActors.keySet()) {
            ArrayList<String> actorList = moviesAndActors.get(movieID);
            String movieName = moviesIDs.get(movieID);
            for (String actorID : actorList) {
                String actor1 = actorsIDs.get(actorID);
                for (String s : actorList) {
                    String actor2 = actorsIDs.get(s);
                    if (!s.equals(actorID)) { // double for loop and check so that an actor does not create an edge with themselves
                        // if the actors do not have an edge yet, create a new set of movie edges between the two actors and add this movie to the set
                        if (!actorsGraph.hasEdge(actor1, actor2)) {
                            Set<String> movieEdgeSet = new HashSet<>();
                            movieEdgeSet.add(movieName);
                            actorsGraph.insertUndirected(actor1, actor2, movieEdgeSet);
                        }
                        // if actors already have an edge, update this movie name to the edge
                        else {
                            actorsGraph.getLabel(actor1, actor2).add(movieName);
                        }
                    }
                }
            }
        }
        newCenterOfUniverse("Kevin Bacon");

    }

    /**
     * read the file and produce a Map of the actors or movies that has their ID number as Key and name as Value
     *
     * @param pathName -- file name (will use actors.txt or movie.txt)
     * @return -- a Map with actors or movies ID number as key and name as value
     * @throws Exception -- if error reading the file
     */
    public Map<String, String> loadActorsOrMovies(String pathName) throws Exception {
        Map<String, String> IDMap = new HashMap<>();
        BufferedReader input;
        input = new BufferedReader(new FileReader(pathName));
        String actorOrLine;
        try {
            //read each line in file, split the line to create an index for actor ID and index for actor name
            while ((actorOrLine = input.readLine()) != null) {
                String[] actorID = actorOrLine.split("\\|");
                IDMap.put(actorID[0], actorID[1]); //add to map with ID as key and name as value

            }
        } catch (IOException e) {
            System.err.println("An error occurred when trying to read this file: " + e.getMessage());
        } finally {
            input.close();
        }
        return IDMap;
    }

    /**
     * read the file and produce a Map that has a movies ID number as Key and an array of all the IDs
     * of each actor in the movie the as the Value
     *
     * @param pathName -- file name (will use movie-actors.txt)
     * @return -- a Map with a movies ID number as key and an array list of all actors in the movie as the value
     * @throws Exception -- if error reading the file
     */
    public Map<String, ArrayList<String>> loadMovieActors(String pathName) throws Exception {
        Map<String, ArrayList<String>> moviesMap = new HashMap<>();
        BufferedReader input;
        input = new BufferedReader(new FileReader(pathName));
        String movieAndActorLine;
        try {
            while ((movieAndActorLine = input.readLine()) != null) {
                String[] movieAndActorsIDS = movieAndActorLine.split("\\|");
                ArrayList<String> actors;
                // if the movie ID is not already in the Map, create a new array list to hold the actors to use as value in map
                if (!moviesMap.containsKey(movieAndActorsIDS[0])) {
                    actors = new ArrayList<>();
                }
                // else just get the current actors list and add to it
                else {
                    actors = moviesMap.get(movieAndActorsIDS[0]);
                }
                actors.add(movieAndActorsIDS[1]);
                moviesMap.put(movieAndActorsIDS[0], actors);
            }
        } catch (IOException e) {
            System.err.println("An error occurred when trying to read this file: " + e.getMessage());
        } finally {
            input.close();
        }
        return moviesMap;
    }

    /**
     * compare the separation of the vertices in the graph and return a specified number of either the top or the
     * bottom vertices
     * @param num -- number of centers of the universe
     */
    public static void centersOfUniverse(int num) {

        class Node implements Comparable<Node> {
            private double separation;
            private String actor;

            // create a node for each actor and hold the value of its separation from the center
            public Node(String s) {
                this.actor = s;
                separation = GraphLib.averageSeparation(GraphLib.bfs(actorsGraph, actor), actor);
            }

            @Override
            public String toString() {
                return actor + ": " + separation;
            }

            // compare the separation of the node to the center to the separation of another node to the center
            @Override
            public int compareTo(Node other) {
                if (this.separation > other.separation) return 1;
                else if (this.separation == other.separation) return 0;
                else return -1;
            }
        }

        // add all vertices to a list from bfs
        ArrayList<Node> allVerticesList = new ArrayList<>();
        for (String vertex : GraphLib.bfs(actorsGraph, "Kevin Bacon").vertices()) {
            allVerticesList.add(new Node(vertex));
        }

        Collections.sort(allVerticesList);  // sort according to separation numbers
        // if num is positive, output the lowest i indices
        if (num > 0) {
            for (int i = 0; i < num; i++) {
                System.out.println(allVerticesList.get(i));
            }
        }
        // if num is negative, output the highest i indices
        else {
            for (int i = 1; i <= Math.abs(num); i++) {
                System.out.println(allVerticesList.get(allVerticesList.size() - i));
            }
        }
    }

    /**
     *
     * @param low
     * @param high
     * @return
     */
    public static List<String> degreesSort(int low, int high) {
        List<String> verticeDegrees = new ArrayList<String>();
        for (String v : actorsGraph.vertices()) {
            if (actorsGraph.inDegree(v) >= low && actorsGraph.inDegree(v) <= high) {
                verticeDegrees.add(v); // create a list of vertices
            }
        }
        // call sort which compares two vertices degrees ints

        verticeDegrees.sort((v1, v2) -> actorsGraph.inDegree(v1) - actorsGraph.inDegree(v2));
        return verticeDegrees;
    }

    /**
     * Use GraphLib method missing vertices to output all vertices that are not connected to the center
     */
    public static void infiniteSeparation() {
        Set<String> missingVertices = GraphLib.missingVertices(actorsGraph, pathTree);
        System.out.println(missingVertices);
    }

    /**
     * find the path from a given vertex to the current center of the game universe
     * @param name -- actor name which you want to find path for
     */
    public static void findPath(String name) {
        List<String> path = GraphLib.getPath(pathTree, name);
        System.out.println(path);
    }

    /**
     * List actors sorted by non-infinite separation from the current center, with separation between low and high
     * @param low -- low boundary of separation from the current center
     * @param high -- high boundary of separation from the current center
     * @return -- list of vertices
     */
    public static List<String> noninfiniteSeparation(int low, int high) {
        List<String> verticeSep = new ArrayList<String>();
        // for all vertices in the path, check their separation from the current center
        for (String v : pathTree.vertices()) {
            int size = GraphLib.getPath(pathTree, v).size();
            // if the vertex fits the separation boundaries, then add to the list to later sort
            if (size >= low && size <= high) {
                verticeSep.add(v); // create a list of vertices
            }
        }
        // call sort which compares two vertices separations ints

        verticeSep.sort((v1, v2) -> GraphLib.getPath(pathTree, v1).size() - GraphLib.getPath(pathTree, v2).size());
        return verticeSep;
    }

    /**
     * Make a new actor the center of the game universe
     * @param name -- string of an actors name
     */
    public static void newCenterOfUniverse(String name) {
        center = name;
        pathTree = GraphLib.bfs(actorsGraph, center);   // start a new path tree with the new name as center
        System.out.println(name + " is now the center of the acting universe, connected to " + pathTree.numVertices() + "/" + actorsGraph.numVertices() + " actors with average separation " + GraphLib.averageSeparation(pathTree, center));

    }

    /**
     * Go through the different commands and inputs allowed by the Kevin Bacon game
     * @param args
     */
    public static void main(String[] args) {

        KevinBaconGame game = new KevinBaconGame();

        // set boolean to true and run game while it is true, when you want to quit, change boolean to false
        boolean playing = true;
        while (playing) {

            // print out all the commands
            System.out.println("Commands: \n" +
                    "c <#>: list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation\n" +
                    "d <low> <high>: list actors sorted by degree, with degree between low and high\n" +
                    "i: list actors with infinite separation from the current center\n" +
                    "p <name>: find path from <name> to current center of the universe\n" +
                    "s <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high\n" +
                    "u <name>: make <name> the center of the universe\n" +
                    "q: quit game");

            Scanner scanner = new Scanner(System.in);
            String userInput = scanner.nextLine();
            String command = userInput.substring(0, 1); // command is the first letter of the user input
            // match the input to the right command
            // if the input is "c":
            if (command.equalsIgnoreCase("c")) {
                try {
                    int num = Integer.parseInt(userInput.substring(2));
                    // if num is 0, just return an empty list
                    if (num == 0){
                        ArrayList<String> emptyList = new ArrayList<>();
                        System.out.println(emptyList);
                    }
                    else{
                         // characters after the intial commands letter turn into integer for how long the list show be
                        centersOfUniverse(num);
                    }
                } catch (Exception e) {
                    System.out.println("Invalid input");
                }
            // if the input is "d":
            } else if (command.equalsIgnoreCase("d")) {
                try {
                    // create a list split by space so that index zero will be the low and index one will be the high
                    String[] nums = userInput.substring(2).split(" ");
                    int low = Integer.parseInt(nums[0]);
                    int high = Integer.parseInt(nums[1]);
                    List<String> result = degreesSort(low, high);
                    System.out.println(result);
                } catch (Exception e) {
                    System.out.println("Invalid input");
                }
            } else if (command.equalsIgnoreCase("i")) {
                infiniteSeparation();
            } else if (command.equalsIgnoreCase("p")) {
                if (userInput.length() < 2) { // does not run without the name
                    System.out.println("Not a valid input");
                } else {
                    String name = userInput.substring(2);
                    if (!actorsGraph.hasVertex(name)) { // if the actor does not exist, then it cannot run
                        System.out.println("Not an existing actor, try again");
                    } else {
                        findPath(name);
                    }
                }
            // if the input is "s":
            } else if (command.equalsIgnoreCase("s")) {
                try {
                    String[] nums = userInput.substring(2).split(" ");
                    int low = Integer.parseInt(nums[0]);
                    int high = Integer.parseInt(nums[1]);
                    List<String> result = noninfiniteSeparation(low, high);
                    System.out.println(result);
                } catch (Exception e) {
                    System.out.println("Invalid input");
                }
            } else if (command.equalsIgnoreCase("u")) {
                if (userInput.length() < 2) {
                    System.out.println("Not a valid input");
                } else {
                    String name = userInput.substring(2);
                    if (!actorsGraph.hasVertex(name)) {
                        System.out.println("Not an existing actor, try again");
                    } else {
                        newCenterOfUniverse(name);
                    }
                }
            // if the input is "q"
            } else if (command.equalsIgnoreCase("q")) {
                playing = false; // make the boolean false and stop playing
            } else {
                System.out.println("Invalid");
            }
        }
    }
}