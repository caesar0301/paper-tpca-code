package omnilab.bd.markovprediction.order0;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;

public class LineCounter {
	//This method is used to count the number of lines for each file in the folder "input_file_location"
	//return a TreeMap, in which the Key is file name and the Value is the line number of the file
	public TreeMap<String, Integer> counter(String input_file_location) throws IOException{
		TreeMap<String, Integer> line_num = new TreeMap<String, Integer>();
		
		File input_folder = new File(input_file_location);
		if(!input_folder.exists())
			System.out.println("The input folder does not exist!");
		File[] files = input_folder.listFiles();
		
		for(File input_file : files){
			String input_file_name = input_file.getAbsolutePath().split("\\\\")[input_file.getAbsolutePath().split("\\\\").length - 1];
			int num = 0;
			
			FileReader freader = new FileReader(input_file);
			BufferedReader breader = new BufferedReader(freader);
				
			while((breader.readLine() != null)){
				num++;
			}
			
			line_num.put(input_file_name, num);
			
			breader.close();
			freader.close();
		}
		return line_num;
	}
}
