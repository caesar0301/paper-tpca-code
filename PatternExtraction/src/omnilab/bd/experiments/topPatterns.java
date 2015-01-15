package omnilab.bd.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import omnilab.bd.pattern.ap.APNameHelper;
import omnilab.bd.pattern.core.Pattern;
import omnilab.bd.pattern.core.PatternSet;
import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.AlgoPrefixSpan;
import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.SequentialPattern;
import omnilab.bd.pattern.spmf.BIDE_and_prefixspan.SequentialPatterns;
import omnilab.bd.pattern.spmf.itemset.Itemset;
import omnilab.bd.pattern.spmf.sequence_database.Sequence;
import omnilab.bd.pattern.spmf.sequence_database.SequenceDatabase;
import omnilab.bd.pattern.utils.SequencesHelper;


class GlobalPattern{

	private List<Itemset> itemsets = null;
	
	public GlobalPattern(List<Itemset> itemsets){
		this.itemsets = itemsets;
	}
	
	public List<Itemset> getItemsets(){
		return this.itemsets;
	}
	
	@Override
    public int hashCode() {
		String itemstr = "";
		for ( Itemset itemset : this.itemsets ) {
			for ( Integer item : itemset.getItems() ){
				itemstr += String.valueOf(item);
			}
		}
		return itemstr.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final GlobalPattern other = (GlobalPattern) obj;
        List<Itemset> thisItemsetList = this.getItemsets();
        List<Itemset> otherItemsetList = other.getItemsets();
        if ( thisItemsetList.size() != otherItemsetList.size())
        	return false;
        boolean flag = true;
        for ( int i=0; i<thisItemsetList.size(); i++){
        	if ( !thisItemsetList.get(i).containsAll(otherItemsetList.get(i)) ||
        			!otherItemsetList.get(i).containsAll(thisItemsetList.get(i)) )
        		flag = false;
        }
        return flag;
    }
}



/**
 * Mining the top patterns associated with user behavior change.
 * 
 * Please ref:
 * W. Gong, Trajectory Pattern Change Analysis in Campus WiFi Networks, MobiGIS'2013
 *
 */
public class topPatterns {
	private static String DEFAULT_USER_FOLDER = "/Users/chenxm/Jamin/Datasets/SJTU/Location/";
	private static String changePoints = "/Users/chenxm/Jamin/Datasets/SJTU/changedata-points.txt";
	
	public static void main(String[] args) throws ParseException, IOException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = sdf.parse("2013-06-10");
		Date endDate = sdf.parse("2013-06-20");
		
		Map<GlobalPattern, Integer> patternCountMap = new HashMap<GlobalPattern, Integer>();
		APNameHelper apNameHelper = new APNameHelper(); // global apname helper
		Set<String> users =  statChangePoints(changePoints).keySet();
		
		for ( String user : users){
			System.out.println(user);
			
			SequencesHelper helper = new SequencesHelper(DEFAULT_USER_FOLDER+user, true, apNameHelper);
			List<Sequence> sequences = helper.getSequences();
			List<Date> dates =  helper.getDates();
			
			SequenceDatabase newdb = new SequenceDatabase();
			for(int i = 0; i<dates.size(); i++){
				long ttmp = dates.get(i).getTime()/1000;
				if ( ttmp >= startDate.getTime()/1000 && ttmp <= endDate.getTime()/1000 ){
					newdb.addSequence(sequences.get(i), dates.get(i));
				}
			}
			
			double minsup_step = 1.0;
			PatternSet pattern_set = genPatternSetFromDatabase(newdb, minsup_step, user, 6);
			while ( pattern_set != null && pattern_set.size() < 10 && 
					minsup_step > 0.2){
				minsup_step -= 0.05;
				pattern_set = genPatternSetFromDatabase(newdb, minsup_step, user, 6);
			}

			if ( pattern_set == null )  continue;
			
			pattern_set = pattern_set.cloneTopPatterns(10);
			//System.out.println(pattern_set);
			
			for ( Pattern p : pattern_set.getPatterns() ){
				GlobalPattern gp  = new GlobalPattern(p.getPatternItemsetList());
				if ( !patternCountMap.containsKey(gp) ){
					patternCountMap.put(gp, 1);
				} else {
					patternCountMap.put(gp, patternCountMap.get(gp)+1);
				}
			}
		}
		
		System.out.println(String.format("Total pattern: %d", patternCountMap.size()));
		
		List<Entry<GlobalPattern, Integer>> es = new LinkedList<Entry<GlobalPattern, Integer>>();
		es.addAll(patternCountMap.entrySet());
		Collections.sort(es, new Comparator<Entry<GlobalPattern, Integer>>() {
			@Override
			public int compare(Entry<GlobalPattern, Integer> e1, Entry<GlobalPattern, Integer> e2){
				return (e1.getValue().compareTo(e2.getValue()));
			}
		});
		
		int topN = 50;
		for(int i = es.size()-1; i >= es.size()-topN; i--){
			Entry<GlobalPattern, Integer> entry = es.get(i);
			StringBuffer sb = new StringBuffer();
			for ( Itemset itemset : entry.getKey().getItemsets() ){
				sb.append("(");
				for ( Integer item : itemset.getItems() ){
					sb.append(apNameHelper.getNameById(item));
					sb.append(",");
				}
				sb.append(") ");
			}
			sb.append(String.format("%.3f", 1.0*entry.getValue()/users.size()));
			System.out.println(sb.toString());
		}
		
	}
	
	private static Map<String, Integer> statChangePoints(String cpfile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File(cpfile)));
		String thisLine = "";
		
		Map<String, Integer> cuser = new HashMap<String, Integer>();
		while ((thisLine = br.readLine()) != null) {
			thisLine = thisLine.replaceAll("[\r\n]", "");
			if( thisLine.charAt(0) == '#' || thisLine.length() == 0)
				continue;
			String[] chops = thisLine.split(" ");
			if ( chops.length < 2 )
				continue;
			String uid  = chops[0];
			int ccnt = 0;
			for( int i = 1; i < chops.length; i++ ){
				double prob = 0.0;
				try{
					prob = Double.parseDouble(chops[i]);
				}catch (Exception NumberFormatException){
					continue;
				}
				if ( prob >= 0.8 ){
					ccnt  += 1;
				}
			}
			
			if ( ccnt > 0 )
				cuser.put(uid, ccnt);
		}
		
		br.close();
		
		return cuser;
	}
	
	
	private static PatternSet genPatternSetFromDatabase(SequenceDatabase db, double minsup, String user_id, int max_pattern_length) throws IOException{
		// Prefixspan
		AlgoPrefixSpan algo = new AlgoPrefixSpan();
		algo.setMaximumPatternLength(max_pattern_length);
		SequentialPatterns raw_patterns = algo.runAlgorithm(db, minsup, null, true);
				
		int levelCount=0;
		// translate raw patterns into my pattern set structure
		PatternSet pattern_set = new PatternSet(user_id, db.getSequenceIDs(), true, false);
		for(List<SequentialPattern> level : raw_patterns.getLevels()){
			for(SequentialPattern raw_pattern : level){
				// FILTER OUT PATTERN WITH LENGTH ONE
				if ( raw_pattern.size() == 1 ) {continue;}
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
