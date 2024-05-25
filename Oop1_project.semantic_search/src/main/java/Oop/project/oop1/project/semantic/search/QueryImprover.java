package Oop.project.oop1.project.semantic.search;


import java.util.List;

/**
 * parent class for query mods
 */
public abstract class QueryImprover {
    /**
     * get result needed in the child classes
     * @param query to get result for
     * @return list of strings with th result
     * @throws Exception ..
     */
    public abstract List<String> getResult(String query)throws Exception;

    /**
     * check if a letter is vowel
     * @param ch character to check if is a vowel
     * @return true or false
     */
    public static boolean isVowel(char ch) {
        return "aeiou".indexOf(Character.toLowerCase(ch)) != -1;
    }
}
