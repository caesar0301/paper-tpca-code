package cn.edu.sjtu.omnilab.tpca.pattern.core;


/**
 * A temporary implementation of distance measure between pattern sets.
 * @author chenxm
 *
 */
public class PatternSetSimilarity {
	
	public static double similarityWeightedBySupport(PatternSet ps1, PatternSet ps2){
		/*
		 * Pattern Set similarity algo. by [Ying, Lu el. 2010]
		 */
		SimilariyMatricsEntity[][] smatrics = similariyMatrics(ps1, ps2);
		return symmetricSimilarity(smatrics);
	}
	
	
	public static SimilariyMatricsEntity[][] similariyMatrics(PatternSet ps1, PatternSet ps2){
		SimilariyMatricsEntity[][] smat = new SimilariyMatricsEntity[ps1.size()][ps2.size()];
		for ( int i = 0; i < ps1.size(); i++){
			Pattern p1 = ps1.getPattern(i);
			for ( int j = 0; j < ps2.size(); j++){
				SimilariyMatricsEntity new_ent = new SimilariyMatricsEntity();
				Pattern p2 = ps2.getPattern(j);
				new_ent.support1 = p1.getRelativeSupport();
				new_ent.support2 = p2.getRelativeSupport();
				new_ent.sym_sim = PatternSimilarity.LCSSimilarityWeightedByLength(p1, p2);	// length-weighted similarity
				smat[i][j] = new_ent;
			}
		}
		return smat;
	}
	
	
	private static double symmetricSimilarity(SimilariyMatricsEntity[][] smatrics){
		/*
		 * The weighted similarity sum is calculated for each pair of (pi, pj), where
		 * pi is arbitrary pattern from pattern set 1 and pj from pattern set 2;
		 * 
		 * The term 'symmetric' comes from that calculations from both directions are always equal. 
		 */
		double weightedSimilaritySum = 0.0;
		double weightSum = 0.0;
		for ( int i = 0; i < smatrics.length; i++ ){
			for ( int j = 0; j < smatrics[0].length; j++){
				double mean_supp = Math.sqrt(smatrics[i][j].support1 * smatrics[i][j].support2);
				/*
				 * To weight by arithmetic mean using
				 * double mean_supp = 0.5 * (smatrics[i][j].support1 + smatrics[i][j].support2);
				 */
				weightSum += mean_supp;
				weightedSimilaritySum += mean_supp * smatrics[i][j].sym_sim;
			}
		}
		return weightedSimilaritySum / weightSum;
	}
	
	
	private static double asymmetricSimilarity(SimilariyMatricsEntity[][] smatrics){
		double weightedSimilaritySum = 0.0;
		double weightSum = 0.0;
		// Pattern1 --> pattern2
		for ( int i = 0; i < smatrics.length; i++ ){
			double temp_sim_value = 0;
			double temp_support1 = 0;
			double temp_support2 = 0;
			for ( int j = 0; j < smatrics[0].length; j++){
				if ( smatrics[i][j].sym_sim > temp_sim_value ){
					temp_sim_value = smatrics[i][j].sym_sim;
					temp_support1 = smatrics[i][j].support1;
					temp_support2 = smatrics[i][j].support2;
				}
			}
			double mean_supp = Math.sqrt(temp_support1 * temp_support2);
			weightSum += mean_supp;
			weightedSimilaritySum += mean_supp * temp_sim_value;
		}
		// pattern2 --> pattern1
		for ( int j = 0; j < smatrics[0].length; j++ ){
			double temp_sim_value = 0;
			double temp_support1 = 0;
			double temp_support2 = 0;
			for ( int i = 0; i < smatrics.length; i++){
				if ( smatrics[i][j].sym_sim > temp_sim_value ){
					temp_sim_value = smatrics[i][j].sym_sim;
					temp_support1 = smatrics[i][j].support1;
					temp_support2 = smatrics[i][j].support2;
				}
			}
			double mean_supp = Math.sqrt(temp_support1 * temp_support2);
			weightSum += mean_supp;
			weightedSimilaritySum += mean_supp * temp_sim_value;
		}
		if (weightSum == 0)
			return 0;
		return weightedSimilaritySum / weightSum;
	}
}
