package cn.edu.sjtu.omnilab.tpca.pattern.chain;

public class PatternChainNode {
	// the index of pattern set in pattern set list
	private int indexPatternSet;
	// the index of pattern in pattern set ( which is converted into a list innerly)
	private int indexPattern;
	// node type
	private short type;
	
	// node type candidates
	public static final short TYPE_ADDED = 100;
	public static final short TYPE_STATIONARY = 101;
	public static final short TYPE_PERISHED = 102;

	
	/**
	 * Default constructor.
	 * @param indexPattern The index of a pattern in a pattern set.
	 * @param indexPatternSet The index of a pattern set in a list.
	 */
	public PatternChainNode(int indexPattern, int indexPatternSet){
		this.indexPattern = indexPattern;
		this.indexPatternSet = indexPatternSet;
		this.type = PatternChainNode.TYPE_ADDED;
	}
	
	
	/**
	 * Set the type of the node.
	 * @param type The type value.
	 */
	public void setType(short type){
		this.type = type;
	}
	
	
	/**
	 * Check if two nodes equals.
	 * @param node The node to which compared.
	 * @return True if they represent the same pattern, otherwise, false.
	 */
	public boolean equals(PatternChainNode node){
		// equal if they are the same pattern in the same pattern set
		if ( this.indexPattern == node.getPatternIndex() && 
				this.indexPatternSet == node.getPatternSetIndex() )
			return true;
		return false;
	}
	
	
	/**
	 * Get the type value of the node.
	 * @return The value of node type.
	 */
	public int getType(){
		return this.type;
	}
	
	
	/**
	 * Get the index of pattern set to which this node belonging to.
	 * @return The index of the pattern set.
	 */
	public int getPatternSetIndex(){
		return this.indexPatternSet;
	}
	
	
	/**
	 * Get the index of pattern to which this node belonging to.
	 * @return The index of the pattern.
	 */
	public int getPatternIndex(){
		return this.indexPattern;
	}
	
	
	public PatternChainNode clone(){
		PatternChainNode clone = new PatternChainNode(this.indexPattern, this.indexPatternSet);
		clone.setType(this.type);
		return clone;
	}
	
	
	/**
	 * Get the plain representation of this node.
	 * @return A string.
	 */
	public String toString(){
		StringBuffer r = new StringBuffer();
		r.append("{");
		r.append(indexPatternSet+"("+indexPattern+")"+type);
		r.append("}");
		return r.toString();
	}
}
