package omnilab.bd.pattern.graph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


/**
 * PatternPath
 * A path is a constructed with a list of nodes and edges.
 * ALl nodes take an ascending order by time.
 * @author chenxm
 */
public class PatternPath {
	// all nodes along this path
	private List<PatternGraphNode> nodes;
	
	
	/**
	 * Default constructor.
	 */
	public PatternPath(){
		this.nodes = new LinkedList<PatternGraphNode>();
	}
	
	
	/**
	 * Add a new node to this path
	 * @param node The graph node to add.
	 */
	public void addNode(PatternGraphNode node){
		this.nodes.add(node);
	}
	
	
	/**
	 * Save this path to file on disk.
	 * @param filename The file to store this path.
	 * @throws IOException
	 */
	public void save2file(String filename) throws IOException{
		FileWriter fw = new FileWriter(new File(filename), true);
		fw.write( this.toString()+"\n" );
		fw.close();
	}
	
	
	/**
	 * print function.
	 */
	public String toString(){
		StringBuffer r = new StringBuffer("");
		for ( PatternGraphNode node : nodes ){
			if ( r.length() == 0 )
				r.append(node+":"+node.getPattern().getPatternItemsetListAsString());
			else
				r.append(" --> " + node+":"+node.getPattern().getPatternItemsetListAsString());
		}
		return r.toString();
	}
}
