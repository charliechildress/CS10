import org.w3c.dom.CharacterData;

import java.io.*;
import java.security.Key;
import java.util.*;
import java.util.zip.InflaterInputStream;

/**
 * Use Huffman encoding to compress and decompress files.
 * @author Charlie Childress, Dartmouth CS 10, Fall 2021, using material given on course webpage
 *
 */

public class Huffman {
    private Map<Character, String> codeMap;

    public Huffman() {

    }

    /**
     * Read the text in a file and run through the file, returning an ArrayList of all the characters in the file
     * Return error if problem occurs
     *
     * @param fileName
     * @return an array list of all characters in a file
     * @throws IOException
     */
    public ArrayList<Character> readFileCharacters(String fileName) throws IOException {
        ArrayList<Character> characterArrayList = new ArrayList<Character>();
        BufferedReader input = new BufferedReader(new FileReader(fileName));

        try {
            int characterInteger = input.read();
            while (characterInteger != -1) {    // read through the file until there are no chararacters left to read
                char character = (char) characterInteger;
                characterArrayList.add(character);
                characterInteger = input.read();
            }
        } catch (IOException e) {
            System.err.println("An error occurred when trying to read this file: " + e.getMessage());
        } finally {
            input.close();      // close the file after reading it
        }
        return characterArrayList;
    }

    /**
     * establish a new hash map to store the characters and their frequencies
     * for each character in the character ArrayList, check if the hash map contains the character already
     * if it does, increase the frequency by one, if not, add the character to the map with a frequency of 1
     *
     * @param charList - an ArrayList of all characters in a file
     * @return frequencyMap - a hash map storing each character from the array list as the key and its frequency as value
     */
    public Map characterMap(ArrayList<Character> charList) {
        Map<Character, Integer> frequencyMap = new HashMap<>();

        for (Character character : charList)
            if (frequencyMap.containsKey(character)) {
                frequencyMap.put(character, frequencyMap.get(character) + 1);
            } else {
                frequencyMap.put(character, 1);
            }

        return frequencyMap;

    }

    /**
     * Take the hash map of characters and frequency, use the Comparator class to compare frequency of each character
     * Then create a multiple binary trees for each entry of compared nodes
     * Finally create a priority queue of all binary trees
     *
     * @param frequencyMap - a hash map storing each character from the array list as the key and its frequency as value
     * @return initialTrees - a priority queue of all the binary trees
     */
    public PriorityQueue<BinaryTree<CompareFrequency>> initialTree(Map<Character, Integer> frequencyMap) {

        // initialize comparator and priority queue
        Comparator<BinaryTree<CompareFrequency>> treeComparator = new TreeComparator();
        PriorityQueue<BinaryTree<CompareFrequency>> initialTrees = new PriorityQueue<BinaryTree<CompareFrequency>>(treeComparator);

        // compare each character and create binary trees, adding them to priority queue
        for (Character key : frequencyMap.keySet()) {
            CompareFrequency frequencyData = new CompareFrequency(key, frequencyMap.get(key));
            BinaryTree<CompareFrequency> initialTree = new BinaryTree<CompareFrequency>(frequencyData);
            initialTrees.add(initialTree);
        }
        return initialTrees;
    }

    /**
     * Take the priority queue of binary trees and put them all into an overarching Huffman encoding tree
     * Start with the lowest-frequency trees and work up
     *
     * @param initialTrees - a priority queue of all the binary trees
     * @return initialTrees.poll - a priority queue with nodes containing total frequency over with characters as
     * children, organized from largest to smallest
     */
    public static BinaryTree<CompareFrequency> createTree(PriorityQueue<BinaryTree<CompareFrequency>> initialTrees) {

        if (initialTrees != null) {
            while (initialTrees.size() > 1) {
                // remove the trees from the queue
                BinaryTree<CompareFrequency> t1 = initialTrees.poll();
                BinaryTree<CompareFrequency> t2 = initialTrees.poll();

                // Add them to a new tree with a node that contains t1 + t2 and has t1 on the left and t2 on the right
                CompareFrequency node = new CompareFrequency(null, t1.data.getFrequency() + t2.data.getFrequency());
                BinaryTree<CompareFrequency> t = new BinaryTree<CompareFrequency>(node, t1, t2);

                // Add the new tree to the queue
                initialTrees.add(t);
            }
        }
        if(initialTrees.size() == 1) {
            return new BinaryTree<CompareFrequency>(new CompareFrequency(null, initialTrees.peek().data.getFrequency()), initialTrees.peek(), null);
        }
        return initialTrees.poll();
    }


    /**
     * create a map that pairs characters with their string of '0' and '1's that describe the path from the root
     * to that character
     *
     * @param tree
     * @return this.codeMap - a map of the characters and their code string of numbers
     */
    public Map<Character, String> generateMap(BinaryTree<CompareFrequency> tree) {
        if (tree != null) {
            this.codeMap = new HashMap<Character, String>();

            String path = "";

            // helper method

            helpGenerateMap(path, this.codeMap, tree);
            return this.codeMap;
        }
        return null;
    }

