package omnilab.bd.pattern.main;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import omnilab.bd.pattern.ap.APNameHelper;
import omnilab.bd.pattern.clustering.DBSCAN;
import omnilab.bd.pattern.clustering.Dataset;
import omnilab.bd.pattern.clustering.DistanceMeasure;
import omnilab.bd.pattern.clustering.Instance;
import omnilab.bd.pattern.clustering.PatternSetDistance;
import omnilab.bd.pattern.core.Pattern;
import omnilab.bd.pattern.core.PatternSet;
import omnilab.bd.pattern.core.PatternSetChange;
import omnilab.bd.pattern.graph.PatternGraph;
import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.AlgoPrefixSpan;
import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.SequentialPattern;
import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.SequentialPatterns;
import omnilab.bd.pattern.spmf.sequence_database.Sequence;
import omnilab.bd.pattern.spmf.sequence_database.SequenceDatabase;
import omnilab.bd.pattern.utils.FileManager;
import omnilab.bd.pattern.utils.SequencesHelper;
import omnilab.bd.pattern.utils.UserManager;


/**
 * java omnilab.bd.pattern.main.MainSequentialPatternMining -i <inputfolder> -o <outputfolder>
 * 
 * @author chenxm
 *
 */
@Deprecated
public class MainSequentialPatternMining {
	//************ SETTINGS START **************
	private static boolean debug = true;
	private static boolean verbose = false;
	
	private static int time_span_max = 20; // days
	private static int time_span_min = 5; // days
	private static int segment_size = 10;
	private static double minsup_upper = 0.9;
	private static double minsup_lower = 0.2;
	private static int top_pattern_limit = 20;
	private static double pattern_graph_minsim = 0.15;
	
	private static String DEFAULT_USER_FOLDER = "/Users/chenxm/Jamin/Datasets/dartmouth/Location/";
	private static String DEFAULT_PATTERN_OUT_FOLDER = "/Users/chenxm/Desktop/patterns/";
	
	private static Logger logger = Logger.getLogger("MyLogger");
	//************* SETTINGS END ***************
	
