package omnilab.bd.pattern.spmf.sequence_database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import omnilab.bd.pattern.spmf.itemset.Itemset;

/**
 * Implementation of a sequence. A sequence is a list of itemsets.
 * 
 * Copyright (c) 2008-2012 Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */
public class Sequence implements Serializable{
	private static final long serialVersionUID = 7799580219950158642L;
	// A sequence is a list of itemsets, where an itemset is a list of integers
	private final List<Itemset> itemsets = new ArrayList<Itemset>();
	private int id; // sequence id

	/**
	 * Constructor
	 * @param id the id of this sequence.
	 */
	public Sequence(int id) {
		this.id = id;
	}

	/**
	 * Add an itemset to this sequence.
	 * @param itemset An itemset (list of integers)
	 */
	public void addItemset(Itemset itemset) {
		itemsets.add(itemset);
	}

	/**
	 * Print this sequence to System.out.
	 */
	public void print() {
		System.out.print(toString());
	}

	/**
	 * Return a string representation of this sequence.
	 */
	public String toString() {
		StringBuffer r = new StringBuffer("");
		// for each itemset
		for (Itemset itemset : itemsets) {
			r.append('(');
			// for each item in the current itemset
			for (Integer item : itemset.getItems()) {
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append(')');
		}

		return r.append("    ").toString();
	}
	
	/**
	 * Get the sequence ID of this sequence.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get the list of itemsets in this sequence.
	 * @return the list of itemsets.
	 */
	public List<Itemset> getItemsets() {
		return itemsets;
	}

	/**
	 * Get the itemset at a given position in this sequence.
	 * @param index the position
	 * @return the itemset as a list of integers.
	 */
	public Itemset get(int index) {
		return itemsets.get(index);
	}
	
	/**
	 * Get the size of this sequence (number of itemsets).
	 * @return the size (an integer).
	 */
	public int size() {
		return itemsets.size();
	}

	/**
	 * Make a copy of this sequence while removing some items
	 * that are infrequent with respect to a threshold minsup.
	 * @param mapSequenceID a map with key = item  value = a set of sequence ids containing this item
	 * @param relativeMinSup the minimum support threshold chosen by the user.
	 * @return a copy of this sequence except that item(s) with a support lower than minsup have been excluded.
	 */
	public Sequence cloneSequenceMinusItems(Map<Integer, Set<Integer>> mapSequenceID, double relativeMinSup) {
		// create a new sequence
		Sequence sequence = new Sequence(getId());
		// for each  itemset in the original sequence
		for(Itemset itemset : itemsets){
			// call a method to copy this itemset
			Itemset newItemset = cloneItemsetMinusItems(itemset, mapSequenceID, relativeMinSup);
			// add the copy to the new sequence
			if(newItemset.size() !=0){ 
				sequence.addItemset(newItemset);
			} 
		}
		return sequence; // return the new sequence
	}
	
	/**
	 * Make a copy of an itemset while removing some items
	 * that are infrequent with respect to a threshold minsup.
	 * @param mapSequenceID a map with key = item  value = a set of sequence ids containing this item
	 * @param relativeMinSup the minimum support threshold chosen by the user.
	 * @return a copy of this itemset except that item(s) with a support lower than minsup have been excluded.
	 */
	public Itemset cloneItemsetMinusItems(Itemset itemset,Map<Integer, Set<Integer>> mapSequenceID, double relativeMinsup) {
		// create a new itemset
		Itemset newItemset = new Itemset();
		// for each item of the original itemset
		for(Integer item : itemset.getItems()){
			// if the support is enough
			if(mapSequenceID.get(item).size() >= relativeMinsup){
				newItemset.addItem(item); // add it to the new itemset
			}
		}
		return newItemset; // return the new itemset.
	} 
}
