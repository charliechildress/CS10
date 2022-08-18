import java.awt.*;

/**
 * Parses messages and returns an object that contains Id and Shape
 * @author Franklin Ruan, Charlie Childress, Fall 2021, Dartmouth CS10
 */
public class StringParser {

    /**
     * Return Map containing ID and Shape
     * @return
     */
    public static synchronized IdAndShape makeShapeWithId(String s){
        // instantiate the shape to null
        Shape shape = null;

        // parse the message and save it's parts appropriately
        String[] message = s.split(" ");

        String shapeType = message[0];
        Integer x1 = Integer.parseInt(message[1]);
        Integer y1 = Integer.parseInt(message[2]);
        Integer x2 = Integer.parseInt(message[3]);
        Integer y2 = Integer.parseInt(message[4]);
        Integer rgb = Integer.parseInt(message[message.length-2]);
        Double id = Double.parseDouble(message[message.length-1]);

        // deal with different shape types by creating their respective shapes
        if (shapeType.equals("ellipse")){
            shape = new Ellipse(x1, y1, x2, y2, new Color(rgb));
        }
        if (shapeType.equals("rectangle")){
            shape = new Rectangle(x1, y1, x2, y2, new Color(rgb));
        }
        if (shapeType.equals("segment")){
            shape = new Segment(x1, y1, x2, y2, new Color(rgb));
        }
        if (shapeType.equals("polyline")){
            shape = new Polyline(x1, y1, new Color(rgb));
            for (int i = 3; i < message.length-2; i+=2){ // ensure capturing all the points in polyline
                ((Polyline)shape).setEnd(Integer.parseInt(message[i]), Integer.parseInt(message[i+1]));
            }

        }

        IdAndShape idShape = new IdAndShape(id, shape);
        // return shape
        return idShape;
    }




}
