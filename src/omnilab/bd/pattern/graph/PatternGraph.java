package omnilab.bd.pattern.graph;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import omnilab.bd.pattern.core.PatternSet;
import omnilab.bd.pattern.core.PatternSetSimilarity;
import omnilab.bd.pattern.core.SimilariyMatricsEntity;


/**
 * PatternGraph
 * @author chenxm
 */
public class PatternGraph {
	// inner class helping to create the graph.
	class ConnectionPair{
		public PatternGraphNode node;
		public PatternGraphEdge edge;
	}
	
	// The count of edges
	private int E;
	// All nodes in this graph
	private List<PatternGraphNode> nodes;
	// The map-based implementation of adjacency-lists representation.
	private Map<PatternGraphNode, Set<ConnectionPair>> adj;
	// Minimum support for similarity calculation
	private double minsim = 0.000001;
	
	
	/**
	 * Default constructor.
	 */
	public PatternGraph(double min_sim){
		this.E = 0;
		this.nodes = new LinkedList<PatternGraphNode>();
		this.adj = new HashMap<PatternGraphNode, Set<ConnectionPair>>();
		this.minsim = min_sim;
	}
	
	
	/**
	 * Create a pattern graph from sets of patterns.
	 * @param patternSetList The list of pattern sets.
	 * @param min_sim The minimum similarity to find nearest patterns.
	 * @return A pattern graph.
	 */
	public PatternGraph(List<PatternSet> patternSets, double min_sim){
		this.E = 0;
		this.nodes = new LinkedList<PatternGraphNode>();
		this.adj = new HashMap<PatternGraphNode, Set<ConnectionPair>>();
		this.minsim = min_sim;

		for ( int i = 0; i < patternSets.size(); i++){
			// for each pattern set
			if ( i == 0 ){
				// the first pattern set, add it to the graph directly
				for ( int j=0; j < patternSets.get(i).size(); j++){
					PatternGraphNode new_node = new PatternGraphNode(j, i);
					new_node.setPattern(patternSets.get(i).getPattern(j));
					this.addNode(new_node);
				}
				continue;
			}
			
			// otherwise, i > 0
			int patternSetIndex_t = i-1;
			int patternSetIndex_tk = i;
			PatternSet ps1 = patternSets.get(patternSetIndex_t);
			PatternSet ps2 = patternSets.get(patternSetIndex_tk);
			// add the second pattern set to graph directly
			for ( int j = 0; j < ps2.size(); j++ ){
				PatternGraphNode new_node = new PatternGraphNode(j, i);
				new_node.setPattern(patternSets.get(i).getPattern(j));
				this.addNode(new_node);	
			}
			
			// calculate pattern similarities of these two pattern sets
			SimilariyMatricsEntity[][] smat = PatternSetSimilarity.similariyMatrics(ps1, ps2);
			
			// FORWARD: find the most similar pattern in (t+k) period to pattern of (t) period
			for ( int j = 0; j < ps1.size(); j++ ){
				// j represent index of pattern set (t)
				double max_sim = -1;
				int patternIndex_t = j;
				Set<Integer> patternIndex_tk = new HashSet<Integer>();
				for ( int k = 0; k < smat[0].length; k++){
					// k represent index of pattern set (t+k)
					if ( smat[j][k].sym_sim == max_sim ){
						patternIndex_tk.add(k);
					} else if ( smat[j][k].sym_sim > max_sim ){
						max_sim = smat[j][k].sym_sim;
						patternIndex_tk.clear();
						patternIndex_tk.add(k);
					}
				}
				
				if ( max_sim != 0 && max_sim >=  this.minsim ){
					// if the most similar pattern is found, add edge between these two nodes
					for ( Integer tk : patternIndex_tk)
						this.addEdge(this.getNode(patternSetIndex_t, patternIndex_t), 
								this.getNode(patternSetIndex_tk, tk), 
								max_sim);
				} else {
					// record the max similarity calculation for future usage.
					this.getNode(patternSetIndex_t, patternIndex_t).setMaxFwSim(max_sim);
				}
			}
			
			// BACKWARD: find the most similar pattern in (t) period to pattern of (t+k) period
			for ( int j = 0; j < ps2.size(); j++ ){
				// j represent index of pattern set (t+k)
				double max_sim = -1;
				Set<Integer> patternIndex_t = new HashSet<Integer>();
				int patternIndex_tk = j;
				
				for ( int k = 0; k < smat.length; k++){
					double sym_sim = smat[k][j].sym_sim;
					// k represent index of pattern set (t)
					if ( sym_sim == max_sim ){
						patternIndex_t.add(k);
					} else if ( sym_sim > max_sim ){
						max_sim = sym_sim;
						patternIndex_t.clear();
						patternIndex_t.add(k);
					}
				}
				if ( max_sim != 0 && max_sim >=  this.minsim ){
					// if the most similar pattern is found, add edge between these two nodes
					for ( Integer t : patternIndex_t)
						this.addEdge(this.getNode(patternSetIndex_t, t), 
								this.getNode(patternSetIndex_tk, patternIndex_tk), 
								max_sim);
				} else {
					// record the max similarity calculation for future usage.
					this.getNode(patternSetIndex_tk, patternIndex_tk).setMaxBwSim(max_sim);
				}
			}
		}
	}
	
	
	/**
	 * Get the count of vertexes (nodes) in this graph.
	 * @return The count of vertexes.
	 */
	public int V() { return nodes.size(); }
	
	
	/**
	 *  Get the count of edges in this graph.
	 * @return The count of edges.
	 */
	public int E() { return E; }
	
	
	/**
	 * Get node representing the pattern with patternIndex in patternSetIndex.
	 * @param patternSetIndex The index of its pattern set.
	 * @param patternIndex The index of pattern in its pattern set.
	 * @return A specific graph node.
	 */
	public PatternGraphNode getNode( int patternSetIndex, int patternIndex){
		for ( PatternGraphNode node : nodes)
			if ( node.getPatternSetIndex() == patternSetIndex && node.getPatternIndex() == patternIndex )
				return node;
		return null;
	}
	
	
	/**
	 * Get an iterable object of all nodes in this graph.
	 * @return An interable object of all nodes.
	 */
	public Iterable<PatternGraphNode> getNodes(){
		return this.nodes;
	}
	
	
	/**
	 * Add a new node to this graph.
	 * @param node The node to be added.
	 */
	public void addNode(PatternGraphNode node){
		this.nodes.add(node);
		this.adj.put(node, new HashSet<ConnectionPair>());
	}
	
	
	/**
	 * Add an edge between two nodes.
	 * @param node1 One of the two nodes.
	 * @param node2 The other of the two nodes.
	 */
	public void addEdge(PatternGraphNode node1, PatternGraphNode node2, double similarity){
		PatternGraphEdge new_edge = new PatternGraphEdge();
		new_edge.setSimilarity(similarity);
		if ( !connectionExistsBetween(node1, node2) ){
			// for node1
			ConnectionPair pair1 = new ConnectionPair();
			pair1.node = node2;
			pair1.edge = new_edge;
			this.adj.get(node1).add(pair1);
			// for node2
			ConnectionPair pair2 = new ConnectionPair();
			pair2.node = node1;
			pair2.edge = new_edge;
			this.adj.get(node2).add(pair2);
			// update edge conunt
			this.E++;
		}
	}
	
	
	/**
	 * Check if the connection from n1 to n2 is already recorded.
	 * @param n1
	 * @param n2
	 * @return
	 */
	private boolean connectionExistsBetween(PatternGraphNode n1, PatternGraphNode n2){
		boolean flag = false;
		// from n1
		for (ConnectionPair pair : this.adj.get(n1)){
			if ( pair.node.equals(n2) )
				flag = true;
		}
		// from n2
		for (ConnectionPair pair : this.adj.get(n2)){
			if ( pair.node.equals(n1) )
				flag = true;
		}
		return flag;
	}
	
	
	/**
	 * Get adjacent nodes of specific node in this graph.
	 * @param node The target node.
	 * @return A list of the adjacent nodes of this graph.
	 */
	public List<PatternGraphNode> adj(PatternGraphNode node){
		List<PatternGraphNode> adj_nodes = new LinkedList<PatternGraphNode>();
		for ( ConnectionPair pair : this.adj.get(node)){
			adj_nodes.add(pair.node);
		}
		return adj_nodes;
	}
	
	
	/**
	 * Check if a node has predecessor.
	 * In our pattern evolution mining, the predecessor means an adjacent node
	 * whose pattern set index is smaller than this node.
	 * @param node The target node.
	 * @return boolean flag.
	 */
	public boolean hasPredecessor(PatternGraphNode node){
		return getPredecessors(node).size() > 0 ? true : false;
	}
	
