package cn.edu.sjtu.omnilab.tpca.pattern.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.edu.sjtu.omnilab.tpca.pattern.graph.PatternGraph;
import cn.edu.sjtu.omnilab.tpca.pattern.graph.PatternGraphNode;
import cn.edu.sjtu.omnilab.tpca.pattern.utils.SequencesHelper;

/**
 * An implementation of change measure between pattern sets.
 * This measure considers the types of pattern while evolving.
 * @author chenxm
 *
 */
public class PatternSetChange {
	
	/**
	 * Calculate the measure of change in BITS between two pattern sets.
	 * We generate the Pattern Evolution Graph, classify the 
	 * patterns, and finally calculate average change between different connections.
	 * @param ps1
	 * @param ps2
	 * @param minsim
	 * @return
	 */
	public static double changeInformationMeasure(PatternSet ps1, PatternSet ps2, double minsim){
		List<PatternSet> patternSetList = new ArrayList<PatternSet>();
		patternSetList.add(ps1); patternSetList.add(ps2);
		
		PatternGraph graph = new PatternGraph(patternSetList, minsim);
		graph.classifyingPatterns();
		
		//System.out.println(graph);
		
		double changeTotalInformation = 0.0;
		
		int ps_index = 0;
		for ( int i = 0; i < ps1.size(); i++){
			PatternGraphNode node = graph.getNode(ps_index, i);
			if ( (node.getType() & PatternGraphNode.TYPE_PERISHED) > 0 ){
				double sup = node.getPattern().getRelativeSupport();
				double perished_change = (1.0 - node.getMaxFwSim()) * sup;
				double cinfo = Math.abs(Math.log(1+perished_change)/Math.log(2));
				//System.out.println(String.format("[%d,%d] %s", node.getPatternSetIndex(), node.getPatternIndex(), "PR: "+cinfo));
				//System.out.println("PR sup: "+sup);
				changeTotalInformation += cinfo;
			}
		}
		
		ps_index = 1;
		for ( int i = 0; i < ps2.size(); i++){
			PatternGraphNode node = graph.getNode(ps_index, i);
			
			// for added patterns
			if ( (node.getType() & PatternGraphNode.TYPE_APPEARING) > 0 ){
				// added change equals to ( minimum_distance * support )
				double sup = node.getPattern().getRelativeSupport();
				double added_change = (1.0 - node.getMaxBwSim()) * sup;
				double cinfo = Math.abs(Math.log(1+added_change)/Math.log(2));
				//System.out.println(String.format("[%d,%d] %s", node.getPatternSetIndex(), node.getPatternIndex(), "AP: "+cinfo));
				changeTotalInformation += cinfo;
			}
			
			// for unexpected changed patterns
			if ( (node.getType() & PatternGraphNode.TYPE_STATIONARY) > 0 ){
				
				double local_cinfo = .0;
				for ( PatternGraphNode adj_node : graph.adj(node)){
					// emerging change equals to similarity * abs(support1-support2) / support 1
					double support1 = adj_node.getPattern().getRelativeSupport();
					double support2 = node.getPattern().getRelativeSupport();
					double similarity = PatternSimilarity.LCSSimilarityWeightedByLength(
							adj_node.getPattern(), node.getPattern());
					double maxsup = support1;
					if ( maxsup < support2 )
						maxsup  = support2;
					double stationary_change = Math.pow(Math.E, 1-similarity) * Math.abs(support1 - support2) / support1;
					double cinfo = 0;
					if ( stationary_change != 0)
						cinfo = Math.abs(Math.log(1+stationary_change)/Math.log(2));
					//System.out.println(String.format("[%d,%d] %s", node.getPatternSetIndex(), node.getPatternIndex(), "ST: "+cinfo));
					local_cinfo += cinfo;
				}
				int local_count = graph.adj(node).size();
//				if ( local_count > 0 )
//					changeTotalInformation += (local_cinfo / local_count);
				changeTotalInformation += local_cinfo;
			}			
		}
		
		return changeTotalInformation;
	}

	
	/**
	 * Calculate the time span between pattern sets.
	 * Time span is derived as different in days for the interval medians of two pattern sets.
	 * @param ps1
	 * @param ps2
	 * @param helper
	 * @return
	 */
	public static double calTimeSpanDays(PatternSet ps1, PatternSet ps2, SequencesHelper helper){
		// for pattern set 1
		Date ps1_date_start = ps1.getDateInterval(helper, false);
		Date ps1_date_end = ps1.getDateInterval(helper, true);
		
		// for pattern set 2
		Date ps2_date_start = ps2.getDateInterval(helper, false);
		Date ps2_date_end = ps2.getDateInterval(helper, true);
		
		// time span in days
		double ps1_sw_med = 0.5 * (ps1_date_end.getTime()/1000 + ps1_date_start.getTime()/1000);
		double ps2_sw_med = 0.5 * (ps2_date_end.getTime()/1000 + ps2_date_start.getTime()/1000);
		double time_span = Math.abs(ps2_sw_med - ps1_sw_med) / 3600 / 24;
		
		return time_span;
	}
}
