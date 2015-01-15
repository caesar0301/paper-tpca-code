package omnilab.bd.pattern.core;


import java.util.LinkedList;
import java.util.List;

import omnilab.bd.pattern.spmf.itemset.Itemset;

import com.google.common.collect.Lists;



public class PatternSimilarity {
	/*
	 * Considering three weighted factors:
	 * 1. length of pattern
	 * 2. itemset content, i.e., count of common items
	 * 3. position of itemset in a pattern
	 */	
	
	static class RatioPair{
		public double ratio1 = 0;
		public double ratio2 = 0;
		// total common items, used by dynamic programming LCS
		public int count = 0;
		
		public void set(RatioPair pair){
			this.ratio1 = pair.ratio1;
			this.ratio2 = pair.ratio2;
			this.count = pair.count;
		}
	}
	
	
	public static double LCSSimilarityWeightedByLength(Pattern pattern1, Pattern pattern2){
		/*
		 * Similarity algo. of MSTP-Similarity by [Ying, Lu el. 2010]
		 */
		RatioPair rp = calPatternSimilarRatios(pattern1, pattern2);
		int pattern1_size = pattern1.size();
		int pattern2_size = pattern2.size();
		double ratio1 = 1.0 * rp.ratio1 / pattern1_size;
		double ratio2 = 1.0 * rp.ratio2 / pattern2_size;
		// weighted by length
		return (pattern1_size*ratio1 + pattern2_size*ratio2) / (pattern1_size + pattern2_size);
	}
	
	
	public static List<Itemset> LCS(Pattern p1, Pattern p2){
		/*
		 * Longest Common Subsequence of itemset list, a dynamic programming version
		 */
		return LCS(p1.getPatternItemsetList(), p2.getPatternItemsetList());
	}
	
	
	public static List<Itemset> LCS(List<Itemset> La, List<Itemset> Lb){
		RatioPair[][] matrics = calRatioPairMatrics(La, Lb);
		
		// read the common itemsets out from the matrix
		List<Itemset> lcs = new LinkedList<Itemset>();
		for ( int x = La.size(), y = Lb.size(); x != 0 && y != 0; ){
			if (matrics[x][y].count == matrics[x-1][y].count)
				x--;
			else if (matrics[x][y].count == matrics[x][y-1].count)
				y--;
			else {
				Itemset commonItemset = commonSubset(La.get(x-1), Lb.get(y-1));
				assert commonItemset.size() > 0;
				lcs.add(commonItemset);
				x--;
				y--;
			}
		}
		Lists.reverse(lcs);
		return lcs;
	}
	
	
	private static RatioPair calPatternSimilarRatios(Pattern  p1, Pattern p2){
		RatioPair[][] matrics = calRatioPairMatrics(p1.getPatternItemsetList(), p2.getPatternItemsetList());
		return matrics[p1.size()][p2.size()];
	}
	
	
	private static RatioPair[][] calRatioPairMatrics(List<Itemset> La, List<Itemset> Lb){
		/*
		 * Based on the dynamic programming version of LCS
		 * Different to the above, itemset is weighted by its location in the sequence
		 * For itemset i in L-sequence, the weighted value is calculated by exp(L-i+1)/L
		 */
		int aLen = La.size();
		int bLen = Lb.size();
		
		RatioPair[][] c = new RatioPair[aLen+1][bLen+1];
		for (int i = 0; i < aLen+1; i++){
			for (int j = 0; j < bLen+1; j++){
				c[i][j] = new RatioPair();
			}
		}
		
		int last_a_index = -1;
		int last_b_index = -1;
		for ( int i=1; i < aLen+1; i++){
			for ( int j = 1; j < bLen+1; j++){
				Itemset commonSubset = commonSubset(La.get(i-1), Lb.get(j-1));
				int temp_count = c[i-1][j-1].count + commonSubset.size();
				if ( temp_count > c[i-1][j].count && temp_count > c[i][j-1].count){
					c[i][j].count = temp_count;
					// position weight
					double pw_a = 0, pw_b = 0;
					if ( last_a_index != -1 ) 
						pw_a = -1.0 * (i-1-last_a_index-1) / aLen;
					if ( last_b_index != -1 ) 
						pw_b = -1.0 * (j-1-last_b_index-1) / bLen;
					c[i][j].ratio1 = c[i-1][j-1].ratio1 + Math.pow(Math.E, pw_a) * commonSubset.size() / La.get(i-1).size();
					c[i][j].ratio2 = c[i-1][j-1].ratio2 + Math.pow(Math.E, pw_b) * commonSubset.size() / Lb.get(j-1).size();
					
					last_a_index = i-1;
					last_b_index = j-1;
				} else {
					if ( c[i-1][j].count > c[i][j-1].count)
						c[i][j].set(c[i-1][j]);
					else
						c[i][j].set(c[i][j-1]);
				}
			}
		}
		
		return c;
	}
	
	
	private static Itemset commonSubset(Itemset a, Itemset b){
		Itemset commonItemset = new Itemset();
		List<Integer> aItems = a.getItems();
		List<Integer> bItems = b.getItems();
		for ( Integer e : aItems){
			if ( bItems.contains(e)){
				commonItemset.addItem(e);
			}
		}
		return commonItemset;
	}
}
