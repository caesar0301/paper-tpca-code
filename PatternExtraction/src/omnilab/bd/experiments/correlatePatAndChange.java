package omnilab.bd.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import omnilab.bd.pattern.ap.APNameHelper;
import omnilab.bd.pattern.core.Pattern;
import omnilab.bd.pattern.core.PatternSet;
import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.AlgoPrefixSpan;
import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.SequentialPattern;
import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.SequentialPatterns;
import omnilab.bd.pattern.spmf.sequence_database.SequenceDatabase;
import omnilab.bd.pattern.utils.FileManager;
import omnilab.bd.pattern.utils.SequencesHelper;
import omnilab.bd.pattern.utils.UserManager;


/**
 * Correlate the Pattern content and pattern changes.
 * Work with the output of Bayesian analysis "changedata-points.txt".
 */
public class correlatePatAndChange {
	//************ SETTINGS START **************
	private static boolean debug = true;
	private static boolean verbose = false;
	
	private static int time_span_max = 30; // days
	private static int time_span_min = 8; // days
	private static int segment_size = 15;
	private static double minsup_upper = 0.9;
	private static double minsup_lower = 0.2;
	private static int top_pattern_limit = 10;
	private static int max_pattern_length= 6;
	private static double sliding_fraction = 0.3;
	
	private static String DEFAULT_USER_FOLDER = "/Users/chenxm/Jamin/Datasets/dartmouth/Location/";
	//************* SETTINGS END ***************
	
	public static void main(String [] args) throws IOException{	
        // fetch command line options
		String USER_FOLDER = DEFAULT_USER_FOLDER;
        int optSetting = 0;
        for (; optSetting < args.length; optSetting++) {  
            if ("-i".equals(args[optSetting])) {  
            	USER_FOLDER = args[++optSetting];  
            }
        }
        
		// file manager for input logs
		FileManager fm = new FileManager();
		List<String> files = fm.searchFilesRegex(USER_FOLDER, "\\d+");
		// user manager for user status
		UserManager um = miningPatterns(files, USER_FOLDER, 350);
		
		System.out.println("Mining correlation...");
		miningCorrelation(um, 
				"/Users/chenxm/Desktop/changedata-points.txt",
				"/Users/chenxm/Desktop/corPatCh.txt",
				"/Users/chenxm/Desktop/corRoiChTs.txt");
		
		// clear user data
		um.clear();
	}
	
	
	private static void miningCorrelation(UserManager um, String inputfile, String pcOfile, String roiOfile) throws NumberFormatException, IOException{
		// mining correlation
		// read the change point data and store them in a map structure
		Map<String, List<Integer>> user_cpoints_map = new HashMap<String,List<Integer>>();
		File cpfile = new File(inputfile);
		BufferedReader br = new BufferedReader(new FileReader(cpfile));
		String sep = " ";
		String thisLine = "";
		while ((thisLine = br.readLine()) != null) {
			thisLine = thisLine.replaceAll("[\r\n]", "");
			if ( thisLine.length() == 0) continue;
			String[] lineParts = thisLine.split(sep);
			String uid = lineParts[0];
			List<Integer> points = new ArrayList<Integer>();
			for ( int i = 1; i<lineParts.length; i++){
				int val = Integer.parseInt(lineParts[i]);
				// remove continuous points? 
				points.add(val);
			}
			// store a record of user
			user_cpoints_map.put(uid, points);
		}
		br.close();
		
		// prepare output file stream
		File pointsFile = new File(pcOfile);
		FileWriter pointsFW = new FileWriter(pointsFile, true);
		File freFile = new File(roiOfile);
		FileWriter freFW = new FileWriter(freFile, true);
		
		for ( String uid : user_cpoints_map.keySet() ){
			if ( user_cpoints_map.get(uid).size() < 2 ) 
				continue;
			SequencesHelper helper = um.getSequencesHelper(uid);
			if( helper == null)
				continue;
			List<PatternSet> patternSetList = um.getPatternSetList(uid);
			List<Integer> cpointsIntegers = user_cpoints_map.get(uid);
			int start = -1;
			int end = -1;
			for ( Integer point : cpointsIntegers){
				if ( start == -1){
					start  = point;
					continue;
				}
				end = point;
				// calculate average pattern count
				double ave_psc = 0.0;
				int total_ps = 0;
				for ( int j = start; j <= end; j++){
					total_ps += patternSetList.get(j).size();
				}
				ave_psc = 1.0*total_ps / (end-start+1);
				// calculate time span, i.e., frequency
				PatternSet startPS = patternSetList.get(start);
				PatternSet endPS = patternSetList.get(end);
				double startTime = 0.5* ( 
						startPS.getDateInterval(helper, false).getTime()/1000 +
						startPS.getDateInterval(helper, true).getTime()/1000 );
				double endTime = 0.5* ( 
						endPS.getDateInterval(helper, false).getTime()/1000 +
						endPS.getDateInterval(helper, true).getTime()/1000 );
				double spanDays = (endTime - startTime) / 3600 / 24;
				// write pattern change count and frequency
				pointsFW.write(String.format("%.3f,%.3f\n", ave_psc, spanDays));
				// write the frequency represented by span days for ROI mining
				freFW.write(String.format("%s %.3f %.3f\n", uid, startTime, endTime));
				// reset a new start
				start = end;
			}
		}
		pointsFW.close();
		freFW.close();
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
			List<SequenceDatabase> segmentdbs = sequencesHelper.createSegmentDatabases(segment_size, segment_size/2, time_span_max, time_span_min, sliding_fraction);
			if (debug) { 
				System.out.println("segment dbs: " + segmentdbs.size());
			}
			
			if (segmentdbs.size() >= 5){
				for (int i = 0; i< segmentdbs.size(); i++){	
					SequenceDatabase segdb = segmentdbs.get(i);
					double minsup_step = minsup_upper;
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