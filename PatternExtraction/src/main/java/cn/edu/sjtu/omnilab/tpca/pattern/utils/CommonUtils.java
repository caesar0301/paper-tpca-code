package cn.edu.sjtu.omnilab.tpca.pattern.utils;

public class CommonUtils {
	/**
	 * Translate a similarity value to distance measure.
	 * Given 0 <= sim <= 1, 1/3 <= rst <= 1;
	 * @param sim
	 * @return
	 */
	public static double similarity2Distance(double sim){
		return 1 / (1.0 + 2 * sim);
	}
}
