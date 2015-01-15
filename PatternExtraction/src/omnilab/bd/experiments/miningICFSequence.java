package omnilab.bd.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import omnilab.bd.pattern.ap.APNameHelper;
import omnilab.bd.pattern.core.Pattern;
import omnilab.bd.pattern.core.PatternSet;
import omnilab.bd.pattern.core.PatternSetChange;
import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.AlgoPrefixSpan;
import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.SequentialPattern;
import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.SequentialPatterns;
import omnilab.bd.pattern.spmf.sequence_database.SequenceDatabase;
import omnilab.bd.pattern.utils.FileManager;
import omnilab.bd.pattern.utils.SequencesHelper;
import omnilab.bd.pattern.utils.UserManager;


/**
 * Mining the Informational Change Function (ICF) measure for each individual window (pattern set).
 * Ref: W. Gong, Trajectory Pattern Change Analysis in Campus WiFi Networks, MobiGIS'2013
 *
 */
public class miningICFSequence {
	//************ SETTINGS START **************
	private static boolean debug = true;
	private static boolean verbose = true;
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
	private static double pattern_graph_minsim = 0.15;

	private static String DEFAULT_USER_FOLDER = "/Users/chenxm/Jamin/Datasets/SJTU/Location/";
	private static String DEFAULT_CHNAGE_DATA = "/Users/chenxm/Jamin/Datasets/SJTU/changedata.txt";
	//************* SETTINGS END ***************
	
	public static void main(String [] args) throws IOException{	
        // fetch command line options
		String USER_FOLDER = DEFAULT_USER_FOLDER;
		
		// file manager for input logs
		FileManager fm = new FileManager();
		List<String> files = fm.searchFilesRegex(USER_FOLDER, "\\d+");
		
		// user manager for user status
		UserManager um = miningPatterns(files, USER_FOLDER, 10000);
		System.out.println(um.size());
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
			SequencesHelper sequencesHelper = new SequencesHelper(userFolder + user_id, true, apNameHelper);
			um.setSequencesHelper(user_id, sequencesHelper);
			
			List<SequenceDatabase> segmentdbs = sequencesHelper.createSegmentDatabases(segment_size_max, segment_size_min, time_span_max, time_span_min, sliding_fraction);
			if (debug) { 
				System.out.println("segment dbs: " + segmentdbs.size());
			}
						
			if (segmentdbs.size() >= 10){
				for (int i = 0; i< segmentdbs.size(); i++){	
					SequenceDatabase segdb = segmentdbs.get(i);
					double minsup_step = 1.0;
					PatternSet pattern_set = genPatternSetFromDatabase(segdb, minsup_step, user_id);
					while ( pattern_set != null && pattern_set.size() < top_pattern_limit && 
							minsup_step > minsup_lower){
						minsup_step -= 0.05;
						pattern_set = genPatternSetFromDatabase(segdb, minsup_step, user_id);
					}

					if ( pattern_set == null )  continue;
					pattern_set = pattern_set.cloneTopPatterns(top_pattern_limit);
					
					if (debug && verbose){
						System.out.println(String.format("\n****** segment db #%d/%d *******", i, segmentdbs.size()));
						segdb.printDatabaseStats();
						System.out.println(String.format("------ Patterns (TOP %d) -------", top_pattern_limit ));
						System.out.println(String.format("final minsup: %.3f", minsup_step));
						for ( Pattern p : pattern_set.getPatterns())
							System.out.println(p.toString());
					}
					
					// update user status
					um.addPatternSet(user_id, pattern_set);
				}
			} 
//			else {
//				File file = new File(userFolder + user_id);
//				file.delete();
//			}
			
			miningPatternChange(um, DEFAULT_CHNAGE_DATA);
			
			// clear user data
			um.clear();
			
			c++;
		}
		
		return um;
	}
	
	
	private static void miningPatternChange(UserManager um, String outputfile) throws IOException{
		File ofile = new File(outputfile);
		ofile.getParentFile().mkdirs();
		// Appended mode.
		BufferedWriter bw = new BufferedWriter(new FileWriter(ofile, true));
					
		for ( String uid : um.getAllUsers()) {
			System.out.println("UID: "+uid);
			List<PatternSet> patternSets = um.getPatternSetList(uid);
			if ( patternSets.size() == 0 )
				continue;
			
			SequencesHelper helper = um.getSequencesHelper(uid);
			StringBuffer r = new StringBuffer();
			StringBuffer or = new StringBuffer();
			r.append("ps_index\tchange_sensitivity\n");
			for ( int i = 1; i < patternSets.size(); i++){
				PatternSet ps = patternSets.get(i);
				if ( ps.size() > 0){
					r.append(i+"\t");
					double cinfoTotal = 0.0;
					double weightTotal = 0.0;
					for ( int j = i-5; j < i; j++){
						// look back for 5 windows to calculate the weighted average change
						if ( j >= 0 && patternSets.get(j).size() > 0 ){
							double cinfo = PatternSetChange.changeInformationMeasure(patternSets.get(j), 
									patternSets.get(i), pattern_graph_minsim);
							double tspan = PatternSetChange.calTimeSpanDays(patternSets.get(j), 
									patternSets.get(i), helper);
							cinfoTotal += 1.0 / tspan * cinfo;
							weightTotal += 1.0 / tspan;
						}
					}
					// also output pattern set interval
					Date startTime = ps.getDateInterval(helper, false);
					Date endTime = ps.getDateInterval(helper, true);
					if (weightTotal != 0){
						r.append(String.format("%.3f", cinfoTotal / weightTotal));
						or.append(String.format("%d,%d,%.3f ", startTime.getTime()/1000,
								endTime.getTime()/1000, cinfoTotal/weightTotal));
					} else {
						r.append("*/0");
						or.append("/0 ");
					}
					r.append("\n");
				}
			}
			
			System.out.println(r.toString());
			
			// dump statistics
			if ( or.length() > 0 )
				bw.write(uid + " "+or.toString()+"\n");
		}

		bw.close();
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
		// for BIDE
//		AlgoBIDEPlus algo  =  new AlgoBIDEPlus();
//		SequentialPatterns raw_patterns = algo.runAlgorithm(db, null, (int)Math.floor(minsup*db.size()));

		// Prefixspan
		AlgoPrefixSpan algo = new AlgoPrefixSpan();
		algo.setMaximumPatternLength(max_pattern_length);
		SequentialPatterns raw_patterns = algo.runAlgorithm(db, minsup, null, true);
		
		// for SPAM
//		AlgoSPAM algo = new AlgoSPAM();
//		algo.setMaximumPatternLength(max_pattern_length);
//		SequentialPatterns raw_patterns = algo.runAlgorithm(db, null, minsup);
				
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