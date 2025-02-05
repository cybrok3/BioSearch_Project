package info_retrieval_system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import gr.uoc.csd.hy463.NXMLFileReader;
import mitos.stemmer.Stemmer;

public class PartialIndexing {
	
	private String COLLECTION_PATH; //= "C:\\Users\\tasos\\Documents\\CSD workspace\\current_stuff\\cs463_information_retrieval_systems\\Project\\initial testing\\MiniCollection";
    private String STOPWORDS_FOLDER; //"stopwords_files"; // Folder that has the stopwords txt
    //private static final String[] STOPWORDS_FILENAMES = {"stopwordsEn.txt", "stopwordsGr.txt"}; // The actual txt files for the stopwords
    private String COLLECTION_INDEX_PATH; //"CollectionIndex/";
    private String VOCABULARY_FILE; // COLLECTION_INDEX_PATH + "VocabularyFile.txt";
    private String DOCUMENTS_FILE; //COLLECTION_INDEX_Path + "documents.txt";
    private String POSTING_FILE; //COLLECTION_INDEX_PATH + "PostingFile.txt";
    private String COLLECTION_INDEX_FOLDERNAME;
    //private static final String[] XML_TAGS = {"pmcid", "title", "abstr", "body", "journal", "publisher"}; // The tags we are interested about
    
    private TreeSet<Word> vocabulary = new TreeSet<>(); // Vocabulary storing word - df
    //private TreeSet<Document> docsFile = new TreeSet<>(); // DocumentsFile storing pmcid - path
    private Set<Document> docsFile = new LinkedHashSet<>();
    private Set<String> stopwords = new HashSet<>(); // Set of stopwords
    private Queue<String> partialVocFilenames = new LinkedList<>();
    private Queue<String> partialPostingFilenames = new LinkedList<>();
    
    
    private int MEMORY_THRESHOLD = 10200; // 50 million records
    private int currentRecords = 0;
    private int numOfFiles = 0;
    private int docsProcessed = 0;
    private int mergesDone = 0;
    private int numberOfFilesCreated = 1;
    private int uniqueWords;
    
    private ProgressObserver observer;
    
