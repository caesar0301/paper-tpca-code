package cn.edu.sjtu.omnilab.tpca.pattern.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import cn.edu.sjtu.omnilab.tpca.pattern.spmf.BIDE_and_prefixspan.AlgoBIDEPlus;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.BIDE_and_prefixspan.SequentialPatterns;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.sequence_database.SequenceDatabase;


/**
 * Example of how to use the BIDE+ algorithm, from the source code.
 * 
 * @author Philippe Fournier-Viger
 */
public class MainTestBIDEPlus_saveToMemory {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"));
		sequenceDatabase.print();
		// Create an instance of the algorithm
		AlgoBIDEPlus algo  = new AlgoBIDEPlus();
		
		// execute the algorithm
		SequentialPatterns patterns = algo.runAlgorithm(sequenceDatabase, null, 2);    
		algo.printStatistics(sequenceDatabase.size());
		patterns.printFrequentPatterns(sequenceDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestBIDEPlus_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}