package omnilab.bd.pattern.chain;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import omnilab.bd.pattern.core.PatternSet;
import omnilab.bd.pattern.core.PatternSetSimilarity;
import omnilab.bd.pattern.core.SimilariyMatricsEntity;

public class PatternChainSet {
	// the original list of pattern set
	private List<PatternSet> patternSetList;
	// generated pattern chains
	private List<PatternChain> patternChains;
	
	
	/**
	 * Default constructor.
	 * @param patternSetList The backward reference to the list of pattern sets
	 */
	public PatternChainSet(List<PatternSet> patternSetList){
		this.patternSetList = patternSetList;
		this.patternChains = new LinkedList<PatternChain>();
	}
	
	
	/**
	 * Get the list of pattern sets
	 * @return A list of pattern sets.
	 */
	public List<PatternSet> getPatternSets(){
		return this.patternSetList;
	}
	
	
	/**
	 * Get the list of pattern chains.
	 * @return A list of pattern chains.
	 */
	public List<PatternChain> getPatternChains(){
		return this.patternChains;
	}
	
	
	/**
	 * Mining all pattern chains from given pattern sets
	 * @param min_sim The minimum similarity value with which the pattern evolves.
	 */
	public void minePatternChains(double min_sim){
		
		for ( int i = 0; i < this.patternSetList.size(); i++){
			if ( i == 0 ){
				// first pattern set, add all patterns as the first nodes of chains.
				for ( int j = 0; j < this.patternSetList.get(i).size(); j++ ){
					int indexPattern = j;
					PatternChainNode new_node = new PatternChainNode(indexPattern, i);
					PatternChain new_chain =  new PatternChain();
					new_chain.appendPatternChainNode(new_node);
					this.patternChains.add(new_chain);
				}
			} else {
				// two pattern sets
				PatternSet ps1 = patternSetList.get(i-1);
				PatternSet ps2 = patternSetList.get(i);
				// pattern sets similarity matrics
				SimilariyMatricsEntity[][] smat = PatternSetSimilarity.similariyMatrics(ps1, ps2);
				// pattern set index
				int patternSetIndex_t = i-1;
				int patternSetIndex_tk = i;
			
				// FORWARD: find the most similar pattern in (t+k) period to pattern of (t) period
				for ( int j = 0; j < ps1.size(); j++ ){
					// j represent index of pattern set (t)
					double sim_value = -1;
					int patternIndex_t = j;
					int patternIndex_tk = 0;
					for ( int k = 0; k < smat[0].length; k++){
						// k represent index of pattern set (t+k)
						if ( smat[j][k].sym_sim > sim_value ){
							sim_value = smat[j][k].sym_sim;
							patternIndex_tk = k;
						}
					}
					if ( sim_value != 0 && sim_value >=  min_sim ){
						// if the most similar pattern is found
						PatternChainNode new_node = new PatternChainNode(patternIndex_tk, patternSetIndex_tk);
						new_node.setType(PatternChainNode.TYPE_STATIONARY);
						appendPatternNodeToChainAtIndexOfPatternSet(new_node, patternIndex_t, patternSetIndex_t );
					} else {
						// otherwise, set the pattern as perished.
						setPatternNodeTypeAtIndexOfPatternset(PatternChainNode.TYPE_PERISHED, patternIndex_t, patternSetIndex_t );
					}
				}
				
				
				// BACKWARD: find the most similar pattern in (t) period to pattern of (t+k) period
				for ( int j = 0; j < ps2.size(); j++ ){
					// j represent index of pattern set (t+k)
					double sim_value = -1;
					int patternIndex_t = 0;
					int patternIndex_tk = j;
					for ( int k = 0; k < smat.length; k++){
						// k represent index of pattern set (t)
						if ( smat[k][j].sym_sim > sim_value ){
							sim_value = smat[k][j].sym_sim;
							patternIndex_t = k;
						}
					}
					if ( sim_value != 0 && sim_value >=  min_sim ){
						// if the most similar pattern is found
						PatternChainNode new_node = new PatternChainNode(patternIndex_tk, patternSetIndex_tk);
						new_node.setType(PatternChainNode.TYPE_STATIONARY);
						appendPatternNodeToChainAtIndexOfPatternSet(new_node, patternIndex_t, patternSetIndex_t );
					} else {
						// otherwise, a new pattern appears, then we create a new chain for it
						PatternChainNode new_node = new PatternChainNode(patternIndex_tk, patternSetIndex_tk);
						PatternChain new_chain = new PatternChain();
						new_chain.appendPatternChainNode(new_node);
						this.patternChains.add(new_chain);
					}
				}
			}
		}
	}
	
	
	/**
	 * Append a node to pattern chain after existing node with specific patternIndex and patternSetIndex.
	 * @param node An object of PatternNode to be appended.
	 * @param patternIndex The index of pattern in a pattern set.
	 * @param patternSetIndex The index of a pattern set in a list.
	 */
	private void appendPatternNodeToChainAtIndexOfPatternSet(PatternChainNode node, int patternIndex, int patternSetIndex){
		ListIterator<PatternChain> litPatternChain = this.patternChains.listIterator();
		while ( litPatternChain.hasNext() ){
			PatternChain chain = litPatternChain.next();
			PatternChainNode last_node = chain.getNode(chain.size()-1);
			
			if ( last_node.equals(node))  continue;
			if (last_node.getType() == PatternChainNode.TYPE_PERISHED){
				this.processPatternChain(chain);
				litPatternChain.remove();
				System.out.println("remove");
				continue;
			}
			
			if ( last_node.getPatternIndex() == patternIndex && last_node.getPatternSetIndex() == patternSetIndex ){
				if ( last_node.getType() != PatternChainNode.TYPE_ADDED )
					last_node.setType(PatternChainNode.TYPE_STATIONARY);
				chain.appendPatternChainNode(node);
			} else {
				// for security
				if ( chain.size() == 1 )  continue;
				PatternChainNode last_2nd_node = chain.getNode(chain.size()-2);
				if ( last_2nd_node.getPatternIndex() == patternIndex && last_2nd_node.getPatternSetIndex() == patternSetIndex ){
					PatternChain clone_chain = chain.clone();
					clone_chain.getNodes().remove(clone_chain.size()-1);
					if ( last_2nd_node.getType() != PatternChainNode.TYPE_ADDED )
						last_2nd_node.setType(PatternChainNode.TYPE_STATIONARY);
					clone_chain.appendPatternChainNode(node);
					litPatternChain.add(clone_chain);
				}
			}
		}
	}
	
	
	/**
	 * Set node type with specific patternIndex and patternSetIndex.
	 * @param type The node type among PatternNode.TYPE_*.
	 * @param patternIndex The index of a pattern in pattern set.
	 * @param patternSetIndex The index of a pattern set in a list.
	 */
	private void setPatternNodeTypeAtIndexOfPatternset(int type, int patternIndex, int patternSetIndex){
		for ( PatternChain chain : this.patternChains ){
			PatternChainNode last_node = chain.getNode(chain.size()-1);
			if ( last_node.getPatternIndex() == patternIndex && last_node.getPatternSetIndex() == patternSetIndex ){
				last_node.setType(PatternChainNode.TYPE_PERISHED);
			}
		}
	}
	
	
	private void processPatternChain(PatternChain chain){
		// TODO: process chain
		return;
	}
}