	private List<ConnectionPair> getPredecessors(PatternGraphNode node){
		List<ConnectionPair> preds = new LinkedList<ConnectionPair>();
		for ( ConnectionPair pair : this.adj.get(node) ){
			if ( pair.node.getPatternSetIndex() < node.getPatternSetIndex() ){
				preds.add(pair);
			}
		}
		return preds;
	}
	
	
	/**
	 * Check if a node has successor.
	 * In our pattern evolution mining, the successor means an adjacent node
	 * whose pattern set index is larger than this node.
	 * @param node The target node.
	 * @return boolean flag.
	 */
	public boolean hasSuccessor(PatternGraphNode node){
		return getSuccessors(node).size() > 0 ? true : false;
	}
	
	private List<ConnectionPair> getSuccessors(PatternGraphNode node){
		List<ConnectionPair> succs = new LinkedList<ConnectionPair>();
		for ( ConnectionPair pair : this.adj.get(node) ){
			if ( pair.node.getPatternSetIndex() > node.getPatternSetIndex() ){
				succs.add(pair);
			}
		}
		return succs;
	}
	
	
	/**
	 * Get all paths starting with specific node.
	 * @param node The start node.
	 * @return A list of pattern path regarding to this node.
	 */
	public List<PatternPath> getAllPathsOfNode(PatternGraphNode node){
		List<PatternPath> paths = new LinkedList<PatternPath>();
		Stack<PatternGraphNode> cur_stack = new Stack<PatternGraphNode>();
		// push the root into stack
		cur_stack.push(node);
		dfs(node, cur_stack, paths);
		return paths;
	}
	
	
	/**
	 * Deep first search implementation of graph searching.
	 * @param node The node to start with.
	 * @param cur_stack The stack storing encountered nodes by now.
	 * @param paths A list to store found paths.
	 */
	private void dfs(PatternGraphNode node, Stack<PatternGraphNode> cur_stack, List<PatternPath> paths){
		if( hasSuccessor(node) ){
			// if it has successors, then for each adjacent node
			for ( PatternGraphNode adjNode : adj(node) ){
				// if the adjacent node is from the next pattern set
				if ( adjNode.getPatternSetIndex() > node.getPatternSetIndex() ){
					cur_stack.push(adjNode);
					dfs(adjNode, cur_stack, paths);
					cur_stack.pop();
				}
			}
		} else {
			// otherwise, export all the nodes in stack
			PatternPath new_path = new PatternPath();
			for ( PatternGraphNode n : cur_stack )  { new_path.addNode(n); }
			paths.add(new_path);
		}
	}
	
	
	/**
	 * Print function.
	 */
	public String toString(){
		StringBuffer r = new StringBuffer();
		r.append(String.format("======== PATTERN GRAPH (V:%d, E:%d) ========\n", V(), E()));
		for( PatternGraphNode node : this.nodes ){
			r.append(String.format("%s %s: ", node, 
					node.getPattern().getPatternItemsetListAsString()));
			for ( ConnectionPair pair : this.adj.get(node)){
				r.append(String.format("%s,%.2f  ", pair.node, 
						pair.edge.getSimilarity()));
			}
			if ( (node.getType() & PatternGraphNode.TYPE_APPEARING) > 0 )
				r.append("AP ");
			if ((node.getType() & PatternGraphNode.TYPE_PERISHED) > 0)
				r.append("PR ");
			if ((node.getType() & PatternGraphNode.TYPE_STATIONARY) > 0)
				r.append("ST ");
			r.append('\n');
		}
		return r.toString();
	}
	
	
	/**
	 * Detect the pattern types according to the relationships of patterns
	 * between sliding windows.
	 */
	public void classifyingPatterns(){
		for ( PatternGraphNode node : this.nodes){
			if (!hasSuccessor(node))
				node.setType(PatternGraphNode.TYPE_PERISHED);
			if (!hasPredecessor(node))
				node.setType(PatternGraphNode.TYPE_APPEARING);
			else
				node.setType(PatternGraphNode.TYPE_STATIONARY);
		}
	}
	
	
	/**
	 * Dump all possible paths in this graph.
	 * @param outputFile
	 * @throws IOException
	 */
	public void outputAllPaths(String outputFile) throws IOException{
		for ( PatternGraphNode node : this.getNodes())
			// check if the node has predecessor or not
			if ( !this.hasPredecessor(node) )
				// a new start node of a pattern path
				for (PatternPath path :  this.getAllPathsOfNode(node)){
					// for each pattern path of the node
					path.save2file(outputFile);
				}
	}
	
	
	/**
	 * TEST MAIN
	 * @param args
	 */
	public static void main(String[] args){
		PatternGraph graph = new PatternGraph(0.1);
		System.out.println(graph);
	}
}
