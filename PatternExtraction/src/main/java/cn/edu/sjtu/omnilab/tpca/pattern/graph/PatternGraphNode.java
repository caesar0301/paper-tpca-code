package cn.edu.sjtu.omnilab.tpca.pattern.graph;

import cn.edu.sjtu.omnilab.tpca.pattern.chain.PatternChainNode;
import cn.edu.sjtu.omnilab.tpca.pattern.core.Pattern;


/**
 * PatternGraphNode
 * @author chenxm
 */
public class PatternGraphNode {
	// the index of pattern set in pattern set list
	private int indexPatternSet;
	// the index of pattern in pattern set ( which is converted into a list innerly)
	private int indexPattern;
	// original pattern
	private Pattern pattern;
	// maximum backward similarity to the previous sliding window, which is used for added patterns
	private double maxBackwardSimilarity = -1;
	// maximum forward similarity to the following slinding window, which is used for the perished patterns
	private double maxForwardSimilarity = -1;
	
	// pattern type indicators
	public static final int TYPE_STATIONARY = 0x0001;
	public static final int TYPE_APPEARING = 0x0002;
	public static final int TYPE_PERISHED = 0x0004;
	// pattern type
	private int type = 0x0000; // unset

	
	/**
	 * Default constructor.
	 * @param indexPattern The index of a pattern in a pattern set.
	 * @param indexPatternSet The index of a pattern set in a list.
	 */
	public PatternGraphNode(int indexPattern, int indexPatternSet){
		this.indexPattern = indexPattern;
		this.indexPatternSet = indexPatternSet;
	}
	
	
	/**
	 * Set the pattern this node refers to.
	 * @param p The original pattern.
	 */
	public void setPattern(Pattern p){
		this.pattern = p;
	}
	
	
	/**
	 * Get the original pattern this node refers to.
	 * @return The original pattern.
	 */
	public Pattern getPattern(){
		return this.pattern;
	}
	
	
	/**
	 * Type setter
	 * @param pattern_type
	 */
	public void setType(int pattern_type){
		this.type = (this.type | pattern_type);
	}
	
	
	/**
	 * Type getter
	 * @return
	 */
	public int getType(){
		return this.type;
	}
	
	
	public void setMaxBwSim(double sim){
		maxBackwardSimilarity = sim;
	}
	
	
	public void setMaxFwSim(double sim){
		maxForwardSimilarity = sim;
	}
	
	
	public double getMaxBwSim(){
		return maxBackwardSimilarity;
	}
	
	
	public double getMaxFwSim(){
		return maxForwardSimilarity;
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
	
	
	/**
	 * Get the plain representation of this node.
	 * @return A string.
	 */
	public String toString(){
		StringBuffer r = new StringBuffer();
		r.append(String.format("[%s,%s]", indexPatternSet, indexPattern));
		return r.toString();
	}
}
