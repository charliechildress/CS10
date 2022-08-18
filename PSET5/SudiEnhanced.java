import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


import static java.lang.Math.log;

/**
 * The following code creates a Part of Speech (POS Tagger) called Sudi
 *
 * We train Sudi by building a Hidden Markov Model (HMM) from a corpus of existing training data
 * This data can either be manually input or read through files
 *
 * We then use the Viterbi algorithm to predict the POS of each word in a sentence based on our Hidden Markov Model
 * The algorithm can identity POSs from either text files or manually input sentences
 *
 * @author Charlie Childress and Kirusha Lanski, PS-5, Fall 2021, using material given on CS10 course webpage
 */

public class SudiEnhanced {

    Map<String, Map<String, Double>> transitions; // keeps track of transitions ("edge" on graph), think of as a table on CS 10 PS-5 webpage
    Map<String, Map<String, Double>> observations; // keeps track of observations ("vertices" on graph), think of as a table on CS 10 PS-5 webpage


    /**
     * Adds data from a single sentence to the model to train it
     * Maps each word in the sentence to the tag associated with the word, given by the training data
     * Adds an entry for each word in both transitions & observations
     *
     * @param sentence a String, delimited by spaces, to be added to model
     * @param tag the tags, delimited by space, that correspond to each word in the sentence
     */
    public void trainSingleLine(String sentence, String tag) throws IOException {
        sentence.toLowerCase();

        String[] sentences = sentence.split(" "); // put each word into String array
        String[] tags = tag.split(" "); // put each tag into array


        // POPULATE BOTH MAPS

        for (int i = 0; i < sentences.length; i++) { // for each word in the sentence
            // TRANSITIONS
            if (i == 0) { // if this is the first word of the sentence
                if (!transitions.containsKey("#")) { // if this is the first word of entire training set
                    HashMap<String, Double> first = new HashMap<>(); // create a map for all the first words
                    first.put(tags[0], 1.0); // set freq off state of first word to 1
                    transitions.put("#", first); // add to map
                    // create total sum tracker
                    transitions.get("#").put("Total", 1.0);
                } else {
                    if (transitions.get("#").containsKey(tags[0])) { // if the state of first word of sentence has already been put into the map, add 1 to current freq
                        transitions.get("#").put(tags[0], transitions.get("#").get(tags[0]) + 1); // increase freq by 1
                    } else { // put the state into the map w/ freq of 1
                        transitions.get("#").put(tags[0], 1.0);
                    }
                    // add 1 to total sum in #
                    transitions.get("#").put("Total", transitions.get("#").get("Total") + 1);
                }
            } else {
                if (transitions.containsKey(tags[i - 1])) { // if the tag of previous has been input into map already
                    if (transitions.get(tags[i - 1]).containsKey(tags[i])) { // if the previous tag has established transition to current tag
                        transitions.get(tags[i - 1]).put(tags[i], transitions.get(tags[i - 1]).get(tags[i]) + 1); // increase freq by 1
                    } else { // create transition w/ freq of 1
                        transitions.get(tags[i - 1]).put(tags[i], 1.0);
                    }
                    // add 1 to total sum of tag
                    transitions.get(tags[i - 1]).put("Total", transitions.get(tags[i - 1]).get("Total") + 1);

                } else { // create new row for previous tag & add transition to new tag at freq 1
                    HashMap<String, Double> first = new HashMap<>(); // create a map for all the first words
                    first.put(tags[i], 1.0); // set freq off state of first word to 1
                    transitions.put(tags[i - 1], first); // add to map
                    // create total sum tracker
                    transitions.get(tags[i - 1]).put("Total", 1.0);

                }
            }

            // OBSERVATIONS
            if (!observations.containsKey(tags[i])) { // if the tag doesn't exist in the map yet
                HashMap<String, Double> first = new HashMap<>(); // create a map for all the first words
                first.put(sentences[i], 1.0); // set freq off state of first word to 1
                observations.put(tags[i], first); // add to map
                // create total sum tracker
                observations.get(tags[i]).put("Total", 1.0);
            } else { // if tag exists in map
                if (observations.get(tags[i]).containsKey(sentences[i])) { // if the tag already has the word
                    observations.get(tags[i]).put(sentences[i], observations.get(tags[i]).get(sentences[i]) + 1); // add 1 to freq of word
                } else { // add word to tag & set freq to 1
                    observations.get(tags[i]).put(sentences[i], 1.0);
                }
                // add 1 to total sum of tag
                observations.get(tags[i]).put("Total", observations.get(tags[i]).get("Total") + 1);
            }
        }
    }

