package Oop.project.oop1.project.semantic.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * get list of phrases that matches the query
 */
public class Output {
    /**
     * get the lines that contains the word in the document
     * @param documentContent document that we analyzed previously
     * @param query word to get the lines that contains it in the document
     * @return list of phrases
     * @throws IOException ...
     */

    public static List<String> getPhrase(String documentContent,String query) throws IOException {

        ArrayList<String> result = new ArrayList<String>();
        String[] phrases = documentContent.split("\n");
        String[] queryParts = query.split(" ");
        for(String phrase : phrases){
            String loweredPhrase=phrase.toLowerCase();
            for(String part : queryParts) {
                if (loweredPhrase.contains(part) || loweredPhrase.contains(SingularPlural.singularPlural(part))) {
                    result.add(phrase);
                }
            }
        }
        if(!result.isEmpty()) {
            return result;
        }
        //ehtiyat iza sar mechkle w m le2a ljomle byaatine lcontent kello
        else{
            result.add(documentContent);
            return result;
        }
    }

    /**
     * print the phrases to the console
     * @param phrases phrases to print
     */
    public static void printPhrases(List<String> phrases){
        for(String phrase : phrases){
            System.out.println(phrase);
        }

    }


}
