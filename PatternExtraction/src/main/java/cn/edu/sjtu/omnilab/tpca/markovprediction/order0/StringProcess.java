package cn.edu.sjtu.omnilab.tpca.markovprediction.order0;

import java.util.ArrayList;

public class StringProcess {
	//This function is used to split the input string with "," , and store them in an arraylist
	public static ArrayList<String> str_split(String input_string){
		ArrayList<String> output_list = new ArrayList<String>();	//to store strings after split
		if(input_string.indexOf(",")!=-1){
			//input_string is combinated
			for(int k = 0; k < input_string.split("\\,").length; k++) 
				output_list.add(input_string.split("\\,")[k]);
		}
		else 
			output_list.add(input_string);
		return output_list;
	}
	
	//This function is used to combine the list of strings together into one separating with ","
	public static String str_combine(ArrayList<String> input_list){
		String output_String = "";
		for(int k = 0 ; k < input_list.size(); k++){
			output_String += input_list.get(k);
			if(k != (input_list.size()-1))
				output_String += ",";
		}
		return output_String;
	}
}
