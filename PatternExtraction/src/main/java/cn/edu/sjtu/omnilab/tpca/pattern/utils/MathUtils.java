package cn.edu.sjtu.omnilab.tpca.pattern.utils;


public class MathUtils {
	
	/**
	 *
	 * @param a
	 * @return
	 */
	public static double log2(double a) {
		return Math.log(a) / Math.log(2);
	}
	
	/**
	  * Returns the KL divergence, K(p1 || p2).
	  * Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
	  * 
	  * The log is w.r.t. base 2. <p>
	  *
	  * *Note*: If any value in <tt>p2</tt> is <tt>0.0</tt> then the KL-divergence
	  * is <tt>infinite</tt>. Limin changes it to zero instead of infinite. 
	  * 
	  */
	public static double klDivergence(double[] p1, double[] p2) {
		double klDiv = 0.0;
		for (int i = 0; i < p1.length; ++i) {
			if (p1[i] == 0) { continue; }
			if (p2[i] == 0.0) { continue; } // Limin
			klDiv += p1[i] * log2( p1[i] / p2[i] );
		}
		
		return klDiv; // moved this division out of the loop -DM
	}
	
	
	/**
	 * Calclulate the Shannon Entropy of a distribution.
	 * @param p The probabilities of each value in the distribution.
	 * @return
	 */
	public static double shannonEntropy(double[] p){
		double entropy = 0.0;
		for ( double frequency : p){
			if ( frequency != 0 )
				entropy -= frequency * log2(frequency);
		}
		return entropy;
	}
	
	
	public static void main(String[] args){
		double[] p = {0.5, 0.5};
		System.out.println("ShannonEntropy: "+ shannonEntropy(p));
	}
}