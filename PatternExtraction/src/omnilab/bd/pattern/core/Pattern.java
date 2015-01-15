package omnilab.bd.pattern.core;

import java.io.Serializable;
import java.util.List;

import omnilab.bd.pattern.spmf.itemset.Itemset;


public class Pattern implements Serializable{
	private static final long serialVersionUID = 8583876187696492258L;
	/*
	 * Formated pattern, a little difference from sequence
	 */
	private int level = 0;					// Number of itemset in a pattern
	private int absolute_support = 0;		// Number of transactions including this pattern
	private int base_support = 0;			// Number of total transactions 
	private double relative_support = 0.0;		// Support rate
	
	private List<Itemset> itemsets = null;
	private List<Integer> tids = null;		// IDs of transactions including this pattern
	
	public static final short CLOSE_EQUAL = 0;
	public static final short CLOSE_SMALLER = 1;
	public static final short CLOSE_BIGGER = 2;
	public static final short CLOSE_DIFF = 3;
	
	public Pattern(int level){
		this.level = level;
	}
	
	public int size(){
		assert itemsets.size() == level;
		return level;
	}
	
	public int getAbsoluteSupport(){
		return absolute_support;
	}
	
	public int getSupportBase(){
		return base_support;
	}
	
	public double getRelativeSupport(){
		return relative_support;
	}
	
	public List<Itemset> getPatternItemsetList(){
		return itemsets;
	}
	
	public String getPatternItemsetListAsString(){
		StringBuffer r = new StringBuffer("");
		r.append("{");
		for ( Itemset itemset : this.itemsets){
			r.append("(");
			for ( Integer item : itemset.getItems()){
				r.append(item);
				//r.append(apNameHelper.getNameById(item));
				r.append(" ");
			}
			r.append(")");
		}
		r.append("}");
		return r.toString();
	}
	
	public List<Integer> getPatternTransactionIDs(){
		return this.tids;
	}
	
	public String getPatternTransactionIDsAsString(){
		StringBuffer r = new StringBuffer();
		if(tids != null){
			for(Integer id : tids){
				r.append(id);
				r.append(',');
			}
		}
		return r.toString();
	}
	
	public Itemset getItemsetAt(int index){
		if ( index < 0 || index >= itemsets.size() )
			return null;
		return this.itemsets.get(index);
	}
	
	public void setAbsoluteSupport(int sup){
		this.absolute_support = sup;
	}
	
	public void setRelativeSupport(double relative_support, int base){
		this.base_support = base;
		this.relative_support = relative_support;
	}
	
	public void setTransactionIDs(List<Integer> tids){
		this.tids = tids;
	}
	
	public void setPatternItemsetList(List<Itemset> itemsets){
		this.itemsets = itemsets;
	}
	
	
	public int isClosed(Pattern p, boolean support_constrained){
		boolean a2b = this.isClosedComparedTo(p, support_constrained);
		boolean b2a = p.isClosedComparedTo(this, support_constrained);
		if ( a2b == true && b2a == true )
			// different, remain both and return 11 --> 3
			return CLOSE_DIFF;
		else if ( a2b == true && b2a == false )
			// a bigger than b, remain a and return 10 --> 2
			return CLOSE_BIGGER;
		else if ( a2b == false && b2a == true )
			// a smaller than b, remain b and return 01 --> 1
			return CLOSE_SMALLER;
		else 
			// the same, remain either and return 00 --> 0
			return CLOSE_EQUAL;
	}
	
	
	public String toString(){
		StringBuffer r = new StringBuffer();
		r.append(getPatternItemsetListAsString() + " ");
		r.append('[');
		r.append(getPatternTransactionIDsAsString());
		r.append(']');
		r.append('\t');
		r.append(getRelativeSupport() + " ");
		r.append("(");
		r.append(getAbsoluteSupport()+"/"+getSupportBase());
		r.append(")");
		return r.toString();
	}
	
	
	private boolean isClosedComparedTo(Pattern p, boolean support_constrained){
		boolean isClosed = true;
		if ( support_constrained ){
			// support equality is constrained.
			if ( this.relative_support == p.relative_support ){
				boolean totally_contained = true;
				int cur_pointer = 0;
				for ( int i = 0; i < this.size(); i++ ){
					boolean contained = false;
					for ( int j = cur_pointer; j < p.size(); j++ ){
						if ( p.getItemsetAt(j).containsAll(this.getItemsetAt(i)) ){
							contained  = true;
							cur_pointer = j+1;
							break;
						}
					}
					if ( contained == false ){
						totally_contained = false;
						break;
					}
				}
				if ( totally_contained ){
					isClosed = false;
				}
			}
		} else {
			// otherwise
			boolean totally_contained = true;
			int cur_pointer = 0;
			for ( int i = 0; i < this.size(); i++ ){
				boolean contained = false;
				for ( int j = cur_pointer; j < p.size(); j++ ){
					if ( p.getItemsetAt(j).containsAll(this.getItemsetAt(i)) ){
						contained  = true;
						cur_pointer = j+1;
						break;
					}
				}
				if ( contained == false ){
					totally_contained = false;
					break;
				}
			}
			if ( totally_contained ){
				isClosed = false;
			}
		}
		return isClosed;
	}
}