	public static void main(String [] args) throws IOException{	
        // fetch command line options
		String USER_FOLDER = DEFAULT_USER_FOLDER;
		String PATTERN_OUT_FOLDER = DEFAULT_PATTERN_OUT_FOLDER;
        int optSetting = 0;
        for (; optSetting < args.length; optSetting++) {  
            if ("-i".equals(args[optSetting])) {  
            	USER_FOLDER = args[++optSetting];  
            } else if ("-o".equals(args[optSetting])) {  
            	PATTERN_OUT_FOLDER = args[++optSetting];
            }
        }
        
        // configure logger
		FileHandler fh = new FileHandler("running.log");
		fh.setFormatter(new SimpleFormatter());
		logger.addHandler(fh);
        
        // prepare output folders
        File out_folder = new File(PATTERN_OUT_FOLDER);
        if ( ! out_folder.exists() ) out_folder.mkdirs();
        
		// file manager for input logs
		FileManager fm = new FileManager();
		List<String> files = fm.searchFiles(USER_FOLDER);
		// user manager for user status
		UserManager um = new UserManager();
		APNameHelper apNameHelper = new APNameHelper();
		
		int c = 0;
		while ( c < files.size() && c<100){
			// for each user
			String user_id = files.get(c);
			System.out.println("\nUser " + c + ": "+user_id);
			
			// convert logs into inner database of sequence helper
			SequencesHelper sequencesHelper = new SequencesHelper(USER_FOLDER + user_id, false, apNameHelper);
			if (debug) { 
				System.out.println(sequencesHelper);
			}
			
			// manage user status
			um.setSequencesHelper(user_id, sequencesHelper);
			
			// following processes are all based on the inner database of sequence helper
			// create segment sequence databases with sliding window of specific size
			List<SequenceDatabase> segmentdbs = createSegmentDatabases(sequencesHelper, segment_size);
			if (debug) { 
				System.out.println("segment dbs: " + segmentdbs.size());
			}
			
			if (segmentdbs.size() > 2){
				for (int i = 0; i< segmentdbs.size(); i++){	
					SequenceDatabase segdb = segmentdbs.get(i);
					// for each segment database, extract patterns stored in a pattern set.
					// mine top-N patterns
					double minsup_step = minsup_upper;
					PatternSet pattern_set = genPatternSetFromDatabase(segdb, minsup_step, user_id);
					while ( pattern_set != null && pattern_set.size() < top_pattern_limit && 
							minsup_step > minsup_lower){
						minsup_step -= 0.05;
						pattern_set = genPatternSetFromDatabase(segdb, minsup_step, user_id);
					}

					if ( pattern_set == null ) 
						continue;
					
					if (debug && verbose){
						System.out.println(String.format("\n****** segment db #%d/%d *******", i, segmentdbs.size()));
						//segdb.print();
						segdb.printDatabaseStats();
						System.out.println(String.format("------ Patterns (TOP %d) -------", top_pattern_limit ));
						System.out.println(String.format("final minsup: %.3f", minsup_step));
						for ( Pattern p : pattern_set.getPatterns())
							System.out.println(p.toString());
					}
					
					// update user status
					um.addPatternSet(user_id, pattern_set);
				}
			
				// get all generated pattern sets
				List<PatternSet> patternSetList = um.getPatternSetList(user_id);
				
				// MINING PATTERNS SETS USING THE MODEL OF PATTERN GRAPH
				// create a graph for all patterns
				PatternGraph graph = new PatternGraph(patternSetList, pattern_graph_minsim);
				graph.classifyingPatterns();
				if (debug ) { System.out.println(graph); }
				
				
			}
			
			miningPatternChangeByClustering(um);
//			miningPatternChange(um);
			
			// clear user data
			um.clear();

			c++;
		}
	}
	
	
	public static void miningPatternChange(UserManager um){
		for ( String uid : um.getAllUsers()) {
			System.out.println("UID: "+uid);
			List<PatternSet> patternSets = um.getPatternSetList(uid);
			if ( patternSets.size() == 0 )
				return;
			
			SequencesHelper helper = um.getSequencesHelper(uid);
			StringBuffer r = new StringBuffer();
			r.append("ps_index\tchange_sensitivity\n");
			for ( int i = 1; i < patternSets.size(); i++){
				if ( patternSets.get(i).size() > 0){
					r.append(i+"\t");
					double cinfoTotal = 0.0;
					double weightTotal = 0.0;
					for ( int j = 0; j < i; j++){
						if ( patternSets.get(j).size() > 0 ){
							double cinfo = PatternSetChange.changeInformationMeasure(patternSets.get(j), 
									patternSets.get(i), pattern_graph_minsim);
							double tspan = PatternSetChange.calTimeSpanDays(patternSets.get(j), 
									patternSets.get(i), helper);
							cinfoTotal += 1.0 / tspan * cinfo;
							weightTotal += 1.0 / tspan;
						}
					}
					if (weightTotal != 0)
						r.append(String.format("---> %.4f", cinfoTotal / weightTotal));
					else {
						r.append("---> */0");
					}
					r.append("\n");
				}
			}
			System.out.println(r.toString());
		}
	}
	
	
	public static void miningPatternChangeByClustering(UserManager um){
		for ( String uid : um.getAllUsers()) {
			System.out.println("UID: "+uid);
			Dataset dataset = new Dataset();
			for ( PatternSet ps : um.getPatternSetList(uid)){
				//System.out.println(ps);
				Instance instance = new Instance(ps);
				dataset.add(instance);
			}
			DistanceMeasure dm = new PatternSetDistance();
			DBSCAN dbscan = new DBSCAN(0.75, 3, dm);
			Dataset[] clusters = dbscan.cluster(dataset);
			for ( Dataset cluster : clusters) {
				cluster.sort();
				int[] res = new int[cluster.size()];
				for ( int i = 0; i<cluster.size(); i++){
					res[i] = cluster.getInstance(i).getID();
				}
			}
		}
	}
	
	
	/**
	 * Generate segments from inner database.
	 * @param sequencesHelper The helper to manage inner database.
	 * @param win_size The sliding window size to generate a segment 
	 * @return A list of segments
	 */
	private static List<SequenceDatabase> createSegmentDatabases(SequencesHelper sequencesHelper, int win_size){
		List<Date>  dates = sequencesHelper.getDates();
		List<Sequence> sequences = sequencesHelper.getSequences();
		List<SequenceDatabase> segmentdbs = new LinkedList<SequenceDatabase>();
		
		int volumn_max = win_size; // Maximum number of intervals with trajectories
		int volumn_min = win_size / 2;	// minimum number of intervals with trajectories
		int width_max = time_span_max;
		int width_min = time_span_min;
		
		assert(dates.size() == sequences.size());
		
		Date date_max = dates.get(dates.size()-1);
		int date_start = 0;
		int date_end = 0;
		
		while (date_max.getTime()/1000 - dates.get(date_start).getTime()/1000 >= width_min * 24 * 3600){
			// start a new segment database
			SequenceDatabase newdb = new SequenceDatabase();
			
			for (int i=date_start; i < dates.size(); i++){
				if ( newdb.size() < volumn_max && 
					(dates.get(i).getTime()/1000 - dates.get(date_start).getTime()/1000) < width_max * 24 * 3600){
					newdb.addSequence(sequencesHelper.getSequence(i), dates.get(i));
					date_end = i;
				}else{
					break; // for
				}
			}
			
			if ( newdb.size() >= volumn_min && newdb.getTimeSpanDays() >= width_min){
				// only add the database meeting the constraints.
				segmentdbs.add(newdb);
			}
			
			// update date start index
			if ( newdb.size() > 1){
				List<Date> newdb_dates = newdb.getDates();
				Date start = newdb_dates.get(0);
				Date end = newdb_dates.get(newdb.size()-1);
				// the sliding distance depends on the median time of the sliding window.
				double time_middle = 0.5*(end.getTime()/1000 + start.getTime()/1000 );
				for (int j = date_start; j <= date_end; j++){
					if (dates.get(j).getTime()/1000 >= time_middle){
						date_start = j;
						date_end = j;
						break; // inner for
					}
				}
			} else
				date_start = date_start+1;
		}

		return segmentdbs;
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
		algo.setMaximumPatternLength(10);
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