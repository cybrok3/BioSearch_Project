package Evaluation;
import gr.uoc.csd.hy463.Topic;
import gr.uoc.csd.hy463.TopicsReader;
import info_retrieval_system.PairHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.io.RandomAccessFile;
import java.io.IOException;
import QueryAnswer.Search;

public class IRQualityEvaluator {
	
	private String TOPICS_FOLDER_PATH = "TopicsFile/";
	private String TOPICS_FILE_PATH = TOPICS_FOLDER_PATH + "topics.xml";
	private String RESULTS_FILE_PATH = TOPICS_FOLDER_PATH + "results.txt";
	private String QRELS_FILE_PATH = TOPICS_FOLDER_PATH + "qrels.txt";
	private String EVAL_RESULTS_FILE_PATH = TOPICS_FOLDER_PATH + "eval_results.txt";
	
	private ArrayList<Topic> topics;
	private HashMap<Integer, QRel_Topic> qRelTopics;
	
	private int numOfTopics;
	
	private Search searchEngine;
	
	public IRQualityEvaluator() throws Exception {
		
		searchEngine = new Search();
		
		readTopics();
		
		getResults();
		System.out.println("results.txt created.\n");
		
		read_QRels();
		System.out.println("qrels.txt read completed.\n");
		
		/*for(Map.Entry<Integer, QRel_Topic> entry : qRelTopics.entrySet()) {
			System.out.println("Topic Number: " + entry.getKey());
			System.out.println("Topic known Relevant docs: " + entry.getValue().getRelDocs());
			HashMap<String, Integer> id_rel_Map = entry.getValue().id_rel_Map();
			
			for(Map.Entry<String, Integer> info : id_rel_Map.entrySet()) {
				System.out.println("\tdocID: " + info.getKey() + " relevance: " + info.getValue());
			}
		}*/
		
	}
	
	public void Evaluate() {
		
		try {
			RandomAccessFile resultsFile = new RandomAccessFile(RESULTS_FILE_PATH, "r");
			RandomAccessFile eval_resultsFile = new RandomAccessFile(EVAL_RESULTS_FILE_PATH, "rw");
			
			for(int i = 1; i <= numOfTopics; i++) {
				
				float numRelevant = qRelTopics.get(i).getRelDocs(); // total Number of Relevant docs for current Topic (R)
				
				//Bpref variables
				float sum = 0;
				int n = 0; // The number of known irrelevant docs at each iteration
				
				//Average Precision variables
				int numRelevantRetrieved = 0; // number of relevant documents retrieved so far
				float sumPrecision = 0; // the sum of precision values
				
				
				//Calculate the Sum of the BPref formula
				String line = resultsFile.readLine();
				
				while((line != null)) {
					
					if ("end".equals(line.trim())) {
		                break;
		            }
					
					String[] parts = line.split("\t");
					
					String docID = parts[2];
					
					int rank = Integer.parseInt(parts[3]);
					
					if(qRelTopics.get(i).id_rel_Map().get(docID) != null) { //The current Doc is judged
						
						int relevance = qRelTopics.get(i).id_rel_Map().get(docID);
						
						if(relevance >= 1) { //doc is relevant
							
							//Bpref
							sum += (float)1 - ((float)n/(float)numRelevant);
							
							//AveP'
							numRelevantRetrieved ++;
							sumPrecision += (float)numRelevantRetrieved/(float)rank;
							
						}else { // doc not relevant
							n++;
						}
					}
					
					line = resultsFile.readLine();
				}
				
				//BPref Calculation
				float bPref = ((float)1/(float)numRelevant)*(float)sum;
				
				//Average Precision Calculation
				float aveP = (float)sumPrecision/(float)numRelevant;
				
				eval_resultsFile.writeBytes(String.format("%d\t%f\t%f\n", i, bPref, aveP));
				
			}

            resultsFile.close();
            eval_resultsFile.close();
			
		}catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	public void read_QRels() {
		
		qRelTopics = new HashMap<>();
		
		numOfTopics = 0;
		
		try {
			RandomAccessFile qrelsFile = new RandomAccessFile(QRELS_FILE_PATH, "r");
			
			String line;
			while((line = qrelsFile.readLine()) != null){
				
				String[] parts = line.split("\t");
				
				int topicNum = Integer.parseInt(parts[0]);
				String docID = parts[2] + ".nxml";
				int relevance = Integer.parseInt(parts[3]);
				
				QRel_Topic currentTopic = qRelTopics.get(topicNum);
				
				if(currentTopic == null) {
					currentTopic = new QRel_Topic(topicNum);
					qRelTopics.put(topicNum, currentTopic);
					numOfTopics++;
				}
				currentTopic.add_ID_Rel_Pair(docID, relevance);
				
				if(relevance >= 1) {
					currentTopic.updateRelDocs();
				}
				
			}
			
			qrelsFile.close();
			
		}catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void readTopics() throws Exception { 
		topics = TopicsReader.readTopics(TOPICS_FILE_PATH);
	}
	
	public void getResults() {
		try {
			
			RandomAccessFile resultsFile = new RandomAccessFile(RESULTS_FILE_PATH, "rw");
			
			for(Topic topic : topics) {
				
				int topicNumber = topic.getNumber();
				TreeMap<Float, PairHolder> currentResults = searchEngine.answerQuery(topic.getSummary(), topic.getType().toString().toLowerCase());
				
				if(!currentResults.isEmpty()) {
					int i = 1;
				    for (Map.Entry<Float, PairHolder> entry : currentResults.entrySet()) {
				        resultsFile.writeBytes(String.format("%d\t%d\t%s\t%d\t%f\n", topicNumber, 0, entry.getValue().getName(), i, entry.getKey()));
				        // TOPIC_NO Q0 PMCID RANK SCORE
				        i++;
				    }
				    resultsFile.writeBytes(String.format("%s\n", "end"));
			    }
				
			}
			
			resultsFile.close();
			
		}catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	public static void main(String[] args) throws Exception {
		
		IRQualityEvaluator evaluator = new IRQualityEvaluator();
		evaluator.Evaluate();
        
    }

}