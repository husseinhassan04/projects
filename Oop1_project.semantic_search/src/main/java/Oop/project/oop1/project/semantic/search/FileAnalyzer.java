package Oop.project.oop1.project.semantic.search;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * analyze documents
 */
public class FileAnalyzer{


    private final TokenizerME tokenizer;
    private final POSTaggerME posTagger;
    private final NameFinderME nameFinder;
    private final Directory index;
    private List<String> documents;
    private Map<String, String> documentContents;


    /**
     *  constructor for the analyzer
     * @throws IOException ...
     */
    public FileAnalyzer() throws IOException {

        this.tokenizer = loadTokenizerModel();
        this.posTagger = loadPosModel();
        this.nameFinder = loadNERModel();
        this.index = new RAMDirectory();
        this.documents = new ArrayList<>();
        this.documentContents = new HashMap<>();
        addDocumentsFromPath();

    }

    /**
     * to load the model
     * @return TokenizerModel
     * @throws IOException ...
     */
    private TokenizerME loadTokenizerModel() throws IOException {

        try (InputStream modelIn = new FileInputStream("C:\\Users\\Lenovo\\Downloads\\Oop1_project.semantic_search\\opennlp-en-ud-ewt-tokens-1.0-1.9.3.bin")) {
            TokenizerModel model = new TokenizerModel(modelIn);
            return new TokenizerME(model);
        }
    }

    /**
     * to load the model
     * @return POSModel
     * @throws IOException ...
     */
    private POSTaggerME loadPosModel() throws IOException {
        try (InputStream modelIn = new FileInputStream("C:\\Users\\Lenovo\\Downloads\\Oop1_project.semantic_search\\en-pos-maxent.bin")) {
            POSModel model = new POSModel(modelIn);
            return new POSTaggerME(model);
        }
    }

    /**
     * to load the model
     * @return NERModel
     * @throws IOException ...
     */
    private NameFinderME loadNERModel() throws IOException {
        try (InputStream modelIn = new FileInputStream("C:\\Users\\Lenovo\\Downloads\\Oop1_project.semantic_search\\en-ner-person.bin")) {
            TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
            return new NameFinderME(model);
        }
    }

    //public method laeno 3tezneha bel main
    //bt chil ponctuation w lkalimet li m bet asser bel search

    /**
     * to remove stops word and ponctuation
     * @param text to process
     * @return text ater removing stop words and commas..
     */
    public String preprocessText(String text) {


        text = text.toLowerCase();

        //la nchil kel chi gher ahrof w ar2am (no2at msln)
        text = text.replaceAll("[^a-zA-Z0-9\\s]", "");

        // Removing stop words (you can add more stop words as needed)
        String[] stopWords = {"the ", "is ", "and ", "or ", "it ", "in ", "on ", "at ", "to ", "for ", "what ", "where ", "when ", "why ", "who ",
                "a ","this ","whose ","each ","did ","could ","would ","as ","any ","are ","do ","did ","does ","such ","these ","me ","provide ",
                "give ","tell ","these ","many ","some ","most ","must ","an ","how ","my ","our ","his ",
                "will ","whatever ","whenever ","however ","so ","just ","even ","definitely ","exactly ","precisely ","only ","have "};
        for (String stopWord : stopWords) {
            text = text.replaceAll("\\b" + stopWord + "\\b", "");
        }

        return text;
    }

    /**
     * * Indexes the documents by creating a Lucene index.
     *  *
     *  * This method iterates over the list of documents, preprocesses each document's content
     *  * using the {@code analyzeTextContent} method, and then adds the preprocessed content to
     *  * a Lucene document. The documents are indexed using an instance of {@code IndexWriter} with
     *  * a specified {@code StandardAnalyzer}.
     *
     * @throws IOException ...
     */
    private void indexDocuments() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter writer = new IndexWriter(index, config);

