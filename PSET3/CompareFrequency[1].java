/**
 * Node information and compare function set-ups
 * @author Charlie Childress, Dartmouth CS 10, Fall 2021, using material given on course webpage
 */

public class CompareFrequency implements Comparable<CompareFrequency>{
    // instance variables for frequency and character
    private int frequency;
    private Character character;

    public CompareFrequency(Character character, int frequency){
        this.frequency = frequency;
        this.character = character;
    }

    // establish the getters and setter for characters and frequencies
    public Character getCharacter(){
        return character;
    }

    public Integer getFrequency(){
        return frequency;
    }

    public void setCharacter(Character character){
        this.character = character;
    }

    public void setFrequency(int frequency){
        this.frequency = frequency;
    }

    /**
     * Compare two tree nodes to determine which node occurs more frequently
     * @param tree which you want to get the node from the compare with
     * @return 1, 0, or -1 based on which node is larger
     *
     */

    @Override
    public int compareTo(CompareFrequency tree) {
        if (this.frequency > tree.frequency) {
            return 1;
        }
        if (this.frequency == tree.frequency) {
            return 0;
        }
        else{
            return -1;
        }
    }

    // return a string of the character and its frequency in the text
    @Override
    public String toString(){
        return character + ": " + frequency;
    }
}
