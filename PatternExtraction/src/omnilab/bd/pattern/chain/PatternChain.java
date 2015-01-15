package omnilab.bd.pattern.chain;

import java.util.LinkedList;
import java.util.List;

public class PatternChain {
	// list of pattern node
	private List<PatternChainNode> patternChainNodeList = new LinkedList<PatternChainNode>();

	
	/**
	 * Default constructor.
	 */
	public PatternChain(){}
	
	
	/**
	 * Get the size of chain.
	 * @return The number of nodes in the chain.
	 */
	public int size(){
		return this.patternChainNodeList.size();
	}
	
	
	/**
	 * Get all nodes of the chain.
	 * @return A list of nodes belonging to the chain.
	 */
	public List<PatternChainNode> getNodes(){
		return this.patternChainNodeList;
	}
	
	
	/**
	 * Clone the chain.
	 * @return A chain with the same information.
	 */
	public PatternChain clone(){
		PatternChain chain = new PatternChain();
		for ( PatternChainNode node : this.patternChainNodeList ){
			chain.appendPatternChainNode(node.clone());
		}
		return chain;
	}
	
	
	/**
	 * Append a node to the chain.
	 * @param node The object of PatternChainNode to be added.
	 */
	public void appendPatternChainNode(PatternChainNode node){
		this.patternChainNodeList.add(node);
	}
	
	
	/**
	 * Get a node with specific index in the chain.
	 * @param index The index of the node in the chain.
	 * @return A node.
	 */
	public PatternChainNode getNode(int index){
		return this.patternChainNodeList.get(index);
	}
	
	
	/**
	 * 
	 * @param patternSetIndex
	 * @return
	 */
	public PatternChainNode getNodeOfPatternSetAt(int patternSetIndex){
		for ( PatternChainNode node : this.patternChainNodeList ){
			if ( node.getPatternSetIndex() == patternSetIndex )
				return node;
		}
		return null;
	}
	
	
	/**
	 * Get the plain representation of this chain.
	 * @return A string.
	 */
	public String toString(){
		StringBuffer r = new StringBuffer("");
		for ( PatternChainNode node : this.patternChainNodeList ){
			if ( r.length() == 0 )
				r.append(node.toString());
			else
				r.append(" --> " + node.toString());
		}
		return r.toString();
	}
}
