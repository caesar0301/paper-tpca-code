package cn.edu.sjtu.omnilab.tpca.pattern.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DataPartition {
	//This function is used to partition the log of each user according to the day
	//The form of each line in the output file is: "Date(day)	sequence of the day"
	public static void main(String[] args) throws IOException{
		//define the location of input files and output files
		String input_file_location = "I:/LocationPrediction/MarkovPredictionTest/Dartmouth_WithoutPP/Data/MergedSemanticData/";
		String input_file_path = "";
		String output_file_location = "I:/LocationPrediction/MarkovPredictionTest/Dartmouth_WithoutPP/Data/PartitionedSemanticData/";
		String output_file_path = "";
		
		//read the input_files in
		File input_file_folder = new File(input_file_location);
		if(!input_file_folder.exists())
			System.out.println("Can not find the inout_folder!");
		File[] input_files = input_file_folder.listFiles();
		
		for(File input_file : input_files){
			//Prepare for reading the file
			FileReader freader = new FileReader(input_file);
			BufferedReader breader = new BufferedReader(freader);
			
			//parser the filename and create the output file with the same name for writing preparation
			input_file_path = input_file.getAbsolutePath();
			String file_name = input_file_path.split("\\\\")[input_file_path.split("\\\\").length - 1];
			System.out.println(file_name);
			
			//output files preparation
			output_file_path = output_file_location + file_name;
			File output_file = new File(output_file_path);
			if(!output_file.exists())
				output_file.createNewFile();
			FileWriter fwriter = new FileWriter(output_file, true);
			BufferedWriter bwriter = new BufferedWriter(fwriter);
			
			//read the file line by line
			String line = "";
			String first_line = breader.readLine();
			String now_date = first_line.split("\\t")[1].split("\\ ")[0];
			String now_ap_sequence = first_line.split("\\t")[0];
			while((line = breader.readLine()) != null){
				String ap = line.split("\\t")[0];
				String start_date = line.split("\\t")[1].split("\\ ")[0];
				String end_date = line.split("\\t")[2].split("\\ ")[0];
				
				if(start_date.equals(now_date))
					//check whether the end date is the same as the start_date
					if(end_date.equals(start_date)){	//the same date
						now_ap_sequence += "," + ap;
					}
					else{	//Across two days
						now_ap_sequence += "," + ap;
						bwriter.write(now_date + "\t" + now_ap_sequence + "\n");
						now_date = end_date;
						now_ap_sequence = ap;
					}
				else{
					bwriter.write(now_date + "\t" + now_ap_sequence + "\n");
					now_date = start_date;
					now_ap_sequence = ap;
				}
			}
			bwriter.write(now_date + "\t" + now_ap_sequence);
			
			bwriter.close();
			fwriter.close();
			breader.close();
			freader.close();
		}
	}
}
