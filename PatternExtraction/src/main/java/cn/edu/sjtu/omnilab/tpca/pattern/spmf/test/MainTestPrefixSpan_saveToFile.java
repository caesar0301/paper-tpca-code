package cn.edu.sjtu.omnilab.tpca.pattern.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import cn.edu.sjtu.omnilab.tpca.pattern.spmf.BIDE_and_prefixspan.AlgoPrefixSpan;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.sequence_database.SequenceDatabase;


/**
 * Example of how to use the PrefixSpan algorithm in source code.
 * @author Philippe Fournier-Viger
 */
public class MainTestPrefixSpan_saveToFile {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase();
		sequenceDatabase.loadFile(fileToPath("seq.txt"));
		// print the database to console
		sequenceDatabase.print();
		
		// Create an instance of the algorithm with minsup = 50 %
		AlgoPrefixSpan algo = new AlgoPrefixSpan();
		algo.setMaximumPatternLength(3);
		
		int minsup = (int)(500 * 0.2); // we use a minimum support of 2 sequences.
		
		// execute the algorithm
		algo.runAlgorithm(sequenceDatabase, "/home/chenxm/Desktop/sequential_patterns_2.txt", minsup);    
		algo.printStatistics(sequenceDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPrefixSpan_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}