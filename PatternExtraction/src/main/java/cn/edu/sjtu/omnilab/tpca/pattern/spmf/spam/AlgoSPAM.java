package cn.edu.sjtu.omnilab.tpca.pattern.spmf.spam;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.edu.sjtu.omnilab.tpca.pattern.spmf.BIDE_and_prefixspan.SequentialPattern;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.sequence_database.Sequence;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.BIDE_and_prefixspan.SequentialPatterns;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.itemset.Itemset;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.sequence_database.SequenceDatabase;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.tools.MemoryLogger;


/*** 
 * This is an implementation of the SPAM algorithm. 
 * 
 * The SPAM algorithm was originally described in this paper:
 * 
 *     Jay Ayres, Johannes Gehrke, Tomi Yiu, and Jason Flannick. Sequential PAttern Mining Using Bitmaps. 
 *     In Proceedings of the Eighth ACM SIGKDD International Conference on Knowledge Discovery and Data Mining. 
 *     Edmonton, Alberta, Canada, July 2002.
 * 
 * I tried to do what is indicated in that paper but some optimizations are not described with enough details in the paper.
 * So my implementation does not include these optimizations for example:
 * - lookup tables for bitmaps
 * - compression of bitmaps.
 *
 * Copyright (c) 2008-2012 Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
 */

public class AlgoSPAM{
		
	// for statistics
	private long startTime;
	private long endTime;
	private int patternCount;
	
	// minsup
	private int minsup = 0;

	// object to write to a file
	BufferedWriter writer = null;
	
	// Vertical database
	Map<Integer, Bitmap> verticalDB = new HashMap<Integer, Bitmap>();
	
	// List indicating the number of bits per sequence
	List<Integer> sequencesSize = null;
	int lastBitIndex = 0;  // the last bit position that is used in bitmaps
	
	// maximum pattern length in terms of item count
	private int maximumPatternLength = Integer.MAX_VALUE;
	
	// The sequential patterns that are found 
	// (if the user want to keep them into memory)
	private SequentialPatterns patterns = null;
		
	/**
	 * Default constructor
	 */
	public AlgoSPAM(){
	}

	/**
	 * Method to run the algorithm
	 * @param input  path to an input file
	 * @param outputFilePath path for writing the output file
	 * @param minsupRel the minimum support as a relative value 
	 * @throws IOException exception if error while writing the file or reading
	 */
	public SequentialPatterns runAlgorithm(SequenceDatabase database, String outputFilePath, double minsupRel) throws IOException {
		// initialize the number of patterns found
		patternCount =0; 
		// to log the memory used
		MemoryLogger.getInstance().reset(); 
		// record start time
		startTime = System.currentTimeMillis(); 
		// RUN THE ALGORITHM
		spam(database, outputFilePath, minsupRel); 
		// record end time
		endTime = System.currentTimeMillis(); 
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		return patterns;
	}
	
