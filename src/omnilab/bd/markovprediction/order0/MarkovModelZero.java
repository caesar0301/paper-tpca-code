package omnilab.bd.markovprediction.order0;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.security.auth.kerberos.KerberosKey;

import omnilab.bd.pattern.spmf.itemset.Itemset;
import omnilab.bd.pattern.spmf.sequence_database.Sequence;
import omnilab.bd.pattern.utils.SequencesHelper;
import omnilab.bd.pattern.utils.UserManager;

public class MarkovModelZero {
	private TreeMap<Integer, Integer> modelzero_self;	//the model created with his own hostory trajectories
	private TreeMap<Integer, Integer> modelzero_other;	//the model created with others' hostory trajectories
	private TreeMap<Integer, Float> predict_tmap_self = new TreeMap<Integer, Float>();
	private TreeMap<Integer, Float> predict_tmap_other = new TreeMap<Integer, Float>();
	private TreeMap<Integer, Float> predict_tmap_combine = new TreeMap<Integer, Float>();
	private List<Entry<Integer, Float>> sorted_predict_self = new LinkedList<Entry<Integer, Float>>();
	private List<Entry<Integer, Float>> sorted_predict_other = new LinkedList<Entry<Integer, Float>>();
	public int self_result = -1;
	public int other_result = -1;
	public int combine_result = -1;
	
	public MarkovModelZero() {
		 this.modelzero_self = new TreeMap<Integer, Integer>();	//k: AP		V:Count
		 this.modelzero_other = new TreeMap<Integer, Integer>();	//k: AP		V:Count
	}
	
	public void initialization(TreeMap<String, Set<Integer>> topSimilaryTreeMap, String uid_self, UserManager um){	//add the sequence of the top-20 pattern set to initialize the model 	
		Iterator<String> topSimilary_iter = topSimilaryTreeMap.keySet().iterator();
		while(topSimilary_iter.hasNext()){	//the sequences of each user
			String uid = topSimilary_iter.next();
			Set<Integer> transids = topSimilaryTreeMap.get(uid);
			
			TreeMap<Integer, Integer> model = uid.equals(uid_self) ? this.modelzero_self : this.modelzero_other; //if .. else ..
			for(int seq_id : transids){	//add each sequence into the model
				List<Integer> ap_list = getAPList(uid,seq_id, um);
				for(int now_ap : ap_list){
					if(model.containsKey(now_ap)){
						model.put(now_ap, model.get(now_ap)+1);
					}
					else{
						model.put(now_ap, (int)1);
					}
				}
			}
		}
	}
	
	public void update(int ap) {
		//update the model(TreeMap modelzero)
		int num;
		if(this.modelzero_self.containsKey(ap)){
			num = modelzero_self.get(ap);
			num += 1;
			this.modelzero_self.put(ap, num);
		}
		else {
			this.modelzero_self.put(ap, (int)1);
		}	
	}		

	private long find_total_num(boolean isself){
		//find the total number in the TreeMap modelzero
		TreeMap<Integer, Integer> model = isself ? this.modelzero_self : this.modelzero_other; //if .. else ..
		
		long total_num = 0;		
		Iterator<Integer> modelzero_iter = model.keySet().iterator();
		int ap_name = 0;
		if(model != null){
			while(modelzero_iter.hasNext()){
				ap_name = modelzero_iter.next();
				total_num += model.get(ap_name);
			}
		}
		return total_num;
	}
	
	private void predict_ap_and_probability(boolean isself){
		//find prediction ap and its probability which is stored in the TreeMap(K:ao name ;  V;probability)
		TreeMap<Integer, Integer> model = isself ? this.modelzero_self : this.modelzero_other; //if .. else ..
		TreeMap<Integer, Float> predict_tmap = isself ? this.predict_tmap_self : this.predict_tmap_other; //if .. else ..
		
		long total_num = find_total_num(isself);
		
		Iterator<Integer> modelzero_iter = model.keySet().iterator();
		int ap_name = 0;
		float ap_probability = (float)0;
		if(model != null){
			while(modelzero_iter.hasNext()){
				ap_name = modelzero_iter.next();
				ap_probability = (float)model.get(ap_name)/total_num;
				predict_tmap.put(ap_name, ap_probability);
				//System.out.println("ap"+ap_name+"\t"+ap_probability);
			}
		}	
	}
	