    /**
     * After building observation and transition tables, this method loops through each entry of each row of the
     * tables to convert frequencies to probabilities and logarithmic values
     */
    public void calcProbabilies(){
        // CALCULATE PROBABILITIES & LOGS
        // sum freq for each row in transitions
        for (String row : transitions.keySet()) { // each row
            for (String tag : transitions.get(row).keySet()) { // each item in each row
                if (!tag.equals("Total"))
                    transitions.get(row).put(tag, log(transitions.get(row).get(tag) / transitions.get(row).get("Total"))); // convert frequencies to log probabilities
            }
            transitions.get(row).put("Total", 0.0); // set total log probability to 0 (log 1 = 0)
        }

        // same thing for observations
        for (String row : observations.keySet()) {
            for (String word : observations.get(row).keySet()) {
                if (!word.equals("Total")) {
                    observations.get(row).put(word, log(observations.get(row).get(word) / observations.get(row).get("Total")));
                }
            }
            observations.get(row).put("Total", 0.0);
        }
    }

    /**
     * Method allows us to build a HMM by reading text files to train the model
     * We first read the files, then call trainSingleLine() on each line with a loop to create the model
     *
     * @param filenameSentences a text file with properly formatted sentences
     * @param filenameTags the text file with tags that correspond to each word in the sentences text file
     */
    public void trainWithFilename(String filenameSentences, String filenameTags) throws IOException {
        transitions = new HashMap<>(); // reset & create new tables
        observations = new HashMap<>();

        BufferedReader inputSentences = new BufferedReader(new FileReader(filenameSentences)); // reader for sentence training files
        BufferedReader inputTags = new BufferedReader(new FileReader(filenameTags)); // reader for tags training files

        String lineSentences; // string that represents a line from the test file
        String lineTags; // string that represents a line from the tag file

        while ((lineSentences = inputSentences.readLine()) != null) { // while there is another line to read
            lineTags = inputTags.readLine(); // take next tag line too
            trainSingleLine(lineSentences, lineTags); // call trainSingleLine on the two sentence & tag
        }

        calcProbabilies(); // calculate log probabilities of observation and transition tables
    }