	/**
	 * This is the main method for the SPAM algorithm
	 * @param an input file
	 * @param minsupRel the minimum support as a relative value
	 * @throws IOException 
	 */
	private void spam(SequenceDatabase database, String outputFilePath, double minsupRel) throws IOException{
		// if the user want to keep the result into memory
		if(outputFilePath == null){
			writer = null;
			patterns = new SequentialPatterns("FREQUENT SEQUENTIAL PATTERNS");
		}else{ // if the user want to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(outputFilePath)); 
		}
		
		// the structure to store the vertical database
		// key: an item    value : bitmap
		verticalDB = new HashMap<Integer, Bitmap>();
		
		// STEP 0: SCAN THE DATABASE TO STORE THE FIRST BIT POSITION OF EACH SEQUENCE 
		// AND CALCULATE THE TOTAL NUMBER OF BIT FOR EACH BITMAP
		sequencesSize = new ArrayList<Integer>();
		lastBitIndex =0; // variable to record the last bit position that we will use in bitmaps
		int bitIndex =0;
		// for each line (sequence) in the file until the end
		for ( Sequence sequence : database.getSequences() ) {
			// record the length of the current sequence (for optimizations)
			sequencesSize.add(bitIndex);
			// split the sequence according to spaces into tokens
			for( Itemset itemset : sequence.getItemsets() ){
				// increase the number of bits that we will need for each bitmap
				bitIndex++;
			}
		}
		// record the last bit position for the bitmaps
		lastBitIndex = bitIndex -1;
		
		// Calculate the absolute minimum support 
		// by multipling the percentage with the number of
		// sequences in this database
		minsup = (int)(minsupRel * sequencesSize.size());
		if(minsup ==0){
			minsup =1;
		}
		
		// STEP1: SCAN THE DATABASE TO CREATE THE BITMAP VERTICAL DATABASE REPRESENTATION
		int sid =0; // to know which sequence we are scanning
		// for each line (sequence) from the input file
		for ( Sequence sequence : database.getSequences() ) {
			// for each itemset in the sequence
			for ( int i = 0; i < sequence.size(); i++ ){
				for ( Integer item : sequence.get(i).getItems() ){
					// Get the bitmap for this item. If none, create one.
					Bitmap bitmapItem = verticalDB.get(item);
					if(bitmapItem == null){
						bitmapItem = new Bitmap(lastBitIndex);
						verticalDB.put(item, bitmapItem);
					}
					// Register the bit in the bitmap for this item
					bitmapItem.registerBit(sid, i, sequencesSize);
				}
			}
			sid++;
		}
		
		// STEP2: REMOVE INFREQUENT ITEMS FROM THE DATABASE BECAUSE THEY WILL NOT APPEAR IN ANY FREQUENT SEQUENTIAL PATTERNS
		List<Integer> frequentItems = new ArrayList<Integer>();
		Iterator<Entry<Integer, Bitmap>> iter = verticalDB.entrySet().iterator();
		// we iterate over items from the vertical database that we have in memory
		while (iter.hasNext()) {
			//  we get the bitmap for this item
			Map.Entry<Integer, Bitmap> entry = (Map.Entry<Integer, Bitmap>) iter.next();
			// if the cardinality of this bitmap is lower than minsup
			if(entry.getValue().getSupport() < minsup){
				// we remove this item from the database.
				iter.remove(); 
			}else{
				// otherwise, we save this item as a frequent
				// sequential pattern of size 1
				savePattern(entry.getKey(), entry.getValue());
				// and we add this item to a list of frequent items
				// that we will use later.
				frequentItems.add(entry.getKey());
			}
		}
		
		// STEP3: WE PERFORM THE RECURSIVE DEPTH FIRST SEARCH
		// to find longer sequential patterns recursively
		
		if(maximumPatternLength == 1){
			return;
		}
		// for each frequent item
		for(Entry<Integer, Bitmap> entry: verticalDB.entrySet()){
			// We create a prefix with that item
			Prefix prefix = new Prefix();
			prefix.addItemset(new Itemset(entry.getKey()));
			// We call the depth first search method with that prefix
			// and the list of frequent items to try to find
			// larger sequential patterns by appending some of these
			// items.
			dfsPruning(prefix, entry.getValue(), frequentItems, frequentItems, entry.getKey(), 2);
		}
	}
	
