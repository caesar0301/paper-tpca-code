package omnilab.bd.pattern.main;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import omnilab.bd.pattern.ap.APNameHelper;
import omnilab.bd.pattern.core.Pattern;
import omnilab.bd.pattern.core.PatternSet;
import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.AlgoPrefixSpan;
import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.SequentialPattern;
import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.SequentialPatterns;
import omnilab.bd.pattern.spmf.sequence_database.SequenceDatabase;
import omnilab.bd.pattern.utils.FileManager;
import omnilab.bd.pattern.utils.ObjPersistence;
import omnilab.bd.pattern.utils.SequencesHelper;
import omnilab.bd.pattern.utils.UserManager;


public class SerializablePatternExample {
	//************ SETTINGS START **************
	// SLIDING WINDOW
	private static int time_span_max = 7; // days
	private static int time_span_min = 4; // days
	private static int segment_size_max = 10000; // loose this constraint to meet new data format
	private static int segment_size_min = 2;
	private static double sliding_fraction = 0.2;
	// PATTERNS
	private static double minsup_lower = 0.2;
	private static int top_pattern_limit = 10;

	private static String DEFAULT_USER_FOLDER = "/home/chenxm/tera/Datasets/dartmouth/test/";
	//************* SETTINGS END ***************
	
	public static void main(String [] args) throws IOException, ClassNotFoundException{	
        // fetch command line options
		//String USER_FOLDER = DEFAULT_USER_FOLDER;
		
		// file manager for input logs
		//FileManager fm = new FileManager();
		//List<String> files = fm.searchFilesRegex(USER_FOLDER, "\\d+");
		// user manager for user status
		//UserManager um = miningPatterns(files, USER_FOLDER, 10000);
		//System.out.println(um.size());
		
		// Serialize the user manager and apname helper
		//ObjPersistence.toBinary(um, "um.data");
		// Deserialize the user manager and apname helper
		UserManager dserUM = (UserManager) ObjPersistence.fromBinary("C:/Users/gwj/Desktop/um.dat");

		// for test
		System.out.println("User manager recovered.");
		System.out.println(dserUM.size());
		for ( String user : dserUM.getAllUsers() ){
			System.out.println(user);
			for ( PatternSet ps : dserUM.getPatternSetList(user)){
				System.out.println(ps);
			}
		}
	}
	

	private static UserManager miningPatterns(List<String> files, String userFolder, int testLimit) throws IOException{
		// user manager for user status
		UserManager um = new UserManager();
		APNameHelper apNameHelper = new APNameHelper();
		int c = 0;
		while ( c < files.size() && c < testLimit){
			// for each user
			String user_id = files.get(c);
			System.out.println("\nUser " + c + ": "+user_id);
			
			// convert logs into inner database of sequence helper
			SequencesHelper sequencesHelper = new SequencesHelper(userFolder + user_id, false, apNameHelper);
			um.setSequencesHelper(user_id, sequencesHelper);
			System.out.println(sequencesHelper.size());
			List<SequenceDatabase> segmentdbs = 
				sequencesHelper.createSegmentDatabases(segment_size_max, segment_size_min, time_span_max, time_span_min, sliding_fraction);
			System.out.println(segmentdbs.size());
			if (segmentdbs.size() >= 2){
				for (int i = 0; i< segmentdbs.size(); i++){	
					SequenceDatabase segdb = segmentdbs.get(i);
					double minsup_step = 1.0;
					PatternSet pattern_set = genPatternSetFromDatabase(segdb, minsup_step, user_id);
					while ( pattern_set != null && pattern_set.size() < top_pattern_limit && minsup_step > minsup_lower){
						minsup_step -= 0.05;
						if ( minsup_step >= minsup_lower)
							pattern_set = genPatternSetFromDatabase(segdb, minsup_step, user_id);
						else
							break;
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
	 * Several constraints are considered during pattern mining:
	 * 1. exclude 1-length pattern. (implemented in this func.)
	 * 2. exclude patterns with duplicate itemsets between adj ones, e.g, (a)(a)(b).
	 * (implemented in prefixspan only)
	 * 3. mining closed patterns. (implemented in PatternSet constructor and BIDE+.)
	 * @param db The sequence database.
	 * @return A set of patterns.
	 * @throws IOException
	 */
	private static PatternSet genPatternSetFromDatabase(SequenceDatabase db, double minsup, String user_id) throws IOException{
		// Prefixspan
		AlgoPrefixSpan algo = new AlgoPrefixSpan();
		algo.setMaximumPatternLength(10);
		SequentialPatterns raw_patterns = algo.runAlgorithm(db, minsup, null, true);
				
		int levelCount=0;
		// translate raw patterns into my pattern set structure
		PatternSet pattern_set = new PatternSet(user_id, db.getSequenceIDs(), true, false);
		for(List<SequentialPattern> level : raw_patterns.getLevels()){
			for(SequentialPattern raw_pattern : level){
				// FILTER OUT PATTERN WITH LENGTH ONE
//				if ( raw_pattern.size() == 1 ) {continue;}
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