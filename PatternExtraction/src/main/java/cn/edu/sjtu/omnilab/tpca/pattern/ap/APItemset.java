package cn.edu.sjtu.omnilab.tpca.pattern.ap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class APItemset {
	private List<String> APNames;
	private Date SessionStart;
	private Date SessionEnd;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public APItemset( String APNames){
		this.APNames = Arrays.asList(APNames.split(","));
		this.SessionStart = null;
		this.SessionEnd = null;
	}
	
	public APItemset(String APNames, String SessionStart, String SessionEnd) throws ParseException{
		this.APNames = Arrays.asList(APNames.split(","));
		this.SessionStart = this.sdf.parse(SessionStart);
		this.SessionEnd = this.sdf.parse(SessionEnd);
	}
	
	public long getSessionDurationMillisec(){
		// Return the milliseconds
		return this.SessionEnd.getTime()-this.SessionStart.getTime();
	}
	
	public String[] getAPNames(){
		return (String[]) this.APNames.toArray();
	}
	
	public Date getSessionStart(){
		return this.SessionStart;
	}
	
	public String toString(){
		String buf = Arrays.toString(this.getAPNames());
		return buf;
	}
	
	public String toStringWithTime(){
		String buf = Arrays.toString(this.getAPNames());
		buf += ("\t"+this.sdf.format(this.SessionStart));
		buf += ("\t"+this.getSessionDurationMillisec()/1000);
		return buf;
	}
}