    /**
     * Viterbi algorithm is based on psuedocode on the CS10 course webpage, under October 29 lecture notes
     *
     * The algorithm uses the transition and observation tables created above to find the most likely combinations of POS
     * for a sentence using backtracking across the highest probabilities
     *
     * @param words an array of String that represents a sentence, each item is a word in the sentence
     * @return a list of String that hold the most likely POS state for each word in a sentence
     */
    public ArrayList<String> viterbi(String[] words) {
        Set<String> currStates = new HashSet<>(); // create set of current states
        currStates.add("#"); // add the starting state
        Map<String, Double> currScores = new HashMap<>(); // keep track current scores
        currScores.put("#", 0.0); // add starting score
        ArrayList<HashMap<String, String>> backtrack = new ArrayList<>(); // create backtracker that links states to previous states
        int backtrackCounter = -1; // create counter, initialize to -1
        ArrayList<String> finalStates = new ArrayList<>(); // what is returned

        for (String word : words) { // for each word in the sentence
            backtrack.add(new HashMap<>()); // add next item to backtracker
            backtrackCounter++;
            Set<String> nextStates = new HashSet<>(); // create set of next states
            Map<String, Double> nextScores = new HashMap<>(); // keep track of next scores
            for (String currState : currStates) { // for each state in the current state
                if (transitions.containsKey(currState)) { // if transition to new state exists
                     for (String nextState : transitions.get(currState).keySet()) { // for each transition from current state to new state
                        nextStates.add(nextState); // add new state to set of new states
                        double nextScore; // create score for next state
                        if (observations.containsKey(nextState)) { // if the next state is in observations
                            if (observations.get(nextState).containsKey(word)) { // if the state has the word
                                nextScore = currScores.get(currState) + transitions.get(currState).get(nextState) + observations.get(nextState).get(word); // add probabilities together
                            }
                            else { // add probabilities together, using unseen-word penalty of 17.5
                                nextScore = currScores.get(currState) + transitions.get(currState).get(nextState) - 17.5;
                            }
                            if (!nextScores.containsKey(nextState) || nextScore > nextScores.get(nextState)) { // if the state is either the first of its type in nextScores
                                // or if its score is better than the existing state's of its type in nextScore, put it and its score in nextScores
                                nextScores.put(nextState, nextScore);
                                backtrack.get(backtrackCounter).put(nextState, currState); // add link from next to current state into backtrack
                            }
                        }
                    }
                }
            }
            currStates = nextStates; // update current states to next
            currScores = nextScores; // update current scores to next
        }

        // after loop is done, find best end state (current state) using traditional for loop structure
        String bestState = null;
        for (String state : currScores.keySet()) {
            if (bestState == null) {
                bestState = state;
            } else if (currScores.get(state) > currScores.get(bestState)) { // if state is better than best state so far, update best state accordingly
                bestState = state;
            }
        }

        // backtrack to find most optimal combination of POSs of the words in a sentence
        for (int i = backtrack.size() - 1; i >= 0; i--) { // start traversing backwards
            finalStates.add(0, bestState); // add state to output list
            bestState = backtrack.get(i).get(bestState); // set best state to previous state
        }

        return finalStates;
    }

    /**
     * A method that compares Sudi's accuracy at classifying the POS of words in a text file to the actual classifications of those words,
     * to determine Sudi's efficacy
     *
     * @param finalnameTest the file of words Sudi will attempt to classify
     * @param finalnameTrue the actual classifications of the words in the file above
     * @return the percentage accuracy of Sudi at identifying POSs
     */
    public double countAccuracy(String finalnameTest, String finalnameTrue) throws IOException {

        double rightCount = 0.0; // how many words in the file Sudi classified correctly
        double wrongCount = 0.0; // how many words Sudi classifed incorrectly

        BufferedReader inputTest = new BufferedReader(new FileReader(finalnameTest)); // read in the test sentences file
        String lineTest;

        BufferedReader inputTrue = new BufferedReader(new FileReader(finalnameTrue)); // read in the true tags file
        String lineTrue;

        while ((lineTest = inputTest.readLine()) != null) { // for each sentence
            String[] lineSplitTest = lineTest.split(" "); // split sentence into array of words
            List<String> statesTest = viterbi(lineSplitTest); // run viterbi on the sentence

            lineTrue = inputTrue.readLine(); // split each tag line into array
            String[] lineSplitTrue = lineTrue.split(" ");

            for (int i = 0; i < statesTest.size(); i++){ // for each word in the sentence
                if (statesTest.get(i).equals(lineSplitTrue[i])) { // compare Sudi's tag with the actual tag
                    rightCount++;
                }
                else {
                    wrongCount++;
                }
            }

        }

        System.out.println("Number wrong: " + (int)wrongCount); // output the number Sudi got wrong
        return rightCount / (rightCount + wrongCount); // return the % accuracy of Sudi
    }

