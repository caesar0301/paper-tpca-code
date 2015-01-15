package omnilab.bd.pattern.graph;

public class PatternGraphEdge {
	// similarity
	private double similarity = -1;
	
	
	/**
	 * Empty constructor.
	 */
	public PatternGraphEdge() {}
	
	
	/**
	 * Similarity setter
	 * @param sim The similarity of two vertices of this edge.
	 */
	public void setSimilarity(double sim){
		similarity = sim;
	}
	
	
	/**
	 * Similarity getter
	 */
	public double getSimilarity(){
		return similarity;
	}
}
