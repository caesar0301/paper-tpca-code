package omnilab.bd.pattern.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataPartitionWithTime {
	//This function is used to partition the log of each user according to the day
	//The form of each line in the output file is: "Date(day)	sequence of the day"
	public static void main(String[] args) throws IOException, ParseException{
		//define the location of input files and output files
		String input_file_location = "I:/LocationPrediction/MarkovPredictionTest/Dartmouth_WithoutPP/Data/MergedSemanticData/";
		String input_file_path = "";
		String output_file_location = "I:/LocationPrediction/MarkovPredictionTest/Dartmouth_WithoutPP/Data/PartitionedSemanticDataWithTime/";
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
			String date = "";
			while((line = breader.readLine()) != null){
				String ap = line.split("\\t")[0];
				String start_date = line.split("\\t")[1].split("\\ ")[0];
				String start_time = line.split("\\t")[1].split("\\ ")[1];
				String end_date = line.split("\\t")[2].split("\\ ")[0];
				String end_time = line.split("\\t")[2].split("\\ ")[1];
				String durationString = line.split("\\t")[3];
				long duration = Long.parseLong(durationString);
				
				if(start_date.equals(date)){
					//check whether the end date is the same as the start_date
					if(end_date.equals(start_date)){	//the same date
						bwriter.write(ap + "\t" + start_time + "\t" + end_time + "\t" + duration + "\n");
					}
					else{	//Across two days
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date start = df.parse(line.split("\\t")[1]);
						Date end_of_the_day = df.parse(start_date+" "+"23:59:59");
						bwriter.write(ap + "\t" + start_time + "\t" + "23:59:59" + "\t" + (end_of_the_day.getTime() - start.getTime())/1000 + "\n");
						bwriter.write(end_date + "\n");
						Date start_of_the_day = df.parse(end_date+" "+"00:00:00");
						Date end = df.parse(line.split("\\t")[2]);
						bwriter.write(ap + "\t" + "00:00:00" + "\t" + end_time + "\t" + (end.getTime() - start_of_the_day.getTime())/1000 + "\n");
						date = end_date;
					}
				}
				else{
					bwriter.write(start_date + "\n");
					bwriter.write(ap + "\t" + start_time + "\t" + end_time + "\t" + duration + "\n");
					date = start_date;
				}
			}
			
			bwriter.close();
			fwriter.close();
			breader.close();
			freader.close();
		}
	}
}
