package omnilab.bd.pattern.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import omnilab.bd.pattern.clustering.DBSCAN;
import omnilab.bd.pattern.clustering.Dataset;
import omnilab.bd.pattern.clustering.DistanceMeasure;
import omnilab.bd.pattern.clustering.Instance;
import omnilab.bd.pattern.clustering.PatternSetDistance;
import omnilab.bd.pattern.core.Pattern;
import omnilab.bd.pattern.core.PatternSet;
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
 * @author gwj
 *
 */
public class SJTUTest {
	//************ SETTINGS START **************
	private static int segment_size = 14;
	private static double minsup_upper = 0.9;
	private static double minsup_lower = 0.2;
	private static boolean debug = false;
	private static int top_pattern_limit = 30;
	
	private static String DEFAULT_USER_FOLDER = "I:/LocationPrediction/SJTUArubaSyslog/SJTUPartitionedLocationData/";
	private static String DEFAULT_PATTERN_OUT_FOLDER = "C:/Users/gwj/Desktop/SJTUPatterns/";
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
        // prepare output folders
        File out_folder = new File(PATTERN_OUT_FOLDER);
        if ( ! out_folder.exists() ) out_folder.mkdirs();
        
		// file manager for input logs
		FileManager fm = new FileManager();
		List<String> files = fm.searchFiles(USER_FOLDER);
		// user manager for user status
		UserManager um = new UserManager();
		int c = 0;
		while ( c < files.size() && c < 36){
			// for each user
			String user_id = files.get(c);
			System.out.println("\nUser " + c + ": "+user_id);
			
			// convert logs into inner database of sequence helper
			SequencesHelper sequencesHelper = new SequencesHelper(USER_FOLDER + user_id, false, null);
//			if (debug) { System.out.println(sequencesHelper);}
			
			// manage user status
			um.setSequencesHelper(user_id, sequencesHelper);
			
			// following processes are all based on the inner database of sequence helper
			// create segment sequence databases with sliding window of specific size
			List<SequenceDatabase> segmentdbs = createSegmentDatabases(sequencesHelper, segment_size);
			if (debug) { 
				System.out.println("segment dbs: " + segmentdbs.size());
			}
			
			if (segmentdbs.size() > 3){
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
					
					if (debug){
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
			}
			
			c++;
		}
		
		//miningPatternChange(um);
		
		patternWrite(um);
		patternClustering(um);
		
		
		// clear user data
		um.clear();
	}
	
	public static void patternClustering(UserManager um) throws IOException{
		System.out.println("Clustering~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		Dataset dataset = new Dataset();
		for ( String uid : um.getAllUsers()) {
			System.out.println("UID: "+uid);
			for ( PatternSet ps : um.getPatternSetList(uid)){
				if(ps.getPatterns().size() == 0){
					System.out.println(ps.getTransIDs().toString() + "\t" + "null ps!");
				}
				else{
					Instance instance = new Instance(ps);
					dataset.add(instance);
				}
			}
		}
		System.out.println(dataset.size());
		DistanceMeasure dm = new PatternSetDistance();
		DBSCAN dbscan = new DBSCAN(0.52, 4, dm);
		Dataset[] clusters = dbscan.cluster(dataset);
		
		PatternSetDistance.distanceWrite();
		PatternSetDistance.distanceDistributionAnalysis();
		PatternSetDistance.kDistanceGraph(2);
		PatternSetDistance.kDistanceGraph(3);
		PatternSetDistance.kDistanceGraph(4);
		
		System.out.println("Size..........."+ "\t"+clusters.length);
		System.out.println("clustering end!!!!!!!!!!!!!!!!!!!!!!");
		for ( Dataset cluster : clusters) {
			System.out.println("cluster size: "+cluster.size());
			for ( int i = 0; i<cluster.size(); i++){
				PatternSet ps  = (PatternSet) cluster.getInstance(i).dataValue();
				//System.out.println(String.format("%s\t%s", ps.getUserID(), ps.getTransIDs()));
				//System.out.println(ps);
			}
			/*
			cluster.sort();
			int[] res = new int[cluster.size()];
			for ( int i = 0; i<cluster.size(); i++){
				res[i] = cluster.getInstance(i).getID();
			}
			*/
		}
	}
	
	public static void patternWrite(UserManager um) throws IOException{
		for ( String uid : um.getAllUsers() ) {
			//Prepare for the output_file
			String output_file_location = DEFAULT_PATTERN_OUT_FOLDER + uid;
			File output_file = new File(output_file_location);
			if(!output_file.exists())
				output_file.createNewFile();			
			//clear the file
			FileOutputStream fos = new FileOutputStream(DEFAULT_PATTERN_OUT_FOLDER + uid);   
			fos.close();
			FileWriter output_filewriter =new FileWriter(output_file, true);
			
			//Pattern get
			List<PatternSet> patternset_list = um.getPatternSetList(uid);
			for(int i = 0; i < patternset_list.size(); i++ ){
				PatternSet patternSet = patternset_list.get(i);
				Set<Integer> window_tids = patternSet.getTransIDs();
				output_filewriter.write(window_tids.toString() + "\n");
				
				List<Pattern> pattern_list = patternSet.getPatterns();
				for(int k = 0; k < pattern_list.size(); k++){
					Pattern pattern = pattern_list.get(k);
					output_filewriter.write(pattern.getPatternItemsetListAsString() + "\t" + pattern.getRelativeSupport() + "\t" + pattern.getAbsoluteSupport() + "\n");
				}
			}
			
			output_filewriter.close();
		}
	}
	
	
	public static void miningPatternChange(UserManager um){
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
		int width_max = 30; // one month
		int width_min = 7; // one week
		
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
