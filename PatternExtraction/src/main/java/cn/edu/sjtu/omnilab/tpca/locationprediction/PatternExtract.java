package cn.edu.sjtu.omnilab.tpca.locationprediction;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import cn.edu.sjtu.omnilab.tpca.pattern.core.Pattern;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.BIDE_and_prefixspan.AlgoPrefixSpan;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.BIDE_and_prefixspan.SequentialPattern;
import cn.edu.sjtu.omnilab.tpca.pattern.ap.APNameHelper;
import cn.edu.sjtu.omnilab.tpca.pattern.core.PatternSet;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.BIDE_and_prefixspan.SequentialPatterns;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.sequence_database.SequenceDatabase;
import cn.edu.sjtu.omnilab.tpca.pattern.utils.FileManager;
import cn.edu.sjtu.omnilab.tpca.pattern.utils.SequencesHelper;
import cn.edu.sjtu.omnilab.tpca.pattern.utils.UserManager;


public class PatternExtract {
	//************ SETTINGS START **************
	// SLIDING WINDOW
	private static int time_span_max = 7; // days
	private static int time_span_min = 4; // days
	private static int segment_size_max = 10000; // loose this constraint to meet new data format
	private static int segment_size_min = 10;
	private static double sliding_fraction = 0.2;
	// PATTERNS
	private static double minsup_lower = 0.2;
	private static int top_pattern_limit = 10;
	private static int max_pattern_length= 6;
	//************* SETTINGS END ***************
	
	public static UserManager patternExtract(String INPUT_USER_FOLDER) throws IOException{
        // fetch command line options
		String USER_FOLDER = INPUT_USER_FOLDER;      
		// file manager for input logs
		FileManager fm = new FileManager();
		List<String> files = fm.searchFiles(USER_FOLDER);
		// user manager for user status
		UserManager um = new UserManager();
		APNameHelper apNameHelper = new APNameHelper();
		
		int c = 0;
		while ( c < files.size()){
			// for each user
			String user_id = files.get(c);
			System.out.println("\nUser " + c + ": "+user_id);
			// convert logs into inner database of sequence helper
			SequencesHelper sequencesHelper = new SequencesHelper(USER_FOLDER + user_id, true, apNameHelper);
			um.setSequencesHelper(user_id, sequencesHelper);
			List<SequenceDatabase> segmentdbs = sequencesHelper.
					createSegmentDatabases(segment_size_max, segment_size_min, time_span_max, time_span_min, sliding_fraction);		
			if (segmentdbs.size() >= 10){
				for (int i = 0; i< segmentdbs.size(); i++){	
					SequenceDatabase segdb = segmentdbs.get(i);
					double minsup_step = 1.0;
					PatternSet pattern_set = genPatternSetFromDatabase(segdb, minsup_step, user_id);
					while ( pattern_set != null && pattern_set.size() < top_pattern_limit && minsup_step > minsup_lower){
						minsup_step -= 0.05;
						pattern_set = genPatternSetFromDatabase(segdb, minsup_step, user_id);
					}
					if ( pattern_set == null )  continue;
					pattern_set = pattern_set.cloneTopPatterns(top_pattern_limit);			
					// update user status
					um.addPatternSet(user_id, pattern_set);
				}
			} 
			c++;
		}
		return um;
	}
	
	
	/**
	 * Generate patterns from a sequence database.
	 * @param db The sequence database.
	 * @return A set of patterns.
	 * @throws IOException
	 */
	private static PatternSet genPatternSetFromDatabase(SequenceDatabase db, double minsup, String user_id) throws IOException{
		// for BIDE
//		AlgoBIDEPlus algo  =  new AlgoBIDEPlus();
//		SequentialPatterns raw_patterns = algo.runAlgorithm(db, null, (int)Math.floor(minsup*db.size()));

		// Prefixspan
		AlgoPrefixSpan algo = new AlgoPrefixSpan();
		algo.setMaximumPatternLength(max_pattern_length);
		SequentialPatterns raw_patterns = algo.runAlgorithm(db, minsup, null, true);
		
		// for SPAM
//		AlgoSPAM algo = new AlgoSPAM();
//		algo.setMaximumPatternLength(10);
//		SequentialPatterns raw_patterns = algo.runAlgorithm(db, null, minsup);
				
		int levelCount=0;
		// translate raw patterns into my pattern set structure
		PatternSet pattern_set = new PatternSet(user_id, db.getSequenceIDs(), true, false);
		for(List<SequentialPattern> level : raw_patterns.getLevels()){
			for(SequentialPattern raw_pattern : level){
				// FILTER OUT PATTERN WITH LENGTH ONE
				//if ( raw_pattern.size() == 1 ) {continue;}
				// Format each sequential pattern into my pattern class
				// It's convenient to be processed as followed
				Pattern new_pattern = new Pattern(levelCount);
				new_pattern.setPatternItemsetList(raw_pattern.getItemsets());
				new_pattern.setTransactionIDs(new LinkedList<Integer>(raw_pattern.getSequencesID()));
				int base_size = db.size();
				new_pattern.setAbsoluteSupport(raw_pattern.getAbsoluteSupport());
				new_pattern.setRelativeSupport(new Double(raw_pattern.getRelativeSupportFormated(base_size)), base_size);
				pattern_set.addPattern(new_pattern);
			}
			levelCount++;
		}
		return pattern_set;
	}
	
	
}
