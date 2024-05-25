package Oop.project.oop1.project.semantic.search;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * fix mistakes in user query
 */
public class SpellCheckerUtil extends QueryImprover{

    private static Set<String> dictionary = new HashSet<>();

    /**
     * get the words from the dictionary
     * @param dictionaryFilePath path of dictionary to work on
     */
    public static void initializeSpellChecker(String dictionaryFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(dictionaryFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                dictionary.add(line.trim().toLowerCase());
            }
            System.out.println("Spell checker initialized.");
        } catch (IOException e) {
            System.err.println("Error initializing spell checker: " + e.getMessage());
        }
    }

    /**
     * check each part of the phrase for mispelling
     * @param phrase phrase to check for spelling mistakes
     * @return mispelled words
     */
    public List<String> checkPhraseSpelling(String phrase) {
        List<String> misspelledWords = new ArrayList<>();
        String[] words = phrase.split("\\s+"); // Split by whitespace

        for (String word : words) {
            if (!checkSpelling(word.toLowerCase())) {
                misspelledWords.add(word);
            }
        }
        return misspelledWords;
    }

    /**
     * check for misspelling
     * @param word word to check for misspelling
     * @return true or false
     */
    public static boolean checkSpelling(String word) {
        return dictionary.contains(word.toLowerCase());
    }

    /**
     * get words you might mean if the query has a mistake
     * @param query mispelled word
     * @return list of suggestions
     */
    public List<String> getResult(String query) {
        List<String> suggestions = new ArrayList<>();
        // Generate suggestions based on edit distance from the misspelled word
        for (String dictWord : dictionary) {
            if (calculateEditDistance(query, dictWord) == 1) {
                suggestions.add(dictWord);
            }
        }
        return suggestions;
    }

    //Btehsob lfare2 bel ahrof ben kelme w kelme la tchuf ade lchabah
    /**
     * calculate similarity between two words
     * @param word1 first word
     * @param word2 second word
     * @return check distance between the two words
     */
    private static int calculateEditDistance(String word1, String word2) {
        int[][] dp = new int[word1.length() + 1][word2.length() + 1];
        for (int i = 0; i <= word1.length(); i++) {
            for (int j = 0; j <= word2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[word1.length()][word2.length()];
    }



}