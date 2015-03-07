package cn.edu.sjtu.omnilab.tpca.pattern.spmf.BIDE_and_prefixspan;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.edu.sjtu.omnilab.tpca.pattern.spmf.itemset.Itemset;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.sequence_database.Sequence;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.tools.MemoryLogger;
import cn.edu.sjtu.omnilab.tpca.pattern.spmf.sequence_database.SequenceDatabase;


/***
 * This is an implementation of the PrefixSpan algorithm by Pei et al. 2001
 * This implementation is part of the SPMF framework.
 * 
 * NOTE: This implementation saves the pattern  to a file as soon 
 * as they are found or can keep the pattern into memory, depending
 * on what the user choose.
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

public class AlgoPrefixSpan{
		
	// for statistics
	private long startTime;
	private long endTime;
	
	// the number of pattern found
	private int patternCount;
	
	// relative minimum support
	private int minsuppRelative;

	// writer to write output file
	BufferedWriter writer = null;
	
	// The sequential patterns that are found 
	// (if the user want to keep them into memory)
	private SequentialPatterns patterns = null;
	
	// maximum pattern length in terms of item count
	private int maximumPatternLength = Integer.MAX_VALUE;
	
	// Update by chenxm 2013-07-06
	// remove duplicate itemset in adjacent positions.
	private boolean no_dupicate_adj = true;
	
	
	/**
	 * Default constructor
	 */
	public AlgoPrefixSpan(){
		
	}
	
	/**
	 * Run the algorithm
	 * @param database : a sequence database
	 * @param minsupPercent  :  the minimum support as a percentage (e.g. 50%)
	 * @param outputFilePath : the path of the output file to save the result
	 *                         or null if you want the result to be saved into memory
	 * @return return the result, if saved into memory, otherwise null
	 * @throws IOException  exception if error while writing the file
	 */
	public SequentialPatterns runAlgorithm(SequenceDatabase database, double minsupPercent, 
			String outputFilePath, boolean no_dupicate_adj) throws IOException {
		// convert to a relative minimum support
		this.minsuppRelative = (int) Math.ceil(minsupPercent* database.size());
		if(this.minsuppRelative == 0){ // protection
			this.minsuppRelative = 1;
		}
		// remove duplicate itemset in adjacent positions.
		this.no_dupicate_adj = no_dupicate_adj;
		// record start time
		startTime = System.currentTimeMillis();
		// run the algorithm
		prefixSpan(database, outputFilePath);
		// record end time
		endTime = System.currentTimeMillis();
		return patterns;
	}
	
	/**
	 * Run the algorithm
	 * @param database : a sequence database
	 * @param minsupPercent  :  the minimum support as an integer
	 * @param outputFilePath : the path of the output file to save the result
	 *                         or null if you want the result to be saved into memory
	 * @return return the result, if saved into memory, otherwise null 
	 * @throws IOException  exception if error while writing the file
	 */
	public SequentialPatterns runAlgorithm(SequenceDatabase database, String outputFilePath, int minsup) throws IOException {
		// initialize variables for statistics
		patternCount =0;
		MemoryLogger.getInstance().reset();
		// save the minsup chosen  by the user
		this.minsuppRelative = minsup;
		// save the start time
		startTime = System.currentTimeMillis();
		// run the algorithm
		prefixSpan(database, outputFilePath);
		// save the end time
		endTime = System.currentTimeMillis();
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		return patterns;
	}
	
	/**
	 * This is the main method for the PrefixSpan algorithm that is called
	 * to start the algorithm
	 * @param outputFilePath  an output file path if the result should be saved to a file
	 *                        or null if the result should be saved to memory.
	 * @param database a sequence database
	 * @throws IOException exception if an error while writing the output file
	 */
	private void prefixSpan(SequenceDatabase database, String outputFilePath) throws IOException{
		// if the user want to keep the result into memory
		if(outputFilePath == null){
			writer = null;
			patterns = new SequentialPatterns("FREQUENT SEQUENTIAL PATTERNS");
		}else{ // if the user want to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(outputFilePath)); 
		}
		
		// We have to scan the database to find all frequent sequential patterns of size 1.
		// We note the sequences in which these patterns appear.
		Map<Integer, Set<Integer>> mapSequenceID = findSequencesContainingItems(database);
		
		// WE CONVERT THE DATABASE ITON A PSEUDO-DATABASE, AND REMOVE
		// THE ITEMS OF SIZE 1 THAT ARE NOT FREQUENT, SO THAT THE ALGORITHM 
		// WILL NOT CONSIDER THEM ANYMORE. 
		
		// Create a list of pseudosequence
		List<PseudoSequence> initialContext = new ArrayList<PseudoSequence>();
		// for each sequence in  the database
		for(Sequence sequence : database.getSequences()){
			// remove infrequent items
			Sequence optimizedSequence = sequence.cloneSequenceMinusItems(mapSequenceID, minsuppRelative);
			if(optimizedSequence.size() != 0){
				// if the size is > 0, create a pseudo sequence with this sequence
				initialContext.add(new PseudoSequence(optimizedSequence, 0, 0));
			}
		}
				
		// For each item
		for(Entry<Integer, Set<Integer>> entry : mapSequenceID.entrySet()){
			// if the item is frequent  (has a support >= minsup)
			if(entry.getValue().size() >= minsuppRelative){ 
				Integer item = entry.getKey();
				
				// Create the prefix for this projected database
				SequentialPattern prefix = new SequentialPattern();  
				prefix.addItemset(new Itemset(item));
				prefix.setSequencesID(entry.getValue());

				// The prefix is a frequent sequential pattern.
				// We save it in the result.
				savePattern(prefix);  
				
				//***********************************************
				//  Modified by chenxm, 17/5/13
				//***********************************************
				
				// build the projected database for the item
				List<PseudoSequence> projectedContext = buildProjectedContext(item, initialContext,  false, prefix);
				
				//***********************************************
				//  Modified by chenxm, 17/5/13
				//***********************************************
				
				// We make a recursive call to try to find larger sequential
				// patterns starting with this prefix
				if(maximumPatternLength >1){
					recursion(prefix, projectedContext, 2); 
				}
			}
		}		
	}
	
	/**
	 * This method saves a sequential pattern to the output file or
	 * in memory, depending on if the user provided an output file path or not
	 * when he launched the algorithm
	 * @param prefix the pattern to be saved.
	 * @throws IOException exception if error while writing the output file.
	 */
	private void savePattern(SequentialPattern prefix) throws IOException {
		// increase the number of pattern found for statistics purposes
		patternCount++; 
	
		// if the result should be saved to a file
		if(writer != null){
			// create a stringbuffer
			StringBuffer r = new StringBuffer("");
			// for each itemset in this sequential pattern
			for(Itemset itemset : prefix.getItemsets()){
				// for each item
				for(Integer item : itemset.getItems()){
					String string = item.toString();
					r.append(string); // add the item
					r.append(' ');
				}
				r.append("-1 "); // add the itemset separator
			}		
			// add the support
			r.append(" #SUP: ");
			r.append(prefix.getSequencesID().size());
			
			//
//			//  print the list of Pattern IDs that contains this pattern.
			if(prefix.getSequencesID() != null){
				r.append(" #SID: ");
				for(Integer id : prefix.getSequencesID()){
					r.append(id);
					r.append(' ');
				}
			}
			
			// write the string to the file
			writer.write(r.toString());
			// start a new line
			writer.newLine();
		}
		// otherwise the result is kept into memory
		else{
			patterns.addSequence(prefix, prefix.size());
		}

	}
	
	/**
	 * For each item, calculate the sequence id of sequences containing that item
	 * @param database the current sequence database
	 * @return Map of items to sequence IDs that contains each item
	 */
	private Map<Integer, Set<Integer>> findSequencesContainingItems(SequenceDatabase database) {
		// We use a map to store the sequence IDs where an item appear
		// Key : item   Value :  a set of sequence IDs
		Map<Integer, Set<Integer>> mapSequenceID = new HashMap<Integer, Set<Integer>>(); 
		// for each sequence in the current database
		for(Sequence sequence : database.getSequences()){
			// for each itemset in this sequence
			for(Itemset itemset : sequence.getItemsets()){
				// for each item
				for(Integer item : itemset.getItems()){
					// get the set of sequence IDs for this item until now
					Set<Integer> sequenceIDs = mapSequenceID.get(item);
					if(sequenceIDs == null){
						// if the set does not exist, create one
						sequenceIDs = new HashSet<Integer>();
						mapSequenceID.put(item, sequenceIDs);
					}
					// add the sequence ID of the current sequence to the 
					// set of sequences IDs of this item
					sequenceIDs.add(sequence.getId());
				}
			}
		}
		return mapSequenceID;
	}
	

	/**
	 * Create a projected database by pseudo-projection
	 * @param item The item to use to make the pseudo-projection
	 * @param context The current database.
	 * @param inSuffix This boolean indicates if the item "item" is part of a suffix or not.
	 * @param oldPrefix The prefix of the database before projection
	 * @return the projected database.
	 */
	private List<PseudoSequence> buildProjectedContext(Integer item, List<PseudoSequence> database, boolean inSuffix, SequentialPattern oldPrefix) {
		// We create a new projected database
		List<PseudoSequence> sequenceDatabase = new ArrayList<PseudoSequence>();

		// for each sequence in the database received as parameter
		for(PseudoSequence sequence : database){ 
			// for each itemset of the sequence
			for(int i =0; i< sequence.size(); i++){
				// add each sequence only once
				boolean added = false;
				// check if the itemset contains the item that we use for the projection
				int index = sequence.indexOf(i, item);
				// if it does not, and the current item is part of a suffix if inSuffix is true
				//   and vice-versa
				if(index != -1 ){
	
					//***********************************************
					//  Modified by chenxm, 17/5/13
					//***********************************************
					
					// Difference between isPostfix and isPostfixOfOldPrefix:
					// The former check if some itemset is cut or not, and positive itemsets merely exist
					// on the head of a sequence; the latter check if the itemset contains part (the last itemset)
					// of the old prefix or not, and positive itemsets can exist anywhere in a sequence.
					boolean isPostfix = sequence.isPostfix(i);
					boolean isPostfixOfOldPrefix = sequence.isPostfixOfOldPrefix(i, index, oldPrefix);
					if ( inSuffix == false && isPostfix == inSuffix ||
							inSuffix == true && isPostfixOfOldPrefix == inSuffix){
						
					//***********************************************
					//  Modified by chenxm, 17/5/13
					//***********************************************
						
						// if this is not the last item of the itemset of this sequence
						if(index != sequence.getSizeOfItemsetAt(i)-1){ 
							// create a new pseudo sequence
							PseudoSequence newSequence = new PseudoSequence( 
									sequence, i, index+1);
							if(newSequence.size() >0){
								// if the size of this pseudo sequence is greater than 0
								// add it to the projected database.
								sequenceDatabase.add(newSequence);
								added = true;
							} 
						}
						// Otherwise, if this is the last itemset of the sequence	
						else if ((i != sequence.size()-1)){		
							// create a new pseudo sequence
							PseudoSequence newSequence = new PseudoSequence( sequence, i+1, 0);
							if(newSequence.size() >0){
								// if the size of this pseudo sequence is greater than 0
								// add it to the projected database.
								sequenceDatabase.add(newSequence);
								added = true;
							}	
						}
					}
				}
				
				//***********************************************
				//  Modified by chenxm, 17/5/13
				//***********************************************
				
				if ( added ) break;
				
				//***********************************************
				//  Modified by chenxm, 17/5/13
				//***********************************************
				
			}
		}
		return sequenceDatabase; // return the projected database
	}
	
	/**
	 * Method to recursively grow a given sequential pattern.
	 * @param prefix  the current sequential pattern that we want to try to grow
	 * @param database the current projected sequence database
	 * @param k  the prefix length in terms of items
	 * @throws IOException exception if there is an error writing to the output file
	 */
	private void recursion(SequentialPattern prefix, List<PseudoSequence> database, int k) throws IOException {	
		// find frequent items of size 1 in the current projected database.
		Set<Pair> pairs = findAllFrequentPairs(database, prefix);
	
		// For each pair found (a pair is an item with a boolean indicating if it
		// appears in an itemset that is cut (a postfix) or not, and the sequence IDs
		// where it appears in the projected database).
		for(Pair pair : pairs){
			// Update by chenxm 2013-07-06
			// if the item is not the same with the last item of prefix
			Itemset last_prefix_itemset = prefix.get(prefix.size()-1);
			if (this.no_dupicate_adj && 
				pair.getItem() == last_prefix_itemset.get(last_prefix_itemset.size()-1)){
				continue;
			}
				
			// if the item is frequent in the current projected database
			if(pair.getCount() >= minsuppRelative){
				// create the new postfix by appending this item to the prefix
				SequentialPattern newPrefix;
				// if the item is part of a postfix
				if(pair.isPostfix()){ 
					// we append it to the last itemset of the prefix
					newPrefix = appendItemToPrefixOfSequence(prefix, pair.getItem()); 
				}else{ // else, we append it as a new itemset to the sequence
					newPrefix = appendItemToSequence(prefix, pair.getItem());
				}
				newPrefix.setSequencesID(pair.getSequencesID()); 
				// save the pattern
				savePattern(newPrefix);
				
				//***********************************************
				//  Modified by chenxm, 17/5/13
				//***********************************************
				
				// build the projected database with this item
				List<PseudoSequence> projectedContext = buildProjectedContext(pair.getItem(), database, pair.isPostfix(), prefix);  // not new_prefix
				
				//***********************************************
				//  Modified by chenxm, 17/5/13
				//***********************************************

				// make a recursive call
				if( k < maximumPatternLength){
					recursion(newPrefix, projectedContext, k+1);
				}
			}
		}
		// check the current memory usage
		MemoryLogger.getInstance().checkMemory();
	}
	
	/**
	 * Method to find all frequent items in a projected sequence database
	 * @param sequences  the set of sequences
	 * @return A list of pairs, where a pair is an item with (1) a boolean indicating if it
	 *         is in an itemset that is "cut" and (2) the sequence IDs where it occurs.
	 */
	protected Set<Pair> findAllFrequentPairs(List<PseudoSequence> sequences, SequentialPattern prefix){
		// We use a Map the store the pairs.
		Map<Pair, Pair> mapPairs = new HashMap<Pair, Pair>();
		// for each sequence
		for(PseudoSequence sequence : sequences){
			// for each itemset
			for(int i=0; i< sequence.size(); i++){
				// for each item
				for(int j=0; j < sequence.getSizeOfItemsetAt(i); j++){
					Integer item = sequence.getItemAtInItemsetAt(j, i);
					
					//***********************************************
					//  Modified by chenxm, 17/5/13
					//***********************************************
					
					List<Pair> pairs = new LinkedList<Pair>();
					boolean isPostfix = sequence.isPostfix(i);
					// frequent pair without considering prefix
					pairs.add(new Pair(isPostfix, item));
					if ( isPostfix == false )
						// frequent pair considering prefix
						pairs.add(new Pair(sequence.isPostfixOfOldPrefix(i, j, prefix), item));
					
					//***********************************************
					//  Modified by chenxm, 17/5/13
					//***********************************************
					
					for ( Pair pair : pairs ){
						// get the pair object store in the map if there is one already
						Pair oldPair = mapPairs.get(pair);
						// if there is no pair object yet
						if(oldPair == null){
							// store the pair object that we created
							mapPairs.put(pair, pair);
						}else{
							// otherwise use the old one
							pair = oldPair;
						}
						// record the current sequence id for that pair
						pair.getSequencesID().add(sequence.getId());
					}
				}
			}
		}
		MemoryLogger.getInstance().checkMemory();  // check the memory for statistics.
		// return the map of pairs
		return mapPairs.keySet();
	}

	/**
	 *  This method creates a copy of the sequence and add a given item 
	 *  as a new itemset to the sequence. 
	 *  It sets the support of the sequence as the support of the item.
	 * @param prefix  the sequence
	 * @param item the item
	 * @return the new sequence
	 */
	private SequentialPattern appendItemToSequence(SequentialPattern prefix, Integer item) {
		SequentialPattern newPrefix = prefix.cloneSequence();  // isSuffix
		newPrefix.addItemset(new Itemset(item));
		return newPrefix;
	}
	
	/**
	 *  This method creates a copy of the sequence and add a given item 
	 *  to the last itemset of the sequence. 
	 *  It sets the support of the sequence as the support of the item.
	 * @param prefix  the sequence
	 * @param item the item
	 * @return the new sequence
	 */
	private SequentialPattern appendItemToPrefixOfSequence(SequentialPattern prefix, Integer item) {
		SequentialPattern newPrefix = prefix.cloneSequence();
		Itemset itemset = newPrefix.get(newPrefix.size()-1);  // ajoute au dernier itemset
		itemset.addItem(item);   
		return newPrefix;
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 * @param size  the size of the database
	 * @throws IOException 
	 */
	public void printStatistics(int size) throws IOException {
		StringBuffer r = new StringBuffer(200);
		r.append("=============  PREFIXSPAN - STATISTICS =============\n Total time ~ ");
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
	 * Get the maximum length of patterns to be found (in terms of item count)
	 * @return the maximumPatternLength
	 */
	public int getMaximumPatternLength() {
		return maximumPatternLength;
	}

	/**
	 * Set the maximum length of patterns to be found (in terms of item count)
	 * @param maximumPatternLength the maximumPatternLength to set
	 */
	public void setMaximumPatternLength(int maximumPatternLength) {
		this.maximumPatternLength = maximumPatternLength;
	}

}
