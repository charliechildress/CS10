/**
 * Class that acts as an tuple that holds (Double) id and (Shape) shape
 * @author Franklin Ruan, Charlie Childress, Dartmouth CS10, Fall 2021
 */
public class IdAndShape {

    // INSTANCE FIELDS
    Double id;
    Shape shape;

    // CONSTRUCTOR
    public IdAndShape(Double id, Shape shape){
        this.id = id;
        this.shape = shape;
    }

    // METHODS

    /**
     * Setter for ID
     */
    public void setId(Double id) {
        this.id = id;
    }

    /**
     * Setter for Shape
     */
    public void setShape(Shape shape) {
        this.shape = shape;
    }

    /**
     * Getter for ID
     */
    public Double getId() {
        return id;
    }

    /**
     * Getter for Shape
     */
    public Shape getShape() {
        return shape;
    }
}
