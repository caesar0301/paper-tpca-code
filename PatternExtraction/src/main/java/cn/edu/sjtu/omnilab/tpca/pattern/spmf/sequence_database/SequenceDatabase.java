package cn.edu.sjtu.omnilab.tpca.pattern.spmf.sequence_database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.sjtu.omnilab.tpca.pattern.spmf.itemset.Itemset;


/**
 * Implementation of a sequence database, where each sequence is implemented
 * as a list of integers and should have a unique id.
 * See examples in /test/ directory for the format of input files.
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
public class SequenceDatabase {

	// variable that contains the sequences of this database
	private final List<Sequence> sequences = new ArrayList<Sequence>();
	
	// variable that contains the dates of the sequences
	private final List<Date> dates = new ArrayList<Date>();

	/**
	 * Method to load a sequence database from a text file in SPMF format.
	 * @param path  the input file path.
	 * @throws IOException exception if error while reading the file.
	 */
	public void loadFile(String path) throws IOException {
		String thisLine; // variable to read each line.
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			// for each line until the end of the file
			while ((thisLine = myInput.readLine()) != null) {
				// If the current line is not a comment
				if (thisLine.charAt(0) != '#') {
					// split this line according to spaces and process the line
					addSequence(thisLine.split(" "));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}
	
	/**
	 * Method to process a line from the input file
	 * @param tokens A list of tokens from the line (which were separated by spaces in the original file).
	 */
	public void addSequence(String[] entiers) { 
		// create a new Sequence to store the sequence
		Sequence sequence = new Sequence(sequences.size());
		// create a list of strings for the first itemset.
		
		Itemset itemset = new Itemset();
		// for each token in this line
		for (String token : entiers) {
			// if the token start with "<", it indicates a timestamp. 
			// We just ignore it because algorithms that use this class
			// don't need it.
			if (token.codePointAt(0) == '<') { 
				// we ignore
			} 
			// if the token is -1, it means that we reached the end of an itemset.
			else if (token.equals("-1")) { 
				// add the current itemset to the sequence
				sequence.addItemset(itemset);
				// create a new itemset
				itemset = new Itemset();
			} 
			// if the token is -2, it means that we reached the end of 
			// the sequence.
			else if (token.equals("-2")) { 
				// we add it to the list of sequences
				sequences.add(sequence);
			} else { 
				// otherwise it is an item.
				// we parse it as an integer and add it to 
				// the current itemset.
				itemset.addItem(Integer.parseInt(token));
			}
		}
	}
	

	/**
	 * Method to add a sequence to this sequence database
	 * @param sequence A sequence of type "Sequence".
	 */
	public void addSequence(Sequence sequence, Date date) {
		sequences.add(sequence);
		dates.add(date);
	}
	
	
	/**
	 * Print this sequence database to System.out.
	 */
	public void print() {
		System.out.println("============  CONTEXTE ==========");
		for (Sequence sequence : sequences) {
			System.out.print(sequence.getId() + ":  ");
			sequence.print();
			System.out.println("");
		}
	}
	
	
	/**
	 * Print statistics about this database.
	 */
	public void printDatabaseStats() {
		System.out.println("============  STATS ==========");
		System.out.println("Number of sequences : " + sequences.size());
		
		// Calculate the average size of sequences in this database
		long size = 0;
		for(Sequence sequence : sequences){
			size += sequence.size();
		}
		double meansize = ((float)size) / ((float)sequences.size());
		System.out.println("mean size: " + meansize);
		
		// date statistics of this database
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		long span_days = this.getTimeSpanDays();
		System.out.println(String.format("time span (days): %d, from %s to %s", span_days, 
				sdf.format(dates.get(0)), sdf.format(dates.get(dates.size()-1))));
		System.out.println("sequence density (/day): " + (float)sequences.size() / span_days);
		System.out.println("itemset density (/day): " + (float)size / span_days);
	}
	

	/**
	 * Return a string representation of this sequence database.
	 */
	public String toString() {
		StringBuffer r = new StringBuffer();
		// for each sequence
		for (Sequence sequence : sequences) { 
			r.append(sequence.getId());
			r.append(":  ");
			r.append(sequence.toString());
			r.append('\n');
		}
		return r.toString();
	}
	
	/**
	 * Get the sequence count in this database.
	 * @return the sequence count.
	 */
	public int size() {
		return sequences.size();
	}
	
	/**
	 * Get the sequences from this sequence database.
	 * @return A list of sequences (Sequence).
	 */
	public List<Sequence> getSequences() {
		return sequences;
	}
	

	/**
	 * Get the list of sequence IDs for this database.
	 * @return A set containing the sequence IDs of sequence in this
	 * database.
	 */
	public Set<Integer> getSequenceIDs() {
		// create a set
		Set<Integer> set = new HashSet<Integer>();
		// for each sequence
		for (Sequence sequence : getSequences()) {
			set.add(sequence.getId()); // add the id to the set.
		}
		return set; // return the set.
	}
	
	
	/**
	 * Get the dates of sequences from this sequence database.
	 * @return A list of dates.
	 */
	public List<Date> getDates(){
		return this.dates;
	}
	
	
	/**
	 * Get the time span of the sequences in this database
	 * @return The time span in DAYS.
	 */
	public long getTimeSpanDays(){
		Date start = this.dates.get(0);
		Date end = this.dates.get(dates.size()-1);
		return (end.getTime()-start.getTime())/1000/3600/24 + 1; // difference in days
	}

}
