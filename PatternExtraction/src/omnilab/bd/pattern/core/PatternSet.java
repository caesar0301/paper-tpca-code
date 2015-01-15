package omnilab.bd.pattern.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import omnilab.bd.pattern.utils.SequencesHelper;


public class PatternSet implements Serializable{
	private static final long serialVersionUID = 8691287951272451793L;
	// user id of this pattern set
	private String uid = null;
	// sequences in specific sliding window that generates this pattern set
	private Set<Integer> tids = new HashSet<Integer>();
	// pattern map {support: pattern list}
	// the key is a support value, and the value is a list of patterns taking the support
	private Map<Integer, List<Pattern>> supportMapPatterns = new HashMap<Integer, List<Pattern>>();
	// flag indicating if generating closed patterns
	private boolean mining_closed_patterns = false;
	// flag indicating if generating maximal patterns
	private boolean mining_maximal_patterns = false;
	
	
	/**
	 * Default constructor
	 * @param tids The sequence IDs which generate the pattern set
	 * @param close Indicating if mining closed sequential patterns
	 */
	public PatternSet(Set<Integer> tids, boolean closed, boolean maximal){
		this.tids = tids;
		this.mining_closed_patterns = closed;
	}
	
	public PatternSet(String user_id, Set<Integer> tids, boolean closed, boolean maximal){
		this.uid = user_id;
		this.tids = tids;
		this.mining_closed_patterns = closed;
	}
	
	
	/**
	 * Get the user id of this pattern set
	 * @return A string of user ID
	 */
	public String getUserID(){
		return this.uid;
	}
	
	
	/**
	 * Add a pattern to this pattern set.
	 * @param p A pattern to add.
	 */
	public void addPattern(Pattern p){
		int support = p.getAbsoluteSupport();
		if (supportMapPatterns.get(support) == null){
			supportMapPatterns.put(support, new CopyOnWriteArrayList<Pattern>());
			supportMapPatterns.get(support).add(p);
		}
		else if ( !this.mining_maximal_patterns && this.mining_closed_patterns ){
			boolean flag = false;
			
			// VERSEION 1: MERGE INCLUSIVE PATTERNS WITH THE SAME SUPPORT
			// THE CLOSED PATTERNS
			for ( Pattern pat : supportMapPatterns.get(support)){
				int status = p.isClosed(pat, true);
				if ( status == Pattern.CLOSE_EQUAL || status == Pattern.CLOSE_SMALLER) { break; }
				else if ( status == Pattern.CLOSE_BIGGER ) {
					// remove pat then add p
					supportMapPatterns.get(support).remove(pat);
					flag = true;
				} else if ( status == Pattern.CLOSE_DIFF) {
					// remain both
					flag = true;
				}
			}
			
			if ( flag ) supportMapPatterns.get(support).add(p);
		}
		else if ( this.mining_maximal_patterns ){
			boolean flag = false;
			
			//VERSION 2: MERGE ALL INCLUSIVE PATTERNS
			for ( List<Pattern> patterns : supportMapPatterns.values() ){
				for ( Pattern pat : patterns){
					int status = p.isClosed(pat, false);
					if ( status == Pattern.CLOSE_EQUAL || status == Pattern.CLOSE_SMALLER) { break; }
					else if ( status == Pattern.CLOSE_BIGGER ) {
						// remove pat then add p
						patterns.remove(pat);
						flag = true;
					} else if ( status == Pattern.CLOSE_DIFF) {
						// remain both
						flag = true;
					}
				}
			}
		
			if ( flag ) supportMapPatterns.get(support).add(p);
		}
		else {
			supportMapPatterns.get(support).add(p);
		}
	}

	
	/**
	 * Return the number of total patterns in this pattern set.
	 * @return The number of patterns.
	 */
	public int size(){
		int size = 0;
		for ( List<Pattern> list : supportMapPatterns.values()){
			size += list.size();
		}
		return size;
	}
	
	
	/**
	 * Get the i(th) pattern in this pattern set.
	 * @param i The index of pattern in this pattern set.
	 * @return The i(th) pattern.
	 */
	public Pattern getPattern(int i){
		return getPatterns().get(i);
	}
	
	
	/**
	 * Get patterns in this pattern set.
	 * @return A list of patterns in this pattern set.
	 */
	public List<Pattern> getPatterns(){
		List<Pattern> all_patterns = new LinkedList<Pattern>();
		for(List<Pattern> list : supportMapPatterns.values()){
			all_patterns.addAll(list);
		}
		// order by level
		Collections.sort(all_patterns, new Comparator<Pattern>() {
				public int compare(Pattern p1, Pattern p2){
					return p1.size() - p2.size();
			}
		});
		return all_patterns;
	}
	
	
	/**
	 * Get all the patterns whose support is larger than a minimum value.
	 * @param minsup The minimum support value.
	 * @return A list of patterns.
	 */
	public List<Pattern> getPatternsSupportLargerThan(double minsup){
		int s = (int) Math.floor(this.tids.size() * minsup);
		List<Pattern> conditioned_patterns = new LinkedList<Pattern>();
		for ( Integer key : supportMapPatterns.keySet()){
			if ( key >= s ){
				conditioned_patterns.addAll(supportMapPatterns.get(key));
			}
		}
		// order by level
		Collections.sort(conditioned_patterns, new Comparator<Pattern>() {
				public int compare(Pattern p1, Pattern p2){
					return p1.size() - p2.size();
			}
		});
		return conditioned_patterns;
	}
	
	
	/**
	 * Get the sequence IDs in the database that generates this pattern set.
	 * @return A set of transaction IDs.
	 */
	public Set<Integer> getTransIDs(){
		return this.tids;
	}
	
	
	/**
	 * print function
	 */
	public String toString(){
		return toString(0);
	}
	
	
	public String toString(double minsup){
		StringBuffer r = new StringBuffer("");
		r.append("PatternSet/");
		r.append(this.size()+":");
		r.append("\n");
		for ( Pattern pattern : this.getPatternsSupportLargerThan(minsup)){
			r.append(pattern.toString()+"\n");	// for print
		}
		return r.toString();
	}
	
	
	/**
	 * Get the max/min dates of the time interval of this pattern set.
	 * @param helper The SequencesHelper of this pattern set
	 * @param max Boolean flag indicating if getting the maximum date.
	 * @return The maximum or minimum date of this pattern set.
	 */
	public Date getDateInterval(SequencesHelper helper, boolean max){
		int date_max_index = Integer.MIN_VALUE;
		int date_min_index = Integer.MAX_VALUE;
		for ( Integer index : this.getTransIDs() ){
			if ( index > date_max_index )
				date_max_index = index;
			if ( index < date_min_index )
				date_min_index = index;
		}
		Date date_min = helper.getSequenceDate(date_min_index);
		Date date_max = helper.getSequenceDate(date_max_index);
		if (max) return date_max;
		else return date_min;
	}
	
	
	/**
	 * Get a cloned version of this pattern set with TOP number given.
	 * @param top_count
	 * @return A cloned pattern set
	 */
	public PatternSet cloneTopPatterns(int top_count){
		PatternSet clonedPatternSet = new PatternSet(this.uid, this.tids, 
				this.mining_closed_patterns, this.mining_maximal_patterns);
		List<Integer> supports = new ArrayList<Integer>(this.supportMapPatterns.keySet());
		Collections.reverse(supports);
		int counter = 0;
		for ( Integer supp : supports ){
			List<Pattern> pl = this.supportMapPatterns.get(supp);
			if ( counter + pl.size() >= top_count ){
				int delta = top_count - counter;
				if (delta > 0){
					for( int i = 0; i<delta; i++){
						clonedPatternSet.addPattern(pl.get(i));
						counter += 1;
					}
				}else{
					break;
				}
			} else {
				for ( Pattern p : pl){
					clonedPatternSet.addPattern(p);
					counter += 1;
				}
			}
		}
		return clonedPatternSet;
	}
}