	/**
	 * Return a sorted list of entries from given SortedMap.
	 * The returned list is sorted by entry's value.
	 * @param map
	 * @return
	 */
	private List<Entry<Integer, Float>> sortedEntryList(SortedMap<Integer, Float> map){
		List<Entry<Integer, Float>> entryList = new ArrayList<Entry<Integer, Float>>(map.entrySet());
		Collections.sort(entryList, new Comparator<Entry<Integer, Float>>() {
			public int compare(Entry<Integer, Float> e1, Entry<Integer, Float> e2){
				return e1.getValue().compareTo(e2.getValue());
			}
		});
		return entryList;
	}
	
	private void combine_self_other_predict_tmap(){	//combine the top five between self result and other result
		//sort with the probability
		if(this.predict_tmap_self.size() != 0){
			this.sorted_predict_self = sortedEntryList(predict_tmap_self);
		}
		else{
			System.out.println("Self result is null!!!!!!!!!!!!!!!");
		}
		
		//other's sort with probability
		if(this.predict_tmap_other.size() != 0){
			this.sorted_predict_other = sortedEntryList(predict_tmap_other);
		}
		else{
			System.out.println("Other result is null!!!!!!!!!!!!!!!");
		}
		
		if(this.sorted_predict_self.size() != 0){
			int k =0;	//top 5 control
			for(int i = this.sorted_predict_self.size()-1; i >= 0 && k <5; i--){	//top 5
				this.predict_tmap_combine.put(this.sorted_predict_self.get(i).getKey(), this.sorted_predict_self.get(i).getValue() * (float)0.5); 	//self percent is 0.5
				k++;
			}
		}
		else{
			System.out.println("Self result is null!!!!!!!!!!!!!!!");
		}
		
		if(this.sorted_predict_other.size() != 0){
			int k = 0;	//top 5 control
			for(int i = this.sorted_predict_other.size()-1; i >= 0 && k <5; i--){	//top 5
				int now_ap = this.sorted_predict_other.get(i).getKey();
				if(this.predict_tmap_combine.containsKey(now_ap)){
					float probability = this.predict_tmap_combine.get(now_ap) + this.sorted_predict_other.get(i).getValue() * (float)0.5;	//other percent is also 0.5
					this.predict_tmap_combine.put(now_ap, probability);
				}
				else{
					this.predict_tmap_combine.put(now_ap, this.sorted_predict_other.get(i).getValue() * (float)0.5);
				}
				k++;
			}
		}
		else{
			System.out.println("Other result is null!!!!!!!!!!!!!!!");
		}
	}
	
	public void apPrediction(){	//ap prediction, return the combine prediction result
		predict_ap_and_probability(true);	//predict with self data
		System.out.println("self prediction ends");
		predict_ap_and_probability(false);	//predict with others' data		
		System.out.println("other prediction ends");
		combine_self_other_predict_tmap();	//combine prediction
		System.out.println("combine prediction ends");
		
		if(this.sorted_predict_self.size() != 0)
			//self prediction result
			this.self_result = this.sorted_predict_self.get(this.sorted_predict_self.size()-1).getKey();
		
		if(this.sorted_predict_other.size() != 0)
			//other prediction result
			this.other_result = this.sorted_predict_other.get(this.sorted_predict_other.size()-1).getKey();
				
		//sort the combine result treemap
		List<Entry<Integer, Float>> sorted_predict_combine = sortedEntryList(predict_tmap_combine);		
		//combine prediction result
		this.combine_result = sorted_predict_combine.get(sorted_predict_combine.size()-1).getKey();	
	}
	
	public boolean test(int ap, int predict_ap){
		//test the ap prediction with the actual ap is the same or not
		boolean result = false; 
/*		ArrayList<String> ap_list = StringProcess.str_split(ap);
		ArrayList<String> predictap_list = StringProcess.str_split(predict_ap);
		for(int k = 0; k < ap_list.size(); k++){
			if(predictap_list.indexOf(ap_list.get(k))!=-1)
				result = true;
		}	*/
		
		if(ap == predict_ap)
			result = true;
		return result;
	}	
	
	private List<Integer> getAPList(String uid, int seq_id, UserManager um){	//give user id and seq id, get the seq contents(ap list)
		List<Integer> ap_list = new LinkedList<Integer>();
		SequencesHelper now_seqHelper = um.getSequencesHelper(uid);		
		Sequence now_seq = now_seqHelper.getSequenceOrigin(seq_id);
		for(Itemset now_itemset : now_seq.getItemsets()){
			int now_ap = now_itemset.get(0);
			ap_list.add(now_ap);
		}
		return ap_list;
	}
}
