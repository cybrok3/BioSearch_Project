package QueryAnswer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import info_retrieval_system.PairHolder;
import mitos.stemmer.Stemmer;

public class Search {
	
	private String COLLECTION_INDEX_PATH = "CollectionIndex/";
	private String VOCABULARY_FILE = COLLECTION_INDEX_PATH + "VocabularyFile.txt";
	private String POSTING_FILE = COLLECTION_INDEX_PATH + "PostingFile.txt";
	private String DOCUMENTS_FILE = COLLECTION_INDEX_PATH + "Documents.txt";
	private String DOCS_NORMS_FILE = COLLECTION_INDEX_PATH + "DocumentsNorms.txt";
	private String STOPWORDS_FOLDER = "stopwords_files";
	
	private Map<String, PairHolder> vocabulary; // global
	private Map<String, PairHolder> documentsNorms_Type_Path; // global
	private Set<String> stopwords = new HashSet<>(); // global
	
	//private int numOfWords = 0;
	private int numOfDocs = 0;
	
	public Search() {
		
		Stemmer.Initialize();
		this.vocabulary = new HashMap<>();
		this.documentsNorms_Type_Path = new HashMap<>();
		
		try (RandomAccessFile vocFile = new RandomAccessFile(VOCABULARY_FILE, "r");
			  RandomAccessFile docsNorms = new RandomAccessFile(DOCS_NORMS_FILE, "r");
				RandomAccessFile docsPaths = new RandomAccessFile(DOCUMENTS_FILE, "r")){
			
			// Load vocabulary into memory
			String line = vocFile.readLine();
			while(line != null) {
				String[] parts = line.split(" ");
				String word = parts[0];
				int df = Integer.parseInt(parts[1]);
				long pointer = Long.parseLong(parts[2]);
				this.vocabulary.put(word, new PairHolder(df,pointer));
				//numOfWords ++;
				line = vocFile.readLine();
			}
			
			System.out.println("Voc Load to memory completed\n");
			
			// Load docNorms into memory
			String docLine = docsNorms.readLine();
			String docPathLine = docsPaths.readLine();
			while(docLine != null) {
				String[] docParts = docLine.split(" ");
				String docID = docParts[0];
				float norm = Float.parseFloat(docParts[1]);
				String type = docParts[2];
				String[] docPathsParts = docPathLine.split("\\s+", 2);
				String path = docPathsParts[1];
				this.documentsNorms_Type_Path.put(docID, new PairHolder(norm, type, path));
				docLine = docsNorms.readLine();
				docPathLine = docsPaths.readLine();
			}
			
			System.out.println("Norms Load to memory completed\n");
			
			this.numOfDocs = countDocs();
			
			readStopWords(STOPWORDS_FOLDER);
			
			System.out.println("Stopwords read completed\n");
			
			vocFile.close();
			docsNorms.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public TreeMap<Float, PairHolder> answerQuery(String query, String type) {
		
		TreeMap<Float, PairHolder> docRates = new TreeMap<>(Collections.reverseOrder());
		
		System.out.println("Enter query to search for\n");
		
		try (/*Scanner scanner = new Scanner(System.in);*/
				RandomAccessFile postingFile = new RandomAccessFile(POSTING_FILE, "r")) {
			
			Map<String, HashMap<String, Float>> documentVectors = new HashMap<>();
			Set<String> queryDocNames = new HashSet<>(); 
			Set<String> queryTerms = new HashSet<>(); 
			
 			PairHolder df_Pointer; // it is a df - pointer pair
			Long value;
			
			System.out.println("Enter the type filter: diagnosis/test/treatment\n");
				
			String[] queryWords = query.split("[\\s\\p{Punct}]+");
			
			for(String word : queryWords) {
				
				word = word.toLowerCase();
				
				word = Normalizer.normalize(word,Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); // Remove any accent
				
				word = Stemmer.Stem(word); // Stemm the word
				
				if(!isStopword(word)) {
					
					df_Pointer = vocabulary.get(word);
					
					value = df_Pointer != null ? df_Pointer.getPointer() : null;
					
					if(value != null) {
						
						queryTerms.add(word);
						
						//System.out.print(word + ' ' + df_Pointer.getDF()+ ": \n");
						
						postingFile.seek(value);
						String postingLine = postingFile.readLine();
						
						while(postingLine != null) {
							
							if ("end".equals(postingLine.trim())) {
				                break;
				            }
							
							String[] postingParts = postingLine.split(" ");
							String docID = postingParts[0];
							//String docPointer = postingParts[2];

							//System.out.println("    " + docID + ' ' + docPointer + '\n');
							if(documentsNorms_Type_Path.get(docID).getType().equals(type)) {
								queryDocNames.add(docID);
							}
							
							postingLine = postingFile.readLine();
						}	
					}
				}
			}
			
			/*System.out.println("DocNames:");
			
			for(String docName : queryDocNames) {
				System.out.println(docName);
			}*/
			
			createDocVectors(documentVectors, queryDocNames, queryTerms);
			
			if(!documentVectors.isEmpty()) {
				System.out.println("Created doc vectors\n");
			}
			
			//Create Query Vector
			Map<String, Float> queryVector = new HashMap<>();
		    
		    // Count the term frequency (TF) for each term in the query
		    Map<String, Integer> termFrequency = new HashMap<>();
		    for (String term : queryTerms) {
		        termFrequency.put(term, termFrequency.getOrDefault(term, 0) + 1);
		    }
		    
		    // Calculate the TF-IDF for each term and add it to the query vector
		    int totalTerms = queryTerms.size();
		    for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
		        String term = entry.getKey();
		        int frequency = entry.getValue();
		        float tf = (float) frequency / totalTerms; // TF = frequency / total terms
		        float weight = calculateWeight(vocabulary.get(term).getDF(), tf); // Calculate IDF for the term
		        queryVector.put(term, weight);
		    }
		    
		    float queryMagnitude = calculateQueryMagnitude(queryVector);
		    
		    for (Map.Entry<String, HashMap<String, Float>> entry : documentVectors.entrySet()) {
				 
				 String docID = entry.getKey();
				 HashMap<String, Float> docVector = entry.getValue();
				 
				 float rate = calculateCosineRate(queryVector, docVector, queryMagnitude, documentsNorms_Type_Path.get(docID).getTF());
				 
				 docRates.put(rate, new PairHolder(docID, documentsNorms_Type_Path.get(docID).getPath()));
				 
			 }
		    
		    /*if(!docRates.isEmpty()) {
		    	System.out.println("Cosine similarity rates (sorted):");
			    for (Map.Entry<Float, PairHolder> entry : docRates.entrySet()) {
			        System.out.println("Document " + entry.getValue() + ": " + entry.getKey());
			    }
		    }else {
		    	System.out.println("No matches found for the given query");
		    }*/
		    
			//userInput = scanner.nextLine();
			//scanner.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return docRates;
	}
	
	private float calculateQueryMagnitude(Map<String, Float> queryVec) {
		
		float magnitude = 0.0f;
		
		for(Map.Entry<String,Float> entry : queryVec.entrySet()) {
			magnitude += (entry.getValue()*entry.getValue());
		}
		
		magnitude = (float)Math.sqrt(magnitude);
		
		return magnitude;
	}
	
	private float calculateCosineRate(Map<String, Float> queryVec, HashMap<String, Float> docVec, float queryMagnitude, float docMagnitude) {
		
		 float dotProduct = calculateDotProduct(queryVec, docVec);
		
		 float cosRate = dotProduct/(queryMagnitude * docMagnitude);
		 
		 return cosRate;
	}
	
	private float calculateDotProduct(Map<String, Float> vec1, Map<String, Float> vec2) {
		
	    float dotProduct = 0.0f;
	    
	    for (Map.Entry<String, Float> entry : vec1.entrySet()) {
	        String term = entry.getKey();
	        float weight1 = entry.getValue();
	        float weight2 = vec2.getOrDefault(term, 0.0f);
	        dotProduct += weight1 * weight2;
	    }
	    
	    return dotProduct;
	}
	
	private void createDocVectors(Map<String, HashMap<String, Float>> documentVectors, Set<String> queryDocNames, Set<String> queryTerms) {
		
		for(String docID : queryDocNames) {
			documentVectors.put(docID, new HashMap<>());
		}
		
		for(String term : queryTerms) {
			PairHolder dfPointer = vocabulary.get(term);
	        
	        if (dfPointer != null) {
	            long pointer = dfPointer.getPointer();
	            try (RandomAccessFile postingFile = new RandomAccessFile(POSTING_FILE, "r")) {
	                postingFile.seek(pointer);
	                String postingLine = postingFile.readLine();
	                
	                while (postingLine != null) {
	                    if ("end".equals(postingLine.trim())) {
	                        break;
	                    }
	                    
	                    String[] postingParts = postingLine.split(" ");
	                    String docID = postingParts[0];
	                    float tf = Float.parseFloat(postingParts[1]);
	                    if(queryDocNames.contains(docID)) {
	                        float weight = calculateWeight(dfPointer.getDF(), tf);
	                        documentVectors.get(docID).put(term, weight);
	                    }
	                    
	                    postingLine = postingFile.readLine();
	                }
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	}
	
	private float calculateWeight(int df, float tf) {
		float idf = (float) ((float) Math.log((float) this.numOfDocs/ df)/Math.log(2));
		return tf * idf;
	}
	
	public double dotProduct(Map<String, Double> vector1, Map<String, Double> vector2) {
	    double dotProduct = 0.0;
	    for (String dimension : vector1.keySet()) { // All the dimensions are all the vocWords basically
	        if (vector2.containsKey(dimension)) {
	            // Multiply the corresponding values and add to the dot product
	            dotProduct += vector1.get(dimension) * vector2.get(dimension);
	        }
	        // If dimension doesn't exist in vector2, its value is treated as zero
	    }
	    return dotProduct;
	}

	
	private void readStopWords(String folderPath) {
    	
    	File folder = new File(folderPath);
        
        // Get the list of file names
        String[] filenames = folder.list();
         
         for (String filename : filenames) {
             try (BufferedReader br = new BufferedReader(new FileReader(folderPath + "/" + filename))) {
                 String line;
                 while ((line = br.readLine()) != null) {
                     stopwords.add(line.trim().toLowerCase());
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
    }
	
	public int countDocs() {
        int count = 0;
        try (RandomAccessFile docsFile = new RandomAccessFile(DOCUMENTS_FILE, "r")) {
            String line = docsFile.readLine();
            while (line != null) {
                count++;
                line = docsFile.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }
	
	private boolean isStopword(String word) {
    	return stopwords.contains(word);
    }
	
	public void updatePaths(String path) {
		
		this.COLLECTION_INDEX_PATH = path;
		this.VOCABULARY_FILE = COLLECTION_INDEX_PATH + "VocabularyFile.txt";
		this.POSTING_FILE = COLLECTION_INDEX_PATH + "PostingFile.txt";
		this.DOCUMENTS_FILE = COLLECTION_INDEX_PATH + "Documents.txt";
		this.DOCS_NORMS_FILE = COLLECTION_INDEX_PATH + "DocumentsNorms.txt";
		
	}
	
	/*public static void main(String[] args) {
		
		Search testSearch = new Search();
        Scanner scanner = new Scanner(System.in);
        String userOption;

        do {
            testSearch.answerQuery(scanner);
            
            System.out.println("\nDo you want to search again? (yes/no)");
            userOption = scanner.nextLine().toLowerCase();
        } while (!userOption.equals("no") && !userOption.equals("quit"));

        System.out.println("Goodbye!");
        scanner.close();
	        
    }*/
	
}
