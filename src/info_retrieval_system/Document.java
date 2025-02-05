package info_retrieval_system;

import java.nio.file.Path;

public class Document implements Comparable<Document>{
	
	private String docID;
	private String type;
	private Path docPath;
	private float norm;
	
	public Document(String docID, Path docPath, String type) {
		
		this.docID = docID;
		this.type = type;
		this.docPath = docPath;
		this.norm = 0;
	}
	
	public Document(String docID) {
		this.docID = docID;
	}

	public void updateNorm(float weight) {
		this.norm += weight*weight;
	}
	
	public String getDocID() {
		return this.docID;
	}
	
	public Path getDocPath() {
		return this.docPath;
	}
	
	public float getNorm() {
		return this.norm;
	}
	
	public String getType() {
		return this.type;
	}
	
	@Override
	public int compareTo(Document otherDoc) {
		return this.docID.compareTo(otherDoc.getDocID());
	}
}