    /**
     * Constructor which initializes the Set of stopwords we want
     */
    public PartialIndexing(ProgressObserver observer, String selectedCollectionFolder, String selectedIndexFolder, String selectedStopwordsFolder, String newFolderName, int threshold) {
    	this.observer = observer;
    	this.COLLECTION_PATH = selectedCollectionFolder;
    	this.COLLECTION_INDEX_FOLDERNAME = "\\" + newFolderName + "/";
    	this.COLLECTION_INDEX_PATH = selectedIndexFolder + COLLECTION_INDEX_FOLDERNAME ; // "\\CollectionIndex/";
    	this.STOPWORDS_FOLDER = selectedStopwordsFolder;
    	Stemmer.Initialize();
    	
    	// Default threshold value 10200 remains
    	if(threshold > 10200) {
    		this.MEMORY_THRESHOLD = threshold;
    	}
    	
    	readStopWords(STOPWORDS_FOLDER);
    	listFilesForFolder(new File(COLLECTION_PATH));
    	
    	System.out.println("Total Number Of Files: " + this.numOfFiles);
    	this.observer.initProgressbar(numOfFiles);
    	
    	try {
            Files.createDirectories(Paths.get(COLLECTION_INDEX_PATH)); // Create CollectionIndex dir
    		this.VOCABULARY_FILE = COLLECTION_INDEX_PATH + "VocabularyFile.txt";
    		this.POSTING_FILE = COLLECTION_INDEX_PATH + "PostingFile.txt";
    		this.DOCUMENTS_FILE = COLLECTION_INDEX_PATH + "Documents.txt";
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method reads the stopwords and creates a Set of them 
     * for efficient check of the words later on
     * @param folderPath
     */
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
    
    /**
     * This method starts the main indexing of the data collection
     */
    public void indexCollection() throws UnsupportedEncodingException, IOException {
        Thread indexingThread = new Thread(() -> {
            try {
                // Initiate the writing of the voc and doc txt files
                RandomAccessFile finalDocsFile = new RandomAccessFile(DOCUMENTS_FILE, "rw");
                
                Instant start = Instant.now();
                
                Files.walk(Paths.get(COLLECTION_PATH))
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        try {
                        	
                        	this.observer.updateDataWriteProgressBar(docsProcessed);
                        	
                        	Path parentFolder = filePath.getParent().getParent().getParent();
                        	String documentType = parentFolder.getFileName().toString();
                        	
                            processDocument(filePath, finalDocsFile, documentType);
                            
                            docsProcessed ++;
                            
                            if (currentRecords >= MEMORY_THRESHOLD) {
                                writeDataToNewFiles(numberOfFilesCreated);
                                numberOfFilesCreated++;
                                currentRecords = 0;
                            }
                            
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                
                finalDocsFile.close();
                
                // After processing all documents write the remaining information also on disk
                writeDataToNewFiles(numberOfFilesCreated);
                
                System.out.println("Data Written");
                
                Instant end = Instant.now();
                long timeElapsed = Duration.between(start, end).toMillis();
                this.observer.printDataWriteStatistics(timeElapsed, docsProcessed);
                
                Instant mergeStart = Instant.now();
                
                this.observer.initProgressbar(partialVocFilenames.size() - 1);
                this.observer.updateMergingProgressBar(mergesDone);

                int i = 1;
                while(partialVocFilenames.size() > 1) { // As long as we have 2 partial indexes
                	mergePartialIndexes(i);
                    i++;
                    mergesDone ++;
                    this.observer.updateMergingProgressBar(mergesDone);
                }
                
                String vocFile = partialVocFilenames.poll();
                String postFile = partialPostingFilenames.poll();
                
                renameFile(vocFile, VOCABULARY_FILE);
                renameFile(postFile, POSTING_FILE);
                
                System.out.println("Creation of IndexFile successful!");
                
                Instant MergeEnd = Instant.now();
                long mergeTimeElapsed = Duration.between(mergeStart, MergeEnd).toMillis();
                this.observer.printMergingStatistics(mergeTimeElapsed, mergesDone);
                
                Instant normStart = Instant.now();
                
                calculateNorms(this.observer);
                
                System.out.println("Norms calculation done\n");
                
                RandomAccessFile docsFileNorms = new RandomAccessFile(COLLECTION_INDEX_PATH + "DocumentsNorms.txt", "rw");
                for(Document currentDoc : docsFile) {
                	docsFileNorms.writeBytes(String.format("%s %f %s\n", currentDoc.getDocID(), Math.sqrt(currentDoc.getNorm()), currentDoc.getType()));
                }
                docsFileNorms.close();
                
                Instant normEnd = Instant.now();
                long normTimeElapsed = Duration.between(normStart, normEnd).toMillis();
                this.observer.printNormCalculationStatistics(normTimeElapsed);
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        indexingThread.start();
    }
    
    /**
     * 
     * @param filePath
     * @param vocabularyWriter
     * @param documentsWriter
     */
    private void processDocument(Path filePath, RandomAccessFile finalDocsFile, String docType) throws UnsupportedEncodingException, IOException{
    	
    	//documentId++;
    	
    	File doc = new File(filePath.toString());
		NXMLFileReader xmlFile = new NXMLFileReader(doc);
		
		String docID = doc.getName();
		
		String pmcid = xmlFile.getPMCID();
		String title = xmlFile.getTitle();
		String abstr = xmlFile.getAbstr();
		String body = xmlFile.getBody();
		String journal = xmlFile.getJournal();
		String publisher = xmlFile.getPublisher();
		//ArrayList<String> authors = xmlFile.getAuthors();
		//HashSet<String> categories = xmlFile.getCategories();
		
		Map<String, Integer> docWords = new HashMap<>();
		
		processText(pmcid, docID, "pmcid", docWords);
		processText(title, docID, "title", docWords);
		processText(abstr, docID, "abstr", docWords);
		processText(body, docID, "body", docWords);
		processText(journal, docID, "journal", docWords);
		processText(publisher, docID, "publisher", docWords);
		
		float maxFreq = findMaxFreq(docWords);
		
		docsFile.add(new Document(docID, filePath, docType)); // Add the current doc to the set sorted by the filenames
		currentRecords ++;
		
		long pointer = finalDocsFile.getFilePointer();
		finalDocsFile.writeBytes(String.format("%s %s\n", docID, filePath.toString()));
		
		
		for(Map.Entry<String, Integer> entry : docWords.entrySet()) {
			
			Word currentWord = new Word(entry.getKey());
			
			// If it wasn't in the voc, add it and update info
			if(!vocabulary.contains(currentWord)) { // It checks based on the 'String word' field of the Word class
				currentWord.addWordToDocInfo(docID, entry.getValue()/maxFreq, pointer);
				currentWord.updateDF();
				vocabulary.add(currentWord);
			}else {
				Word existingWord = vocabulary.floor(currentWord);
			    existingWord.addWordToDocInfo(docID, entry.getValue()/maxFreq, pointer);
			    existingWord.updateDF();
			}
			currentRecords ++;
		}
    }
    
    public void writeDataToNewFiles(int i) {
        try {
        	
            RandomAccessFile newVocFile = new RandomAccessFile(COLLECTION_INDEX_PATH + "Vocabulary" + i + ".txt", "rw");
            RandomAccessFile newPostingFile = new RandomAccessFile(COLLECTION_INDEX_PATH + "PostingFile" + i + ".txt", "rw");
            
            partialVocFilenames.offer(COLLECTION_INDEX_PATH + "Vocabulary" + i + ".txt");
            partialPostingFilenames.offer(COLLECTION_INDEX_PATH + "PostingFile" + i + ".txt");

            long currentPostingFilePointer = 0;

            for (Word vocEntry : vocabulary) {
                String word = vocEntry.getWord();
                int df = vocEntry.getDF();

                // Write word and its DF to vocabulary file
                newVocFile.writeBytes(String.format("%s %d %d\n", word, df, currentPostingFilePointer));

                for (Map.Entry<String, PairHolder> docsID_TF_pointer_entry : vocEntry.docID_tf_pointer().entrySet()) {
                    String docID = docsID_TF_pointer_entry.getKey();
                    newPostingFile.writeBytes(String.format("%s %f %d\n", docID, docsID_TF_pointer_entry.getValue().getTF(), docsID_TF_pointer_entry.getValue().getPointer()));
                }
                
                // Write String "end" to indicate the end of the postings for the term
                newPostingFile.writeBytes(String.format("%s\n", "end"));
                currentPostingFilePointer = newPostingFile.getFilePointer();
            }

            newVocFile.close();
            newPostingFile.close();
            
            uniqueWords += vocabulary.size();

            vocabulary.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @param text
     * @param docName
     * @param tag
     * @param docWords
     */
    private void processText(String text, String docName, String tag, Map<String, Integer> docWords) {
		
	    if (text == null || text.isEmpty()) {
	        return;
	    }

	    try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
	    	
	        String line;
	        
	        while ((line = reader.readLine()) != null) {
	            Scanner scanner = new Scanner(line);
	            scanner.useDelimiter("[\\s\\p{Punct}]+"); // Exclude punctuation

	            while (scanner.hasNext()) {
	            	
	            	String word = scanner.next().toLowerCase(); // To lower case
	            	
	            	String noAccentWord = Normalizer.normalize(word,Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); // Remove any accent
	            	
	            	noAccentWord = Stemmer.Stem(noAccentWord); // Stemm the word
	            	
                    if (!isStopword(noAccentWord)) {
                    	if (docWords.containsKey(noAccentWord)) {
                            docWords.put(noAccentWord, docWords.get(noAccentWord) + 1);
                        } else {
                            docWords.put(noAccentWord, 1);
                        }
                    }
	            }
	            scanner.close();
	        }
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
    
    /**
     * Find max frequency of term in tg
     * @param docWords
     * @return
     */
    private float findMaxFreq(Map<String, Integer> docWords) {
    	
    	int maxFreq = 0;
    	
    	for(Map.Entry<String, Integer> entry : docWords.entrySet()) {
    		if(entry.getValue() > maxFreq) {
    			maxFreq = entry.getValue();
    		}
    	}
    	
    	return maxFreq;
    }
    
    private void mergePartialIndexes(int i) {
        
        String finalVocFilename = COLLECTION_INDEX_PATH + "MergedVocFile" + i + ".txt";
        String finalPostingFilename = COLLECTION_INDEX_PATH + "MergedPostingFile" + i + ".txt";
        
        try {
            RandomAccessFile finalVocFile = new RandomAccessFile(finalVocFilename, "rw");
            RandomAccessFile finalPostingFile = new RandomAccessFile(finalPostingFilename, "rw");
            
            String partialVocFilename1 = partialVocFilenames.poll();
            String partialVocFilename2 = partialVocFilenames.poll();
            
            String partialPostingFilename1 = partialPostingFilenames.poll();
            String partialPostingFilename2 = partialPostingFilenames.poll();
            
            RandomAccessFile partialVocFile1 = new RandomAccessFile(partialVocFilename1, "r");
            RandomAccessFile partialVocFile2 = new RandomAccessFile(partialVocFilename2, "r");
            
            RandomAccessFile partialPostingFile1 = new RandomAccessFile(partialPostingFilename1, "r");
            RandomAccessFile partialPostingFile2 = new RandomAccessFile(partialPostingFilename2, "r");
            
            String line1 = partialVocFile1.readLine();
            String line2 = partialVocFile2.readLine();

            long currentPostingFilePointer = 0;
            
            while (line1 != null && line2 != null) {
                
                String[] parts1 = line1.split(" ");
                String[] parts2 = line2.split(" ");

                String word1 = parts1[0];
                String word2 = parts2[0];

                int df1 = Integer.parseInt(parts1[1]);
                int df2 = Integer.parseInt(parts2[1]);

                long pointer1 = Long.parseLong(parts1[2]);
                long pointer2 = Long.parseLong(parts2[2]);

                if (word1.equals(word2)) {
                    int mergedDF = df1 + df2;

                    // Write word and mergedDF to the final vocabulary file
                    finalVocFile.writeBytes(String.format("%s %d %d\n", word1, mergedDF, currentPostingFilePointer));
                    
                    // Merge posting data from the partial posting files to the final posting file
                    mergePostingData(partialPostingFile1, pointer1, partialPostingFile2, pointer2, finalPostingFile);

                    // Update the pointer for the next word in the final vocabulary file
                    // This will point to the end of the final posting file where we just wrote the data
                    currentPostingFilePointer = finalPostingFile.getFilePointer();

                    line1 = partialVocFile1.readLine();
                    line2 = partialVocFile2.readLine();
                    
                } else if (word1.compareTo(word2) < 0) {
                    // Write word1 and its pointer to the final vocabulary file
                    finalVocFile.writeBytes(String.format("%s %d %d\n", word1, df1, currentPostingFilePointer));
                    copyPostingData(partialPostingFile1, pointer1, finalPostingFile);
                    currentPostingFilePointer = finalPostingFile.getFilePointer();
                    line1 = partialVocFile1.readLine();
                    
                } else {
                    // Write word2 and its pointer to the final vocabulary file
                    finalVocFile.writeBytes(String.format("%s %d %d\n", word2, df2, currentPostingFilePointer));
                    copyPostingData(partialPostingFile2, pointer2, finalPostingFile);
                    currentPostingFilePointer = finalPostingFile.getFilePointer();
                    line2 = partialVocFile2.readLine();
                }
            }
            
            finalVocFile.close();
            finalPostingFile.close();
            partialVocFile1.close();
            partialVocFile2.close();
            partialPostingFile1.close();
            partialPostingFile2.close();
            
            partialVocFilenames.offer(finalVocFilename);
            partialPostingFilenames.offer(finalPostingFilename);
            
            Files.deleteIfExists(Paths.get(partialVocFilename1));
            Files.deleteIfExists(Paths.get(partialVocFilename2));
            Files.deleteIfExists(Paths.get(partialPostingFilename1));
            Files.deleteIfExists(Paths.get(partialPostingFilename2));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    private void mergePostingData(RandomAccessFile file1, long pointer1, RandomAccessFile file2, long pointer2, RandomAccessFile finalFile) throws IOException {
        
        file1.seek(pointer1);
        file2.seek(pointer2);

        String line1 = file1.readLine();
        String line2 = file2.readLine();

        while (line1 != null && line2 != null) {
        	
        	if ("end".equals(line1.trim()) || "end".equals(line2.trim())) {
                break;
            }

            String[] parts1 = line1.split(" ");
            String[] parts2 = line2.split(" ");

            String docName1 = parts1[0];
            String docName2 = parts2[0];
            
            float freq1 = Float.parseFloat(parts1[1].trim());
            float freq2 = Float.parseFloat(parts2[1].trim());
            
            long docPointer1 = Long.parseLong(parts1[2].trim());
            long docPointer2 = Long.parseLong(parts2[2].trim());

            if (docName1.compareTo(docName2) < 0) {
                finalFile.writeBytes(String.format("%s %f %d\n", docName1, freq1, docPointer1));
                line1 = file1.readLine();
            } else {
                finalFile.writeBytes(String.format("%s %f %d\n", docName2, freq2, docPointer2));
                line2 = file2.readLine();
            }
        }

        // Copy remaining lines from file1, if any
        while (line1 != null) {
        	
        	if ("end".equals(line1.trim())) {
                break;
            }
        	
            String[] parts1 = line1.split(" ");
            String docName1 = parts1[0];
            long docPointer1 = Long.parseLong(parts1[2].trim());
            float freq1 = Float.parseFloat(parts1[1].trim());
            finalFile.writeBytes(String.format("%s %f %d\n", docName1, freq1, docPointer1));
            line1 = file1.readLine();
        }

        // Copy remaining lines from file2, if any
        while (line2 != null) {
        	
        	if ("end".equals(line2.trim())) {
                break;
            }
        	
            String[] parts2 = line2.split(" ");
            String docName2 = parts2[0];
            float freq2 = Float.parseFloat(parts2[1].trim());
            long docPointer2 = Long.parseLong(parts2[2].trim());
            finalFile.writeBytes(String.format("%s %f %d\n", docName2, freq2, docPointer2));
            line2 = file2.readLine();
        }
        
        finalFile.writeBytes(String.format("%s\n", "end"));
    }

    private void copyPostingData(RandomAccessFile partialFile, long pointer1, RandomAccessFile finalFile) throws IOException {
        
        partialFile.seek(pointer1);
        String line = partialFile.readLine();
        
        // Copy remaining lines from partial file, excluding the end of postings indicator
        while (line != null) {
        	
        	if ("end".equals(line.trim())) {
                break;
            }
        	
            String[] parts = line.split(" ");
            String docName = parts[0];
            float freq = Float.parseFloat(parts[1].trim());
            long docPointer = Long.parseLong(parts[2].trim());
            finalFile.writeBytes(String.format("%s %f %d\n", docName, freq, docPointer));
            line = partialFile.readLine();
        }
        
        finalFile.writeBytes(String.format("%s\n", "end"));
    }
    
    private void calculateNorms(ProgressObserver observer) {
        try (RandomAccessFile finalVocFile = new RandomAccessFile(VOCABULARY_FILE, "r");
             RandomAccessFile finalPostingFile = new RandomAccessFile(POSTING_FILE, "r");
             RandomAccessFile finalDocFile = new RandomAccessFile(DOCUMENTS_FILE, "rw")) {
        	
        	int k = 0;
        	observer.initProgressbar(uniqueWords);

            String line;
            while ((line = finalVocFile.readLine()) != null) {
                String[] parts = line.split(" ");
                int df = Integer.parseInt(parts[1]);
                long pointer = Long.parseLong(parts[2]);

                float idf = calculateIDF(df);

                // Go to the correct position in the posting file
                finalPostingFile.seek(pointer);

                String postingLine;
                while ((postingLine = finalPostingFile.readLine()) != null) {
                	
                    if (postingLine.equals("end")) {
                        break;
                    }

                    String[] postingParts = postingLine.split(" ");
                    String docID = postingParts[0];
                    float tf = Float.parseFloat(postingParts[1]);

                    float weight = (float) tf * idf;
                    
                    for (Document doc : docsFile) {
                        if (doc.getDocID().equals(docID)) {
                            doc.updateNorm(weight);
                            k++;
                            observer.updateNormsCalculationProgressBar(k);
                            break;
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private float calculateIDF(int df) {
        // Calculate IDF using the formula: IDF = log(Total number of documents / df)
        return (float) ((float) Math.log((float) getNumOfFiles() / df)/Math.log(2));
    }
    
    /**
     * Check if 'word' is a stopword
     * @param word
     * @return true if it is, false if not
     */
    private boolean isStopword(String word) {
    	return stopwords.contains(word);
    }
    
    private void renameFile(String oldFilePath, String newFilePath){
        Path oldPath = Paths.get(oldFilePath);
        Path newPath = Paths.get(newFilePath);
        try {
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Failed to rename the file: " + e.getMessage());
        }
    }
    
    private void listFilesForFolder(File folder) {
    	for (File fileEntry : folder.listFiles()) {
    		if (fileEntry.isDirectory()) {
    			listFilesForFolder(fileEntry);
    		} else {
    			this.numOfFiles++;
    		}
    	}
    }
    
    public int getDocsProcessed() {
    	return this.docsProcessed;
    }
    
    public int getNumOfFiles() {
    	return this.numOfFiles;
    }
    
}
