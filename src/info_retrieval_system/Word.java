package info_retrieval_system;

import java.util.Map;
import java.util.TreeMap;

public class Word implements Comparable<Word>{
	
	private String word;
	private int df;
	private Map<String, PairHolder> docID_tf_pointer;

	/**
	 * Constructor
	 * @param word
	 */
	public Word(String word) {
		
		this.word = word;
		this.df = 0;
		this.docID_tf_pointer = new TreeMap<>();
		
	}
	
	public String getWord() {
		return this.word;
	}
	
	public int getDF() {
		return this.df;
	}
	
	public Map<String, PairHolder> docID_tf_pointer(){
		return this.docID_tf_pointer;
	}
	
	public void addWordToDocInfo(String docID, float termFreq, long pointer) {
		
		//this.docID_TF.put(docID, docID_TF.getOrDefault(docID, 0) + 1);
		this.docID_tf_pointer.put(docID, new PairHolder(termFreq, pointer));
	}
	
	public void updateDF() {
		this.df = this.docID_tf_pointer.size();
	}

	@Override
	public int compareTo(Word otherWord) {
		return this.word.compareTo(otherWord.getWord());
	}
}