    /**
     * Method for manually training and testing Sudi (via console inputting)
     * Asks the user to input their own sentences and POSs associated with each word in the sentence
     * Outputs each word in the sentence next Sudi's classification of that word
     */
    public void consoleTesting() throws IOException {

        transitions = new HashMap<>(); // create and reset new tables
        observations = new HashMap<>();

        //TRAINING
        Scanner scanner = new Scanner(System.in); // initialize scanner
        ArrayList<String> sentences = new ArrayList<>(); // create list to store sentences
        ArrayList<String> tags = new ArrayList<>(); // create list to store tags of each word in a sentences

        System.out.println("\nConsole testing! Type your sentence, with each word and punctuation separated by spaces:"); // prompt user
        try {
            String currentLine; // user's input sentence
            String currentTag; // user's input tags
            while (!(currentLine = scanner.nextLine()).equals("TEST")) {
                System.out.println("Input tags, separated by spaces:");
                currentTag = scanner.nextLine().toUpperCase();
                sentences.add(currentLine); // add input sentence to list of sentences
                tags.add(currentTag); // add input tags to list of tags
                System.out.println("If you wish to move on and test your model type 'TEST' to test your model; otherwise, add another sentence: ");
            }
        }
        catch (Exception e){ // if sentences aren't typed in correctly, throw an exception
            System.out.println("Error, input not properly formatted with spaces!");
        }

        for (int i = 0; i < sentences.size(); i++){ // for each sentence in list
            trainSingleLine(sentences.get(i), tags.get(i)); // train model w/ sentence and tags
        }

        calcProbabilies(); // calculate probabilites for observation and transition tables

        // TESTING
        System.out.println("Type in your test sentence:");
        ArrayList<String[]> testSentences = new ArrayList<>(); // same list of sentences but for testing
        ArrayList<ArrayList<String>> allStates = new ArrayList<>(); // create a final list of all POSs for all input sentences

        String testSentence; // user's input test sentence
        while (!(testSentence = scanner.nextLine()).equals("DONE")) {

            testSentence.toLowerCase();
            String[] words = testSentence.split(" "); // split sentence into words
            testSentences.add(words); // add words to list
            System.out.println("Type 'DONE' if done adding sentences."); // keep adding sentences until 'DONE' is typed
        }

        for (String[] sentence : testSentences) {
            ArrayList <String> singleSentenceState = viterbi(sentence); // call viterbi on each sentence
            allStates.add(singleSentenceState); // add POSs of eacb sentence to collection of all POSs
        }

        // The output string
        String output = "";

        for (int i = 0; i < testSentences.size(); i++){ // for each sentence
            for (int j = 0; j < testSentences.get(i).length; j++){ // for each word in each sentence
                output += testSentences.get(i)[j] + "/" + allStates.get(i).get(j) + " "; // add the word and identified POS of the word to the output string
            }
            output += "\n";
        }

        System.out.println(output);
    }

    /**
     * Main method for testing (both file-driven and console-driven)
     */
    public static void main(String[] args) {
        SudiEnhanced test = new SudiEnhanced();

        // File-driven testing: Brown Corpus

        try {
            System.out.println("\nBrown Corpus:");
            test.trainWithFilename("PS-5/brown-train-sentences.txt", "PS-5/brown-train-tags.txt");
            System.out.println("Test accuracy: " + test.countAccuracy("PS-5/brown-test-sentences.txt" , "PS-5/brown-test-tags.txt") + "!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // File-driven testing: Simple test

        try {
            System.out.println("\nSimple test:");
            test.trainWithFilename("PS-5/simple-train-sentences.txt", "PS-5/simple-train-tags.txt");
            System.out.println("Test accuracy: " + test.countAccuracy("PS-5/simple-test-sentences.txt" , "PS-5/simple-test-tags.txt") + "!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        // Console-driven testing
        try {
            test.consoleTesting();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}


