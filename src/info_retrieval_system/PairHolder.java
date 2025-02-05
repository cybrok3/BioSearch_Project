package info_retrieval_system;

public class PairHolder {
	
	private float tf;
	private long pointer;
	
	private int df;
	
	private String type;
	private String path;
	private String name;

	public PairHolder(float tf, long pointer) {
		this.tf = tf;
		this.pointer = pointer;
	}
	
	// This constructor is meant for holding df, pointer
	public PairHolder(int df, long pointer) {
		this.df = df;
		this.pointer = pointer;
	}
	
	// This constructor meant for holding norm(float value named tf), type
	public PairHolder(float tf, String type, String path) {
		this.tf = tf;
		this.type = type;
		this.path = path;
	}
	
	public PairHolder(String name , String path) {
		this.name = name;
		this.path = path;
	}
	
	public float getTF() {
		return this.tf;
	}
	
	public int getDF() {
		return this.df;
	}
	
	public long getPointer() {
		return this.pointer;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public String getName() {
		return this.name;
	}
	
}
