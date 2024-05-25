package Oop.project.oop1.project.semantic.search;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * get the synonyms of the query
 */
public class QueryAnalyzer extends QueryImprover{

    private static final String USER_AGENT = "Mozilla/5.0";

    /**
     * get the synonyms of a word
     * @param query query to get synonyms for
     * @return list of synonyms
     * @throws Exception
     */
    public List<String> getResult(String query) throws Exception {
        List<String> synonymsList = new ArrayList<>();

        String encodedWordToSearch = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.datamuse.com/words?rel_syn=" + encodedWordToSearch;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        StringBuilder response = new StringBuilder();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        ObjectMapper mapper = new ObjectMapper();

        try {
            ArrayList<Word> words = mapper.readValue(
                    response.toString(),
                    mapper.getTypeFactory().constructCollectionType(ArrayList.class, Word.class)
            );

            for (Word word : words) {
                synonymsList.add(word.getWord());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return synonymsList;
    }


    @Getter  //used from lombok
    static class Word {
        private String word;
        private int score;

    }
}
