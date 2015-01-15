package omnilab.bd.markovprediction.order0;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

public class Evaluation {
	private ArrayList<User> user_list;;
	private ArrayList<Float> accuracy_list;
	
	public Evaluation(){
		this.user_list = new ArrayList<User>();
		this.accuracy_list = new ArrayList<Float>();
	}
	
	public void add_user(User user){
		this.user_list.add(user);
	}
	
	public void add_accuracy(float accuracy){
		this.accuracy_list.add(accuracy);
	}
	
	public void find_max_min_accuracy(){
		//find the highest and lowest accuracy user	
		User max_user = new User("", (float)0);		//to store the user whose accuracy is highest
		User min_user = new User("", (float)0);		//to store the user whose accuracy is lowest
		
		float max_accuracy = (float)0;
		float min_accuracy = (float)1;
		
		for(int i = 0; i < this.user_list.size(); i ++){
			User now_user = this.user_list.get(i);
			if(now_user.accuracy > max_accuracy){
				max_accuracy = now_user.accuracy;
				max_user = now_user;
			}
			if(now_user.accuracy < min_accuracy){
				min_accuracy = now_user.accuracy;
				min_user = now_user;
			}
		}
		System.out.println("Total user : "+ this.user_list.size());
		System.out.println("MAX accuray : "+ max_user.user_name + " "+ max_user.accuracy);
		System.out.println("MIN accuray : "+ min_user.user_name + " "+ min_user.accuracy);		
	}
	
	public void find_median_accuracy(){
		Collections.sort(this.accuracy_list);	
		float median_accuracy = this.accuracy_list.get(this.accuracy_list.size()/2);
		System.out.println("Median accuracy is: " + median_accuracy);
	}
	
	public void find_average_accuracy(){
		float total_accuracy = (float)0;
		for(int i = 0; i < this.accuracy_list.size(); i++){
			total_accuracy += this.accuracy_list.get(i);
		}
		float average_accuracy = total_accuracy/this.accuracy_list.size();
		System.out.println("Average accuracy is: "+ average_accuracy);
	}
	
	public void linenum_accracy_export(String input_file_location, String output_file_path) throws IOException{
		//find the linenum
		LineCounter lcounter = new LineCounter();
		TreeMap<String, Integer> line_num = lcounter.counter(input_file_location);
		
		//Prepare output file
		File output_file = new File(output_file_path);
		if(!output_file.exists())
			output_file.createNewFile();
		FileWriter fwriter = new FileWriter(output_file);
		fwriter.write("User_name"+"\t"+"Line_number"+"\t"+"Accuracy"+"\n");
		
		//write the result into the file
		String u_name = "";
		int u_lnum = 0;
		float u_accuracy;
		for(int i = 0; i < this.user_list.size(); i ++){
			User now_user = this.user_list.get(i);
			u_name = now_user.user_name;
			u_accuracy = now_user.accuracy;
			if(line_num.containsKey(u_name))
				u_lnum = line_num.get(u_name);
			else 
				System.out.println("Can not find this user!");
			fwriter.write(u_name+"\t"+u_lnum+"\t"+u_accuracy+"\n");
		}	
		fwriter.close();
	}
}
