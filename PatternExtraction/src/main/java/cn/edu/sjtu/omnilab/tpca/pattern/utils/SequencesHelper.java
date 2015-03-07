package cn.edu.sjtu.omnilab.tpca.pattern.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import cn.edu.sjtu.omnilab.tpca.pattern.ap.APItemset;
import cn.edu.sjtu.omnilab.tpca.pattern.ap.APNameHelper;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.itemset.Itemset;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.sequence_database.Sequence;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.sequence_database.SequenceDatabase;

/**
 * A helper storage pool of all sequences for an individual user.
 * @author gwj
 *
 */
public class SequencesHelper implements Serializable {
	private static final long serialVersionUID = 2723225347601018569L;
	// global AP name map, must be initialized by setApNameHelper
	private APNameHelper apNameHelper = null;
	// All sequences of specific user
	private List<Sequence> sequences;
	// Related date sequences
	private List<Date> dates;

	private SimpleDateFormat sdfDay = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	/**
	 * Default constructor
	 * @param path
	 * @param with_time
	 * @throws IOException
	 */
	public SequencesHelper(String path, boolean with_time, APNameHelper apNameHelper) throws IOException{
		this.sequences = new LinkedList<Sequence>();
		this.dates = new LinkedList<Date>();
		this.apNameHelper = apNameHelper;
		
		if (with_time)
			loadFileWithTime(path);
		else
			loadFileWithDay(path);
	}
	
	
	/**
	 * Add a sequence with specific date to this helper
	 * @param seq
	 * @param date
	 */
	public void addSequence(Sequence seq, Date date){
		this.sequences.add(seq);
		this.dates.add(date);
	}
	
	
	/**
	 * Set the AP name helper of this helper
	 * @param helper
	 */
	@Deprecated
	public void setApNameHelper(APNameHelper helper){
		this.apNameHelper = helper;
	}
	
	
	/**
	 * Get the AP name helper which matches the AP IDs and names
	 * @return
	 */
	public APNameHelper getApNameHelper(){
		return this.apNameHelper;
	}
	
	
	/**
	 * Get the number of sequences in this helper
	 * @return
	 */
	public int size(){
		return sequences.size();
	}
	
	
	/**
	 * Get all dates of the sequences in this helper
	 * @return
	 */
	public List<Date> getDates(){
		return this.dates;
	}
	
	
	/**
	 * Get the date of specific sequence with given index
	 * @param index
	 * @return
	 */
	public Date getSequenceDate(int index){
		return this.dates.get(index);
	}
	
	
	/**
	 * Get all sequences in this helper
	 * @return
	 */
	public List<Sequence> getSequences(){
		List<Sequence> compressed_sequences = new LinkedList<Sequence>();
		for ( Sequence seq : this.sequences)
			compressed_sequences.add(compressSequence(seq));
		return compressed_sequences;
	}
	
	
	/**
	 * Get a sub list of sequences with given start and end indexes
	 * @param start
	 * @param end
	 * @return
	 */
	public List<Sequence> getSequences(int start, int end){
		List<Sequence> compressed_sequences = new LinkedList<Sequence>();
		for ( Sequence seq : this.sequences.subList(start, end))
			compressed_sequences.add(compressSequence(seq));
		return compressed_sequences;
	}
	

	/**
	 * Get the sequence with given index
	 * @param index
	 * @return
	 */
	public Sequence getSequence(int index){
		return compressSequence(this.sequences.get(index));
	}
	
	
	/**
	 * Get all original sequences without compression in this helper
	 * @return
	 */
	public List<Sequence> getSequencesOrigin(){
		return this.sequences;
	}
	
	
	/**
	 * Get a sub list of original sequences with given start and end indexes
	 * @param start
	 * @param end
	 * @return
	 */
	public List<Sequence> getSequencesOrigin(int start, int end){
		return this.sequences.subList(start, end);
	}
	

