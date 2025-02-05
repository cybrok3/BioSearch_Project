package Evaluation;

import java.util.HashMap;

public class QRel_Topic implements Comparable<QRel_Topic>{
	
	private int topicNum;
	private int relDocs;
	private HashMap<String, Integer> id_rel;
	
	public QRel_Topic(int topicNum) {
		this.topicNum = topicNum;
		this.relDocs = 0;
		this.id_rel = new HashMap<>();
	}
	
	public void add_ID_Rel_Pair(String docID, int rel) {
		this.id_rel.put(docID, rel);
	}
	
	public HashMap<String, Integer> id_rel_Map(){
		return this.id_rel;
	}
	
	public int getTopicNum() {
		return this.topicNum;
	}
	
	public int getRelDocs() {
		return this.relDocs;
	}
	
	public void updateRelDocs() {
		this.relDocs++;
	}
	
	public int compareTo(QRel_Topic other) {
        return Integer.compare(this.topicNum, other.topicNum);
    }

}
