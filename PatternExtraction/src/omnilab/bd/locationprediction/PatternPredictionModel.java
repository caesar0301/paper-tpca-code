package omnilab.bd.locationprediction;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import omnilab.bd.pattern.core.PatternSet;
import omnilab.bd.pattern.core.PatternSetSimilarity;
import omnilab.bd.pattern.utils.SequencesHelper;
import omnilab.bd.pattern.utils.UserManager;

public class PatternPredictionModel {
	private UserManager trainingUM;
	
	public PatternPredictionModel(){
		this.trainingUM = new UserManager();
	}
	
	//Training data, from 2012.12.31
	public void modelCreat(UserManager um, Date first_prediction_data) throws ParseException{
		for ( String uid : um.getAllUsers() ) {
			//Pattern get
			List<PatternSet> patternset_list = um.getPatternSetList(uid);
			SequencesHelper seqHelper = um.getSequencesHelper(uid);
			for(int i = 0; i < patternset_list.size(); i++ ){
				PatternSet patternSet = patternset_list.get(i);
				Date max_date = patternSet.getDateInterval(seqHelper, true);
				if(max_date.compareTo(first_prediction_data) <= 0){
					this.trainingUM.addPatternSet(uid, patternSet);
				}
			}
		}
		System.out.println(this.trainingUM.getAllUsers().size());
	}
	
	public void modelUpdate(UserManager um, Date next_prediction_date){
		for ( String uid : um.getAllUsers() ) {
			//Pattern get
			List<PatternSet> patternset_list = um.getPatternSetList(uid);
			SequencesHelper seqHelper = um.getSequencesHelper(uid);
			for(int i = 0; i < patternset_list.size(); i++ ){
				PatternSet patternSet = patternset_list.get(i);
				Date max_date = patternSet.getDateInterval(seqHelper, true);
				if(max_date.compareTo(next_prediction_date) <= 0){
					this.trainingUM.addPatternSet(uid, patternSet);
				}
			}
		}
		System.out.println(this.trainingUM.getAllUsers().size());
	}
	
	public TreeMap<String, Set<Integer>> topSimilarPSFind(PatternSet now_PS){
		TreeMap<String, Set<Integer>> topSimilarTreeMap = new TreeMap<String, Set<Integer>>();
		TreeMap<Double, List<PatternSet>> similarityPSTreeMap = new TreeMap<Double, List<PatternSet>>();
		
		//Count the similarity
		for (String uid : this.trainingUM.getAllUsers()){
			List<PatternSet> patternset_list = this.trainingUM.getPatternSetList(uid);
			for(int i = 0; i < patternset_list.size(); i++){
				PatternSet patternSet = patternset_list.get(i);
				Double similarity = PatternSetSimilarity.similarityWeightedBySupport(now_PS, patternSet);
				
				List<PatternSet> nowPSList = new ArrayList<PatternSet>();
				if(similarityPSTreeMap.containsKey(similarity)){
					nowPSList = similarityPSTreeMap.get(similarity);
					nowPSList.add(patternSet);
					similarityPSTreeMap.put(similarity, nowPSList);
				}
				else{
					nowPSList.add(patternSet);
					similarityPSTreeMap.put(similarity, nowPSList);
				}
			}
		}
		
		//Sort with the similarity
		Object[] key =  similarityPSTreeMap.keySet().toArray();    
		Arrays.sort(key);    
		
		//Find the top 20 similar PatternSet (>= 20)
		int count = 0;
		for(int i = (key.length - 1); i >= 0; i--){    
			List<PatternSet> PS_list = similarityPSTreeMap.get(key[i]);
			
			for(int k = 0; k < PS_list.size(); k++){
				PatternSet PS = PS_list.get(k);
				String PS_uid = PS.getUserID();
				Set<Integer> PS_transid = PS.getTransIDs();
				if(topSimilarTreeMap.containsKey(PS_uid)){
					Set<Integer> uid_transid = topSimilarTreeMap.get(PS_uid);
					for(Integer transid : PS_transid){
						uid_transid.add(transid);
					}
					topSimilarTreeMap.put(PS_uid, uid_transid);
				}
				else
					topSimilarTreeMap.put(PS_uid, PS_transid);
			}
			
			count += PS_list.size();
			if(count >= 20)
				break;
		}
		
		System.out.println(topSimilarTreeMap.size());
		return topSimilarTreeMap;
	}
	
	public void clear(){
		this.trainingUM.clear();
	}
}