    /**
     * Helper method to generateMap; it recursively goes down the tree to determine the string codes for each leaf and
     * their key-value entry
     * @param path
     * @param inputMap
     * @param tree
     */

    public void helpGenerateMap(String path, Map<Character, String> inputMap, BinaryTree<CompareFrequency> tree){
        if (tree.isLeaf()){
            inputMap.put(tree.data.getCharacter(), path);
        }
        else{
            if (tree.hasLeft()){
                helpGenerateMap(path + "0", inputMap, tree.getLeft());
            }
            if (tree.hasRight()){
                helpGenerateMap(path + "1", inputMap, tree.getRight());
            }
        }
    }

    /**
     * Read the file, get each character from the file and put it in an array, then turn each character in the array
     * into bits
     * @param initialFile
     * @param tree
     * @throws IOException
     */
    public void compressMap(String initialFile, Map<Character, String> tree) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(initialFile));
        BufferedBitWriter bitOutput = new BufferedBitWriter(initialFile.substring(0, initialFile.length()-4)+ "_compressed.txt");

        try{
            int charValue = input.read();
             while (charValue != -1){
                char c = (char)charValue;
                String temp = codeMap.get(c);
                char[] array= temp.toCharArray();

                for (char i: array) {
                    if (i == '0') {
                        bitOutput.writeBit(false);
                    }
                    else {
                        bitOutput.writeBit(true);
                    }
                }
                charValue = input.read();
            }
        }
        catch (IOException e){
            System.err.println("An error occurred when trying to read this file: " + e.getMessage());
        }
        finally{
            input.close();
            bitOutput.close();
        }

    }

    /**
     * read the file and turn each bit into a character and then put the chracters into a tree
     * @param initialFile
     * @param tree
     * @throws IOException
     */
    public void decompressMap(String initialFile, BinaryTree<CompareFrequency> tree) throws IOException {
        BufferedWriter output = new BufferedWriter(new FileWriter(initialFile.substring(0, initialFile.length()-4)+ "decompressed.txt")); // new name of file
        BufferedBitReader bitInput = new BufferedBitReader(initialFile.substring(0, initialFile.length()-4)+ "_compressed.txt");
        // read character by character
        // if you cant go anywhere (if leaf) --> go back to node
        // set traversal node back to top

        BinaryTree<CompareFrequency> traverseTree = tree;
        try{
            while (bitInput.hasNext()){
                boolean temp = bitInput.readBit();
                if (temp == false){
                    traverseTree = traverseTree.getLeft();
                }
                else {
                    traverseTree = traverseTree.getRight();
                }
                if (traverseTree.isLeaf()){
                    char test = traverseTree.getData().getCharacter();
                    output.write(traverseTree.getData().getCharacter());
                    traverseTree = tree;
                }
            }
        }
        catch (IOException e){
            System.err.println("An error occurred when trying to read this file: " + e.getMessage());
        }
        finally{
            bitInput.close();
            output.close();
        }
    }

    /**
     * Create a hash map of the characters in the file and call compressMap to compress the characters
     * @param filename
     * @throws IOException
     */
    public void compileCompression(String filename) throws IOException {
        Map map = characterMap(readFileCharacters(filename));
        BinaryTree<CompareFrequency> tempTree = createTree(initialTree(map));
        HashMap<Character, String> tempHashMap = (HashMap<Character, String>) generateMap(tempTree);

        compressMap(filename, tempHashMap);
    }

    /**
     * create a tree of all the characters in the file and call decompressMap to decompress the characters
     * @param filename
     * @throws IOException
     */
    public void compileDecompression(String filename) throws IOException {
        Map map = characterMap(readFileCharacters(filename));
        BinaryTree<CompareFrequency> tempTree = createTree(initialTree(map));
        decompressMap(filename, tempTree);
    }

    /**
     * Use test files to make sure the code is working correctly
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Huffman constitutionTest = new Huffman();
        constitutionTest.compileCompression("PS3/USConstitution.txt");
        constitutionTest.compileDecompression("PS3/USConstitution.txt");

        Huffman huffmanTest1 = new Huffman(); // empty file
        huffmanTest1.compileCompression("PS3/TestFile1.txt");
        huffmanTest1.compileDecompression("PS3/TestFile1.txt");

        Huffman huffmanTest2 = new Huffman(); // one character file
        huffmanTest2.compileCompression("PS3/TestFile2.txt");
        huffmanTest2.compileDecompression("PS3/TestFile2.txt");


        Huffman huffmanTest3 = new Huffman(); // one character repeated file
        huffmanTest3.compileCompression("PS3/TestFile3.txt");
        huffmanTest3.compileDecompression("PS3/TestFile3.txt");
    }
}