	/**
	 * Get the original sequence with given index
	 * @param index
	 * @return
	 */
	public Sequence getSequenceOrigin(int index){
		return this.sequences.get(index);
	}
	
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("----Sequence Helper-----\n");
		for ( int i=0; i<sequences.size(); i++){
			sb.append(sequences.get(i).getId());
			sb.append("\t");
			sb.append(sdfTime.format(dates.get(i)));
			sb.append("\t");
			sb.append(sequences.get(i).toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	
	/**
	 * Generate segments from inner database.
	 * @param win_volumn_max Maximum number of intervals with trajectories
	 * @param win_volumn_min Minimum number of intervals with trajectories
	 * @param win_width_max
	 * @param win_width_min
	 * @param sliding_fraction
	 * @return A list of segments
	 */
	public List<SequenceDatabase> createSegmentDatabases(int win_volumn_max, int win_volumn_min,
			int win_width_max, int win_width_min, double sliding_fraction){
		List<Date>  dates = this.getDates();
		List<Sequence> sequences = this.getSequences();
		List<SequenceDatabase> segmentdbs = new LinkedList<SequenceDatabase>();
		
		//assert(dates.size() == sequences.size());
		// Please make sure the data is in corrct format.
		if ( dates.size() != sequences.size() || dates.size() < 2 ){
			return segmentdbs;
		}
		
		Date date_max = dates.get(dates.size()-1);
		Date date_start = dates.get(0);
		Date date_end = date_start;

		while (date_max.getTime()/1000 - date_start.getTime()/1000 >= win_width_min*24*3600){
			// decide the end time
			int ctmp = 0;
			for( Date dtmp : this.dates){
				long delta = dtmp.getTime()/1000 - date_start.getTime()/1000;
				if ( delta >= 0 ){
					if ( delta < win_width_max*24*3600 && ctmp < win_volumn_max ){
						ctmp++;
						date_end = dtmp;
					} else {
						break;
					}
				}
			}

			// start a new segment database
			SequenceDatabase newdb = new SequenceDatabase();
			for (int i=0; i < this.dates.size(); i++){
				double curtime = this.dates.get(i).getTime()/1000;
				if ( curtime <= date_end.getTime()/1000 && curtime >= date_start.getTime()/1000 ){
					newdb.addSequence(this.getSequence(i), this.dates.get(i));
				}
			}
			
			// check lower constraint
			if ( newdb.size() >= win_volumn_min && newdb.getTimeSpanDays() >= win_width_min){
				// only add the database meeting lower constraints.
				segmentdbs.add(newdb);
			}
			
			// update date start
			double end_time = date_end.getTime()/1000;
			double start_time = date_start.getTime()/1000;
			double delta = end_time - start_time;	// seconds
			long new_start = (long) (sliding_fraction * delta + start_time);
			date_start = new Date(new_start*1000);

			for( Date dtmp : this.dates){
				if ( dtmp.getTime()/1000 > date_start.getTime()/1000 ){
					date_start = dtmp;
					break;
				}
			}

			date_end = date_start;
		}
		
		return segmentdbs;
	}
	
	
	/**
	 * A compression operation to reduce the voulme of sequence without PATTERN loss 
	 * @param seq
	 * @return
	 */
	private Sequence compressSequence(Sequence seq){
		// COMPRESS AP SEQUENCE WITHOUT PATTERN INFORMATION LOSS
		Sequence new_seq = new Sequence(seq.getId());
		for( Itemset itemset : seq.getItemsets() ){
			if ( new_seq.size() == 0 )
				new_seq.addItemset(itemset);
			else{
				// temp itemset pair to store current itemset and the latest added one
				List<Itemset> temp_pair = new LinkedList<Itemset>();
				temp_pair.add(new_seq.get(new_seq.size()-1));
				temp_pair.add(itemset);
				// TRY TO FIND THE SAME PAIR AND SKIP IF THE PAIR EXISTS
				boolean found = false;
				for ( int i = 0; i < new_seq.size()-1; i++){
					if ( new_seq.get(i).containsAll(temp_pair.get(0)) && new_seq.get(i+1).containsAll(temp_pair.get(1)) )
						found = true;
				}
				if ( ! found )
					new_seq.addItemset(itemset);
			}
		}
		return new_seq;
	}
	
	
	/**
	 * Load sequence records in file which takes format:
	 *   2003-02-28	ResBldg43,ResBldg43,ResBldg43,ResBldg43
	 *   2003-03-01	ResBldg43,ResBldg43,ResBldg43,ResBldg43,ResBldg43
	 *   2003-03-02	ResBldg43,ResBldg43,ResBldg43,ResBldg43
	 *   2003-03-03	ResBldg43,ResBldg43,ResBldg43,ResBldg43
	 *
	 * @param path The path to file.
	 * @throws IOException
	 */
	private void loadFileWithDay(String path) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			// prepare IO stream
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			// var to store date time of each record
			String dateString = "";
			// var to store apnames of each record
			String apString = "";
			// for each line of the file
			while ((thisLine = myInput.readLine()) != null) {
				// remove newline char
				thisLine = thisLine.replaceAll("[\r\n]", "");
				// check if the line is a comment
				if(thisLine.charAt(0) != '#' && thisLine.length() != 0){
					// create a sequence from each line
					String[] lineParts = thisLine.split("\t");
					if(lineParts.length < 2)
						continue;
					// extract datetime and ap names
					dateString = lineParts[0];
					apString = lineParts[1];
					
					// transform list of ap names into list of ap itemsets
					List<APItemset> apitemsets = new ArrayList<APItemset>();
					for (String apname : apString.split(",")) { 
						apitemsets.add(new APItemset(apname));
					}
					// Add this sequence, timestamp of day
					this.addSequence(createSequence(apitemsets), sdfDay.parse(dateString));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(myInput != null) myInput.close();
	    }
	}


	/**
	 * Load sequence records in file which takes format:
	 *   2013-05-03 14:13:26	2013-05-03 15:37:40	YXL-4-A
	 *   2013-05-03 17:07:16	2013-05-03 21:54:05	YXL-3,YXL-4-B,YXL-3,YXL-4-A,YXL-3
	 *   2013-05-04 09:36:07	2013-05-04 12:59:31	YXL-4-A,YXL-2,YXL-3,YXL-4-A,YXL-3
	 *   2013-05-04 14:53:20	2013-05-04 15:16:55	YXL-3,YXL-4-B,YXL-3,YXL-3,YXL-4-A
	 *
	 * @param path The path to file.
	 * @throws IOException
	 */
	private void loadFileWithTime(String path) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			String dateString = "";
			String apString = "";

			while ((thisLine = myInput.readLine()) != null) {
				// remove newline char
				thisLine = thisLine.replaceAll("[\r\n]", "");
				if( thisLine.length() == 0)
					continue;

				// create a sequence from each line
				String[] lineParts = thisLine.split("\t");
				if ( lineParts.length < 3 )
					continue;

				// extract DAY TIME and ap names
				dateString = lineParts[0];
				apString = lineParts[2];
				
				// transform list of ap names into list of ap itemsets
				List<APItemset> apitemsets = new ArrayList<APItemset>();
				for (String apname : apString.split(",")) { 
					apitemsets.add(new APItemset(apname));
				}

				// Add this sequence, timstamp of seconds
				this.addSequence(createSequence(apitemsets), sdfTime.parse(dateString));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
	}
	
	public Sequence createSequence(List<APItemset> apitemsets) {
		Sequence sequence = new Sequence(this.size());
		Itemset itemset = new Itemset();
		for(int i=0; i<apitemsets.size(); i++){
			for (String apname : apitemsets.get(i).getAPNames()){
				itemset.addItem(this.getApNameHelper().getIdByName(apname));
			}
			sequence.addItemset(itemset);
			itemset = new Itemset();
		}
		return sequence;
	}
}