        for (String document : documents) {
            Document luceneDoc = new Document();

            // Preprocess the document content using analyzeTextContent and write it to a StringWriter
            StringWriter preprocessedContent = new StringWriter();
            PrintWriter out = new PrintWriter(preprocessedContent);
            analyzeTextContent(document, out);
            out.close();

            // Add the preprocessed content to the Lucene document
            luceneDoc.add(new TextField("content", preprocessedContent.toString(), Field.Store.YES));
            writer.addDocument(luceneDoc);
        }
        writer.close();
    }


    /**
     * does opennlp tasks tokenization pos and ner
     * loads the result to the printWriter
     * @param textContent content to analyse
     * @param out PrintWriter
     */
    private void analyzeTextContent(String textContent, PrintWriter out) {
        String[] tokens = tokenizer.tokenize(textContent);
        String[] tags = posTagger.tag(tokens);
        Span[] entities = nameFinder.find(tokens);


        for (int i = 0; i < tokens.length; i++) {
            boolean isPartOfEntity = false;
            StringBuilder entityTags = new StringBuilder(); // To store multiple entity types if token is part of multiple entities
            for (Span entity : entities) {
                if (i >= entity.getStart() && i < entity.getEnd()) {
                    entityTags.append(entity.getType()).append(" ");
                    isPartOfEntity = true;
                }
            }
            if (isPartOfEntity) {
                out.println(tokens[i] + "\t" + tags[i] + "\t" + entityTags.toString().trim());
            } else {
                out.println(tokens[i] + "\t" + tags[i] + "\t" + "O");
            }
        }
    }

    /**
     * gets info from csv ad put it in the attribute: documents
     * @param filePath path of csv file
     * @throws IOException ...
     */
    private void analyzeCsvFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {

                content.append(line.replaceAll(",", " ")).append("\n");
            }

            documents.add(content.toString().trim());
        }
    }

    /**
     * put the txt content in the documents attribute of the file analyzer
     * @param filePath path of txt file
     * @throws IOException ...
     */
    private void analyzeTextFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
            documents.add(content.toString());
        }
    }

    /**
     * put the pdf content in the documents attribute of the file analyzer
     * @param filePath path of the pdf
     * @throws IOException ...
     */
    private void analyzePdfFile(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String content = stripper.getText(document);
            documents.add(content);
        }
    }

    /**
     * does the indexing of the documents
     * @throws IOException ...
     */
    public void preprocessDocuments() throws IOException {
        indexDocuments();
    }

    /**
     *  * Searches the Lucene index for documents matching the given query.
     *  *
     *  * This method opens an IndexReader and creates an IndexSearcher to search the index. It parses
     *  * the provided query using a QueryParser and searches for documents containing the query terms
     *  * in the specified field ("content"). It retrieves the top matching documents and returns their
     *  * document indices as a list of integers.
     *
     * @param query user query or any modification on it
     * @return list of indexes
     * @throws IOException ...
     * @throws ParseException ...
     */
    public List<Integer> search(String query) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        Query luceneQuery = new QueryParser("content", new StandardAnalyzer()).parse(query);
        TopDocs topDocs = searcher.search(luceneQuery, 10);
        List<Integer> rankedIndices = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            rankedIndices.add(scoreDoc.doc);
        }
        reader.close();
        return rankedIndices;
    }

    /**
     * specify the document of the lucene index
     * @param index index generated by lucene
     * @return document
     */
    public String getDocument(int index) {
        if (index >= 0 && index < documents.size()) {
            return documents.get(index);
        } else {
            return null;
        }
    }

    /**
     * add all the document of the path to search files in
     */
    private void addDocumentsFromPath() {
        File directory = new File("C:\\Users\\Lenovo\\Downloads\\Oop1_project.semantic_search\\filesToCheck");
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Invalid directory path: " + "C:\\Users\\Lenovo\\Downloads\\Oop1_project.semantic_search\\filesToCheck");
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        String content = "";
                        if (file.getName().endsWith(".pdf")) {
                            try (PDDocument document = PDDocument.load(file)) {
                                PDFTextStripper stripper = new PDFTextStripper();
                                content = stripper.getText(document);
                            }
                        } else if (file.getName().endsWith(".csv")) {
                            BufferedReader csvReader = new BufferedReader(new FileReader(file));
                            StringBuilder csvContent = new StringBuilder();
                            String row;
                            while ((row = csvReader.readLine()) != null) {
                                csvContent.append(row).append("\n");
                            }
                            csvReader.close();
                            content = csvContent.toString();
                        } else if (file.getName().endsWith(".txt")) {
                            content = new String(Files.readAllBytes(file.toPath()));

                            documentContents.put(file.getName(), content);
                        } else {

                            System.err.println("Unsupported file format: " + file.getAbsolutePath());
                            continue; //byeemalo skip
                        }

                        documentContents.put(file.getName(), content);
                    } catch (IOException e) {
                        System.err.println("Error reading file: " + file.getAbsolutePath());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.err.println("No files found in directory: " + "C:\\Users\\Lenovo\\Downloads\\Oop1_project.semantic_search\\filesToCheck");
        }
    }

    /**
     * checks the similarity between two strings
     * used for debugging purpose
     *  ken yhot fare2 ben lcontenue lal file li mahtut bel documents w ben li mahtut bel hashmap
     *  fi \n\n zyede bs m laayneha fa hatayneha krml iza fare2 5 yeetebron nfs lchi satren zyede hajmon 2
     *  byekhod 2 azghar men 5 fa byeetebro nfs lfile
     * @param s1 first word
     * @param s2 second word
     * @return distance between s1 and s2
     */
    public int calculateDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * get the minimum
     * @param a first int
     * @param b second int
     * @param c third int
     * @return min between a b c
     */
    private static int min(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }

    // Method to check if a given content matches any document content and return its filename

    /**
     * ater specifying the document using lucene we use it to specify the name from the hashmap
     * hashmap contains each document name and its content
     * @param content document content
     * @return name of the document
     */
    public String getDocumentName(String content) {

        content= content.replaceAll(","," ");
        for (Map.Entry<String, String> entry : documentContents.entrySet()) {
            String value = entry.getValue().replaceAll(","," ");
            if (calculateDistance(content, value)<5) {
                return entry.getKey();
            }
        }
        return "Couldn't specify file name";
    }

    /**
     * function to do analysis on each file in the directory based on the type
     * @param directoryPath path of the dictionary
     * @throws IOException ...
     */
    public void analyzeDirectory(String directoryPath) throws IOException{
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        if (file.getName().endsWith(".csv")) {
                            analyzeCsvFile(file.getAbsolutePath());
                        } else if (file.getName().endsWith(".txt")) {
                            analyzeTextFile(file.getAbsolutePath());
                        } else if (file.getName().endsWith(".pdf")) {
                            analyzePdfFile(file.getAbsolutePath());
                        }else {
                            System.err.println("Unsupported file format: " + file.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        System.err.println("Error analyzing file: " + file.getAbsolutePath());
                        e.printStackTrace();
                    }

                }
            }
        }
        else {
            System.err.println("Error: Directory not found - " + directoryPath);
        }
    }

}