	/**
	 * This is the dfsPruning method as described in the SPAM paper.
	 * @param prefix the current prefix
	 * @param prefixBitmap  the bitmap corresponding to the current prefix
	 * @param sn  a list of items to be considered for i-steps
	 * @param in  a list of items to be considered for s-steps
	 * @param hasToBeGreaterThanForIStep
	 * @param m size of the current prefix in terms of items
	 * @throws IOException  if there is an error writing a pattern to the output file
	 */
	private void dfsPruning(Prefix prefix, Bitmap prefixBitmap, List<Integer> sn, List<Integer> in, int hasToBeGreaterThanForIStep, int m) throws IOException {
//		System.out.println(prefix.toString());
		
		//  ======  S-STEPS ======
		// Temporary variables (as described in the paper)
		List<Integer> sTemp = new ArrayList<Integer>();
		List<Bitmap> sTempBitmaps = new ArrayList<Bitmap>();
		
		// for each item in sn
		for(Integer i : sn){
			// perform the S-STEP with that item to get a new bitmap
			Bitmap newBitmap = prefixBitmap.createNewBitmapSStep(verticalDB.get(i), sequencesSize,  lastBitIndex);
			// if the support is higher than minsup
			if(newBitmap.getSupport() >= minsup){
				// record that item and pattern in temporary variables
				sTemp.add(i); 
				sTempBitmaps.add(newBitmap);
			}
		}
		// for each pattern recorded for the s-step
		for(int k=0; k < sTemp.size(); k++){
			int item = sTemp.get(k);
			// create the new prefix
			Prefix prefixSStep = prefix.cloneSequence();
			prefixSStep.addItemset(new Itemset(item));
			// create the new bitmap
			Bitmap newBitmap = sTempBitmaps.get(k);

			// save the pattern to the file
			savePattern(prefixSStep, newBitmap);
			// recursively try to extend that pattern
			if(maximumPatternLength > m){
				dfsPruning(prefixSStep, newBitmap, sTemp, sTemp, item, m+1);
			}
		}
		
		// ========  I STEPS =======
		// Temporary variables
		List<Integer> iTemp = new ArrayList<Integer>();
		List<Bitmap> iTempBitmaps = new ArrayList<Bitmap>();
		
		// for each item in in
		for(Integer i : in){
			// the item has to be greater than the largest item
			// already in the last itemset of prefix.
			if(i > hasToBeGreaterThanForIStep){
				
				// Perform an i-step with this item and the current prefix.
				// This creates a new bitmap
				Bitmap newBitmap = prefixBitmap.createNewBitmapIStep(verticalDB.get(i), sequencesSize,  lastBitIndex);
				// If the support is no less than minsup
				if(newBitmap.getSupport() >= minsup){
					// record that item and pattern in temporary variables
					iTemp.add(i);
					iTempBitmaps.add(newBitmap);
				}
			}
		}
		// for each pattern recorded for the i-step
		for(int k=0; k < iTemp.size(); k++){
			int item = iTemp.get(k);
			// create the new prefix
			Prefix prefixIStep = prefix.cloneSequence();
			prefixIStep.getItemsets().get(prefixIStep.size()-1).addItem(item);
			// create the new bitmap
			Bitmap newBitmap = iTempBitmaps.get(k);
			
			// save the pattern
			savePattern(prefixIStep, newBitmap);
			// recursively try to extend that pattern
			if(maximumPatternLength > m){
				dfsPruning(prefixIStep, newBitmap, sTemp, iTemp, item, m+1);
			}
		}	
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * Save a pattern of size 1 to the output file
	 * @param item the item
	 * @param bitmap its bitmap
	 * @throws IOException exception if error while writing to the file
	 */
	private void savePattern(Integer item, Bitmap bitmap) throws IOException {
		patternCount++; // increase the pattern count
		// if the result should be saved to a file
		if(writer != null){
			StringBuffer r = new StringBuffer("");
			r.append(item);
			r.append(" -1 ");
			r.append("SUP: ");
			r.append(bitmap.getSupport());
//			System.out.println(r);
			writer.write(r.toString());
			writer.newLine();
		} else {
			// Add this patter to memory
			SequentialPattern sequentialPattern = new SequentialPattern();
			sequentialPattern.addItemset(new Itemset(item));
			// TODO: FIND THE TRUE SEQUENCES ID
			Set<Integer> sequencesID = new HashSet<Integer>();
			// fake sequence IDs to make sequential pattern function well.
			for ( int i = 0; i < bitmap.getSupport(); i++ ) { sequencesID.add(-i-1); }
			sequentialPattern.setSequencesID(sequencesID);
			this.patterns.addSequence(sequentialPattern, 1);
		}
	}
	
	/**
	 * Save a pattern of size > 1 to the output file.
	 * @param prefix the prefix
	 * @param bitmap its bitmap
	 * @throws IOException exception if error while writing to the file
	 */
	private void savePattern(Prefix prefix, Bitmap bitmap) throws IOException {
		patternCount++;
		// if the result should be saved to a file
		if(writer != null){
			StringBuffer r = new StringBuffer("");
			for(Itemset itemset : prefix.getItemsets()){
				for(Integer item : itemset.getItems()){
					String string = item.toString();
					r.append(string);
					r.append(' ');
				}
				r.append("-1 ");
			}
	
			r.append("SUP: ");
			r.append(bitmap.getSupport());
			
			writer.write(r.toString());
//			System.out.println(r.toString());
			writer.newLine();
		} else {
			// Add this patter to memory
			SequentialPattern sequentialPattern = new SequentialPattern();
			for(Itemset itemset : prefix.getItemsets()) { sequentialPattern.addItemset(itemset); }
			// TODO: FIND THE TRUE SEQUENCES ID
			Set<Integer> sequencesID = new HashSet<Integer>();
			// fake sequence IDs to make sequential pattern function well.
			for ( int i = 0; i < bitmap.getSupport(); i++ ) { sequencesID.add(-i-1); }
			sequentialPattern.setSequencesID(sequencesID);
			this.patterns.addSequence(sequentialPattern, prefix.getItemsets().size());
		}
	}

	/**
	 * Print the statistics of the algorithm execution to System.out.
	 */
	public void printStatistics(int size) {
		StringBuffer r = new StringBuffer(200);
		r.append("=============  Algorithm - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" Frequent sequences count : " + patternCount);
		r.append('\n');
		r.append(" Max memory (mb) : " );
		r.append(MemoryLogger.getInstance().getMaxMemory());
		r.append(patternCount);
		r.append('\n');
		r.append("===================================================\n");
		// if the result was save into memory, print it
		if(patterns !=null){
			patterns.printFrequentPatterns(size);
		}
		System.out.println(r.toString());
	}

	/**
	 * Get the maximum length of patterns to be found (in terms of itemset count)
	 * @return the maximumPatternLength
	 */
	public int getMaximumPatternLength() {
		return maximumPatternLength;
	}

	/**
	 * Set the maximum length of patterns to be found (in terms of itemset count)
	 * @param maximumPatternLength the maximumPatternLength to set
	 */
	public void setMaximumPatternLength(int maximumPatternLength) {
		this.maximumPatternLength = maximumPatternLength;
	}

}
