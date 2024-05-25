package Oop.project.oop1.project.semantic.search;

import org.apache.lucene.queryparser.classic.ParseException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


import java.io.IOException;
import java.util.*;


@SpringBootApplication
public class Application {
	/**
	 * main function
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 */

	public static void main(String[] args) throws IOException, ParseException {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
		DataController dataController = context.getBean(DataController.class);

		// create instance of data to put in db
		Data data = null;


		Scanner scanInt = new Scanner(System.in);
		String dictionaryPath = "C:\\Users\\Lenovo\\Downloads\\Oop1_project.semantic_search\\spellFixerDictionary.txt";
		SpellCheckerUtil.initializeSpellChecker(dictionaryPath);

		final Set<String> printedFiles = new HashSet<String>();
		try {


			FileAnalyzer analyzer = new FileAnalyzer();

			analyzer.analyzeDirectory("C:\\Users\\Lenovo\\Downloads\\Oop1_project.semantic_search\\filesToCheck");


			analyzer.preprocessDocuments();

			for (int i = 0; i < 15; i++) {
				System.out.print("-_-");
			}
			System.out.println("\n \t \tWelcome to The Search Engine!!\n\n");
			int option;

			do {
				System.out.println("Your Query:");
				Scanner scan = new Scanner(System.in);
				String query = scan.nextLine();

				SpellCheckerUtil querySpell = new SpellCheckerUtil();
				List<String> misspelledWords = querySpell.checkPhraseSpelling(query);
				if (!misspelledWords.isEmpty()) {
					System.out.println("Misspelled words in the phrase:");
					for (String word : misspelledWords) {
						System.out.println("- " + word);
						List<String> suggestions =querySpell.getResult(word);
						if (!suggestions.isEmpty()) {
							System.out.println("  * Did you mean :");
							for (int i = 0; i < suggestions.size(); i++) {
								System.out.println("    " + (i + 1) + ". " + suggestions.get(i));
							}
							System.out.print("Choose a correction for '" + word + "' (enter the number or 0 to skip): ");
							int choice = scanInt.nextInt();
							if (choice > 0 && choice <= suggestions.size()) {
								query = query.replace(word, suggestions.get(choice - 1));
							}
						}
					}
					System.out.println("Corrected phrase: " + query);


				}
				query = analyzer.preprocessText(query);
				Data dataInMongo = dataController.getDataByUserQuery(query);
				if (dataInMongo!=null) {
                    System.out.println("\n\nData found in DataBase:\n");
                        for (Map.Entry<String, List<String>> entry : dataInMongo.getResult().entrySet()) {
                            String documentName = entry.getKey().replace(",", ".");
                            System.out.println("\n\nDocument: " + documentName + "\n");

                            List<String> contentList = entry.getValue();
                            System.out.println("Content: ");
                            for (String content : contentList) {
                                System.out.println(content);
                            }
                        }

                    int upOption;
                    do {
                        System.out.println("Do you want to update the data?");
                        System.out.println("1.Yes\n0.No");
                        upOption = scanInt.nextInt();
                    } while (upOption !=0 && upOption!=1);
					if(upOption==1){
						System.out.println("Enter the information you want to add:\n>>");
						String info = scan.nextLine();
						dataController.updateResult(dataInMongo.getId(),info);
					}

                } else {
					data = new Data(query);

					//men2assem lquery la parts la nechteghel aa kel part
					String[] queryParts = query.split(" ");
					VerbConjugator verbConjugator = new VerbConjugator();
					QueryAnalyzer queryAnalyzer = new QueryAnalyzer();

					List<List<Integer>> allResults = new ArrayList<>();

					//mnechteghel aa kel part lahal
					for (String part : queryParts) {
						List<String> synonyms = queryAnalyzer.getResult(part);

						//iza verb mn jib lconjugations tabaoo
                        List<String> conjugatedVerbs = verbConjugator.getResult(part);
						List<Integer> partResults = new ArrayList<>();

						//men chayek iza lkelme laseseye mawjude
						List<Integer> originalResults = analyzer.search(part);
						partResults.addAll(originalResults);

						//men fatech aal singulier iza ken pluriel w aal pluriel iza ken singulier
						List<Integer> singularOrPluralResults = analyzer.search(SingularPlural.singularPlural(part));
						partResults.addAll(singularOrPluralResults);

						//men fatech aa iza kel conjugation lal verb mawjude
						if(conjugatedVerbs!=null){
							for(String verb : conjugatedVerbs){
								List<Integer> searchResults = analyzer.search(verb);
								partResults.addAll(searchResults);
							}
						}

						//men fatech iza kel synonym jebne men datamuse mawjud
						for (String syn : synonyms) {
							List<Integer> searchResults = analyzer.search(syn);
							partResults.addAll(searchResults);
						}

						allResults.add(partResults);
					}

					// Find documents containing all query parts or their synonyms
					List<Integer> finalResults = new ArrayList<>(allResults.getFirst());
					for (List<Integer> partResults : allResults) {
						finalResults.retainAll(partResults);
					}

					// Output the final search results and add results to data instance
					for (Integer index : finalResults) {
						String documentContent = analyzer.getDocument(index);
						String documentName = analyzer.getDocumentName(documentContent);
						if (!printedFiles.contains(documentName)) {
							System.out.println("\n\nFound in document: " + documentName + "\n");
							List<String> phrases = Output.getPhrase(documentContent, query);
							if (!phrases.isEmpty()) {
								Output.printPhrases(phrases);
							} else {
								System.out.println(documentContent);
							}
							String modifiedDocumentName = documentName.replace('.', ',');
							if(!phrases.isEmpty()) {
								data.addDataResult(modifiedDocumentName, phrases);
							}
							printedFiles.add(documentName);
						}
					}

					//iza m le2a wala file khaso fi b oul eno m fi data ; laeno mmkn yle2e files b3ad fa m yaabe db bs b kun fi data
					if(printedFiles.isEmpty()){
						System.out.println("No data found in Database");
					}

					//iza le2a data w wadha b hota bel db
					if(!data.getResult().isEmpty()) {
						dataController.createData(data);
					}

				}
				do {
					System.out.println(">>Do you want to do another search?\n1.Yes\n0.No");
					option= scanInt.nextInt();
				}while(option!=0 && option!=1);

			}while (option!=0);
			} catch(IOException | ParseException e){
				System.err.println("An error occurred: " + e.getMessage());
				e.printStackTrace();
			} catch(Exception e){
				throw new RuntimeException(e);
			}

		System.out.println("Do you want to do any modifications to Database?\n1.Yes\n0.No");
		int dataBaseMod = scanInt.nextInt();
		if(dataBaseMod==1) {
			System.out.println("1.If you want to delete old data specify the number of months");
			System.out.println("2.If you want to empty Database");
			System.out.println("Any other key to exit");
			int deleteOption = scanInt.nextInt();
			if (deleteOption ==1) {
				System.out.println("Specicfy number of months");
				int nbOfMonths = scanInt.nextInt();
				dataController.deleteDataOlderThan(nbOfMonths);
				System.out.println("Data before " + nbOfMonths + " months deleted successfully!!");
			}
			if (deleteOption ==2) {
				dataController.deleteDataOlderThan(0);
				System.out.println("Database emptied successfully!!");
			}
		}

		context.close();


	}

}