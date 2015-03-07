package cn.edu.sjtu.omnilab.tpca.experiments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.edu.sjtu.omnilab.tpca.pattern.utils.FileManager;


/**
 * Genrate statistics about the Datasets
 * 1. User records' total duration
 * 2. Number of trajectories.
 * 3. Trajectory duration distribution on the whole.
 * 
 * Ref: W. Gong, Trajectory Pattern Change Analysis in Campus WiFi Networks, MobiGIS'2013
 *
 */
public class statDataset {
	
	private static SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static void main(String[] args) throws IOException, ParseException{
		String userFolder = "/Users/chenxm/Jamin/Datasets/SJTU/Location/";
		// file manager for input logs
		FileManager fm = new FileManager();
		List<String> files = fm.searchFilesRegex(userFolder, "\\d+");
		
		recordDuration(userFolder, files, "/Users/chenxm/Jamin/Datasets/SJTU/statDur.txt");
		
		trajCount(userFolder, files, "/Users/chenxm/Jamin/Datasets/SJTU/statTrajCount.txt");
		
		trajDuration(userFolder, files, "/Users/chenxm/Jamin/Datasets/SJTU/statTrajDur.txt");
	}
	
	
	public static void recordDuration(String userFolder, List<String> users, String outputfile) throws IOException, ParseException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputfile)));
		
		for ( int i = 0; i < users.size(); i++){
			// for each user
			String uid = users.get(i);			
			Date startTime = null, endTime = null;
			
			BufferedReader br = new BufferedReader(new FileReader(new File(userFolder+uid)));
			String thisLine = "";
			while ((thisLine = br.readLine()) != null) {
				thisLine = thisLine.replaceAll("[\r\n]", "");
				if( thisLine.length() == 0)
					continue;
				String[] chops = thisLine.split("\t");
				if ( chops.length < 3 )
					continue;
				
				if ( startTime == null ){
					startTime = sdfTime.parse(chops[0]);
					endTime = sdfTime.parse(chops[1]);
				} else {
					Date curEndTime = sdfTime.parse(chops[1]);
					if ( curEndTime.getTime() > endTime.getTime() )
						endTime = curEndTime;
				}
			}
			br.close();
			
			if ( startTime != null && endTime != null )
				bw.write(String.format("%s,%d,%d\n", uid, startTime.getTime()/1000, endTime.getTime()/1000));
		}
		
		bw.close();
	}
	
	
	public static void trajCount(String userFolder, List<String> users, String outputfile) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputfile)));
		
		for ( int i = 0; i < users.size(); i++){
			// for each user
			String uid = users.get(i);			
			long trajc = 0;
			
			BufferedReader br = new BufferedReader(new FileReader(new File(userFolder+uid)));
			String thisLine = "";
			while ((thisLine = br.readLine()) != null) {
				thisLine = thisLine.replaceAll("[\r\n]", "");
				if( thisLine.length() == 0)
					continue;
				String[] chops = thisLine.split("\t");
				if ( chops.length < 3 )
					continue;
				trajc++;
			}
			br.close();
			
			bw.write(String.format("%s,%d\n", uid, trajc));
		}
		
		bw.close();
	}
	
	
	public static void trajDuration(String userFolder, List<String> users, String outputfile) throws IOException, ParseException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputfile)));
		
		for ( int i = 0; i < users.size(); i++){
			// for each user
			String uid = users.get(i);			
			Date startTime = null, endTime = null;
			
			BufferedReader br = new BufferedReader(new FileReader(new File(userFolder+uid)));
			String thisLine = "";
			while ((thisLine = br.readLine()) != null) {
				thisLine = thisLine.replaceAll("[\r\n]", "");
				if( thisLine.length() == 0)
					continue;
				String[] chops = thisLine.split("\t");
				if ( chops.length < 3 )
					continue;

				startTime = sdfTime.parse(chops[0]);
				endTime = sdfTime.parse(chops[1]);
				
				long delta = endTime.getTime()/1000-startTime.getTime()/1000;
				bw.write(String.format("%s,%d\n", uid, delta));
			}
			br.close();
		}
		
		bw.close();
	}
}
