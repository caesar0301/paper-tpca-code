package omnilab.bd.pattern.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.AlgoBIDEPlus;
import omnilab.bd.pattern.spmf.sequence_database.SequenceDatabase;

/*
 * Example of how to use the BIDE+ algorithm, from the source code.
 */
public class MainTestBIDEPlus_saveToFile {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"));
		sequenceDatabase.print();
		
		int minsup = 2; // we use a minsup of 2 sequences (50 % of the database size)
		
		AlgoBIDEPlus algo  = new AlgoBIDEPlus();  //
		
		// execute the algorithm
		algo.runAlgorithm(sequenceDatabase, "C://patterns//closed_sequential_patterns.txt", minsup);    
		algo.printStatistics(sequenceDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestBIDEPlus_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}