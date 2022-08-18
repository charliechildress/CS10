import java.util.Comparator;

/**
 * Comparator that implements the CompareFrequency class and compares two binary trees data
 * @author Charlie Childress, Dartmouth CS 10, Fall 2021, using material given on course webpage
 */

class TreeComparator implements Comparator<BinaryTree<CompareFrequency>> {
    @Override
    public int compare(BinaryTree<CompareFrequency> t1, BinaryTree<CompareFrequency> t2) {
        return t1.data.compareTo(t2.data);
    }

}