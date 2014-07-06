package omnilab.bd.locationprediction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import omnilab.bd.markovprediction.order0.MarkovModelZero;
import omnilab.bd.pattern.core.PatternSet;
import omnilab.bd.pattern.spmf.itemset.Itemset;
import omnilab.bd.pattern.spmf.sequence_database.Sequence;
import omnilab.bd.pattern.utils.ObjPersistence;
import omnilab.bd.pattern.utils.SequencesHelper;
import omnilab.bd.pattern.utils.UserManager;

public class MainPrediction {
	public static void main(String[] args) throws IOException, ParseException, ClassNotFoundException{
		//Pattern Extracting
		//String INPUT_USER_FOLDER = "I:/LocationPrediction/MarkovPredictionTest/Dartmouth_WithoutPP/Data/test";
		String INPUT_UM = "I:/LocationPrediction/MarkovPredictionTest/Dartmouth_WithoutPP/Data/PartitionedLocationData/";	//UM data
		UserManager um = (UserManager) ObjPersistence.fromBinary(INPUT_UM);
		//System.out.println("Pattern Extracting End!!!!!!!!!!!!!!!!!!!!!!");
			
		//prepare the output file folder
		String output_file_location = "C:/Users/gwj/Desktop/PatternPrediction/";
		File output_folder = new File(output_file_location);
		if(!output_folder.exists())
			output_folder.mkdirs();
		
		//prediction one by one	
		for ( String uid : um.getAllUsers() ) {
			//for evaluation
			int other_help_evaluation = 0;
			int other_lose_evaluation = 0;
			int self_right = 0;
			int other_right = 0;
			int combine_right = 0;
			int count = 0;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			//Prepare the output file for writing the prediction result
			File output_file = new File(output_file_location + uid);
			if(!output_file.exists()){  
    			output_file.createNewFile();  
    		}
			FileOutputStream fos = new FileOutputStream(output_file_location + uid);   //clear the file
			fos.close();
			FileWriter fwriter =new FileWriter(output_file, true);
			
			//Pattern get
			List<PatternSet> patternset_list = um.getPatternSetList(uid);
			if(patternset_list.size() == 0 || patternset_list.size() == 1)		//This user has no pattern or just one pattern set, continue!
				continue;
			SequencesHelper seqHelper = um.getSequencesHelper(uid);		//orgin sequences where patterns extract from
			
			//take the point just after the first patternset as the point where to begin prediction
			Date firstPSEndDate = patternset_list.get(0).getDateInterval(seqHelper, true);
			int begin_index = 0;
			List<Date> date_list = seqHelper.getDates();
			for(int i = 0 ; i < date_list.size(); i++){
				Date now_date = date_list.get(i);
				if(now_date.compareTo(firstPSEndDate) >= 0){
					begin_index = i;
					break;
				}
			}
			
			//model creat
			PatternPredictionModel ppModel = new PatternPredictionModel();
			ppModel.modelCreat(um, firstPSEndDate);
			
			//the sequences need to predict
			List<Sequence> predictionSequences = seqHelper.getSequencesOrigin(begin_index, date_list.size());
			
			//Prepare a model for each user   
			MarkovModelZero mmzero = new MarkovModelZero();
			
			//prediction day by day
			int PSIndex = 0;
			int MaxPSIndex = patternset_list.size() -1;
			for(int i = 0; i < predictionSequences.size(); i++){
				Date now_date = seqHelper.getSequenceDate(begin_index + i);
				fwriter.write(sdf.format(now_date) + "\n");				
				
				Sequence now_seq = predictionSequences.get(i);
				PatternSet now_PS = patternset_list.get(PSIndex);
				//find the top-20 similar patternset
				TreeMap<String, Set<Integer>> topSimilaryTreeMap = ppModel.topSimilarPSFind(now_PS);
				
				//Markov model initialize
				mmzero.initialization(topSimilaryTreeMap, uid, um);
				
				List<Itemset> ap_list = now_seq.getItemsets();
				for(int k = 0; k < ap_list.size(); k++){
					int now_ap = ap_list.get(k).get(0);
					//MarkovPrediction
					mmzero.apPrediction();
					//prediction result
					int predict_ap_self = mmzero.self_result;
					int predict_ap_other = mmzero.other_result;
					int predict_ap_combine = mmzero.combine_result;
					//test
					boolean result_self = mmzero.test(now_ap, predict_ap_self);
					boolean result_other = mmzero.test(now_ap, predict_ap_other);
					boolean result_combine = mmzero.test(now_ap, predict_ap_combine);
					//write the result into the output file
					if(result_self == true){
						self_right++;
						fwriter.write("Actual_location: " + now_ap + "\t" + "Self_Predict_location: " + predict_ap_self + "\t" + "Right" + "\n");
					}
					else {
						fwriter.write("Actual_location: " + now_ap + "\t" + "Self_Predict_location: " + predict_ap_self + "\t" + "Wrong" + "\n");
					}
					if(result_other == true){
						other_right++;
						fwriter.write("Actual_location: " + now_ap + "\t" + "Other_Predict_location: " + predict_ap_other + "\t" + "Right" + "\n");
					}
					else {
						fwriter.write("Actual_location: " + now_ap + "\t" + "Other_Predict_location: " + predict_ap_other + "\t" + "Wrong" + "\n");
					}
					if(result_combine == true){
						combine_right++;
						fwriter.write("Actual_location: " + now_ap + "\t" + "Combine_Predict_location: " + predict_ap_combine + "\t" + "Right" + "\n");
					}
					else {
						fwriter.write("Actual_location: " + now_ap + "\t" + "Combine_Predict_location: " + predict_ap_combine + "\t" + "Wrong" + "\n");
					}
					//evaluation
					if(result_self == false && result_other == true && result_combine == true)	//evaluate the help with others' data
						other_help_evaluation++;
					if(result_self == true && result_other == false && result_combine == false)	//evaluate the lose with others' data
						other_lose_evaluation++;
					
					count++;
					
					//update the model
					//mmzero.update(now_ap);
				}
				
				//Model update
				if(i != (predictionSequences.size()-1)){						
					Date next_prediction_date = seqHelper.getSequenceDate(begin_index + i + 1);
					if(PSIndex != MaxPSIndex){	//whether it is the last patternset
						//PSIndex update
						Date nextPSMaxDate = patternset_list.get(PSIndex + 1).getDateInterval(seqHelper, true);
						if(next_prediction_date.compareTo(nextPSMaxDate) > 0){
							PSIndex ++;
						}
					}
					//Model update
					ppModel.modelUpdate(um, next_prediction_date);
				}
				else{
					//The last day to prediction, prediction end
					fwriter.write("the help with others' data: "+other_help_evaluation);
					fwriter.write("the lose with others' data: "+other_lose_evaluation);
					fwriter.write("the accuracy with self data: "+(float)self_right/count);
					fwriter.write("the accuracy with other data: "+(float)other_right/count);
					fwriter.write("the accuracy with combine data: "+(float)combine_right/count);
					fwriter.close();
				}				
			}
			
			ppModel.clear();
		}		
		
		um.clear();
	}
	
	/*
	//find the nearest patternset of the prediction ap
	private static int firstPSFind(List<PatternSet> patternset_list, SequencesHelper seqHelper, Date prediction_date){
		int patternSetIndex = -1; 
		for(int i = 0 ; i < patternset_list.size(); i++){
			PatternSet now_PS = patternset_list.get(i);
			Date end_date = now_PS.getDateInterval(seqHelper, true);			
			if(end_date.compareTo(prediction_date) < 0){
				patternSetIndex = i;
			}
			else 
				break;
		}
		return patternSetIndex;
	}
	*/
}
