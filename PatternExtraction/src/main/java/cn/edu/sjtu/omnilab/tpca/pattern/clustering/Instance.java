package cn.edu.sjtu.omnilab.tpca.pattern.clustering;


public class Instance {
	/** Global variable to generate ID for instance */
	static int nextID = 0;
	/** ID of the instance */
	private final int ID;
	/** The real data object of this instance*/
	private Object dataValue;
	
	/**
	 * Default constructor.
	 * @param classValue The class extending this abstract class
	 */
	public Instance(Object data){
		ID = nextID;
		nextID++;
		this.dataValue = data;
	}
	
	/**
	 * Get the ID of this instance
	 */
	public int getID() { return ID; }
	
	
	/**
	 * Get the real data payload of this instance.
	 */
	public Object dataValue() { return this.dataValue; }
    
	/**
	 * Set the real data payload of this instance.
	 * @param data The real data payload of this instance.
	 */
    public void setDataValue(Object data) { this.dataValue = data; }
	
	
    /**
     * Get the instance signature.
     */
	@Override
	public int hashCode() { return ID; }


	/**
	 * Compare two instance if they are the same.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Instance other = (Instance) obj;
		if (ID != other.ID)
			return false;
		return true;
	}
}
