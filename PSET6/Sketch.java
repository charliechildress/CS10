import java.util.Collections;
import java.util.TreeMap;

/**
 * Sketch Class holds mapping between ID and Shape
 * Implemented with a tree map for sorted order
 * @author: Franklin Ruan, Charlie Childress, Dartmouth CS 10, Fall 2021
 */
public class Sketch {
    // INSTANCE VARIABLES -----------------------------
    TreeMap<Double, Shape> idToShape;

    // CONSTRUCTORS
    public Sketch(){
        idToShape = new TreeMap<Double, Shape>(); // create a new map
    }

    // METHODS

    /**
     * Getter method for TreeMap getIdToShape
     */
    public TreeMap<Double, Shape> getIdToShape() {
        return idToShape;
    }

    /**
     * puts key and value into instance map
     */
    public void put(Double d, Shape s){
        idToShape.put(d,s);
    }

    /**
     * gets value given key for instance map
     */
    public Shape get(Double d){
        return idToShape.get(d);
    }
}
