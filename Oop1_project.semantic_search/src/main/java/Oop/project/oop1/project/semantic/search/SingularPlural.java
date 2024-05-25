package Oop.project.oop1.project.semantic.search;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * if a word is singular it give the plural and vice versa
 */
public class SingularPlural {

    /**
     * if a word is singular it give the plural and vice versa
     * @param input word to get the opposite
     * @return if plural returns the singular and vice versa
     * @throws IOException ...
     */
    public static String singularPlural(String input) throws IOException {
        Map<String, String> singularPlural = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Lenovo\\Downloads\\Oop1_project.semantic_search\\singular_plural.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    singularPlural.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        if (singularPlural.containsKey(input)) {
            return singularPlural.get(input);
        } else {
            for (Map.Entry<String, String> entry : singularPlural.entrySet()) {
                if (entry.getValue().equals(input)) {
                    return entry.getKey();
                }
            }
        }
        // If the input is not found in the map, modify it according to the user's request
        if (input.endsWith("s")) {
            String singularForm = input.substring(0, input.length() - 1);
            return singularForm;
        } else {
            return (input+"s");
        }
    }

}
