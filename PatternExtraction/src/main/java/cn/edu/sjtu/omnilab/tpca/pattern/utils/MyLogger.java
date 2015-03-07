package cn.edu.sjtu.omnilab.tpca.pattern.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class MyLogger {
	private String logFileName = "";
	private FileWriter fw = null;
	
	public MyLogger(String logFile){
		logFileName = logFile;
		try {
			fw = new FileWriter(new File(logFileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void Logging(String line){
		if ( line.charAt(line.length()-1) == '\n'){
			try {
				fw.write(line);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				fw.write(line+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void close(){
		try {
			this.fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
