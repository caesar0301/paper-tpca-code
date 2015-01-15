package omnilab.bd.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import omnilab.bd.pattern.utils.FileManager;
import omnilab.bd.pattern.utils.MathUtils;


/**
 * @author chenxm
 */
class ChangeSegment{
	public double startSec;
	public double endSec;
}


class LocationRecord{
	public String name;
	public double startSec;
	public double endSec;
}

/**
 * Correlate the Region of Interests (ROI) and pattern changes.
 * Work with the output of correlatePatAndChange.
 * @author chenxm
 *
 */
public class correlateROIAndChange {

	public static void main(String [] args) throws IOException, ParseException{
		// read change segments
		String chTsFile = "/Users/chenxm/Desktop/corRoiChTs.txt";
		Map<String, List<ChangeSegment>> userSegmentMap = new HashMap<String, List<ChangeSegment>>();
		BufferedReader br = new BufferedReader(new FileReader(chTsFile));
		String thisLine = null;
		while( (thisLine = br.readLine()) != null){
			// remove newline char
			thisLine = thisLine.replaceAll("[\r\n]", "");
			// check if the line takes zero length
			if(thisLine.length() == 0) continue;
			// split line
			String[] lineParts = thisLine.split(" ");
			// new change segment
			ChangeSegment cs = new ChangeSegment();
			String uid = lineParts[0];
			cs.startSec = Double.parseDouble(lineParts[1]);
			cs.endSec = Double.parseDouble(lineParts[2]);
			// add to map store
			if ( ! userSegmentMap.containsKey(uid) )
				userSegmentMap.put(uid, new LinkedList<ChangeSegment>());
			userSegmentMap.get(uid).add(cs);
		}
		br.close();
		
        
		// file manager for input logs
		String userFolder = "/Users/chenxm/Jamin/Datasets/dartmouth/MergedLocation/";
		FileManager fm = new FileManager();
		List<String> files = fm.searchFilesRegex(userFolder, "\\d+");
		
		for ( String uid : userSegmentMap.keySet() ){
			// warning about lost user
			if ( ! files.contains(uid)){
				System.err.println("Lost user in merged location dataset.");
				continue;
			}
			// read records of this user
			String uFile = userFolder + uid;
			BufferedReader recBW = new BufferedReader(new FileReader(uFile));
			List<LocationRecord> records = new LinkedList<LocationRecord>();
			String oneRecord = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			while( (oneRecord = recBW.readLine()) != null){
				// remove newline char
				oneRecord = oneRecord.replaceAll("[\r\n]", "");
				// check if the line takes zero length
				if(oneRecord.length() == 0) continue;
				// split line
				String[] lineParts = oneRecord.split("\t");
				// create a new record
				LocationRecord newRecord = new LocationRecord();
				newRecord.name = lineParts[0];
				newRecord.startSec = sdf.parse(lineParts[1]).getTime()/1000;
				newRecord.endSec = sdf.parse(lineParts[2]).getTime()/1000;
				// add new record to local list
				records.add(newRecord);
			}
			// reading finishes.
			recBW.close();
			
			// prepare output file writer
			File roiFile = new File("/Users/chenxm/Desktop/corRoiCh.txt");
			FileWriter roiFW = new FileWriter(roiFile, true);
			
			for( ChangeSegment cs : userSegmentMap.get(uid) ){
				// generate a data point for each change segment
				Map<String, Double> locationDurationMap = new HashMap<String, Double>();
				for ( LocationRecord rec : records ){
					// accumulate the duration for each location in this change segment.
					if ( rec.startSec >= cs.startSec && rec.endSec <= cs.endSec ){
						if ( ! locationDurationMap.containsKey(rec.name) )
							locationDurationMap.put(rec.name, 0.0);
						locationDurationMap.put(rec.name, 
								locationDurationMap.get(rec.name) + 
								(rec.endSec-rec.startSec) / 3600); // hours
					}
				}
				
				// calculate entropies
				double total_duration = 0.0;
				for ( Double dur : locationDurationMap.values() )
					total_duration += dur.doubleValue();
				
				if ( total_duration != 0 ){
					double[] probs = new double[locationDurationMap.size()];
					int i = 0;
					for ( Double dur : locationDurationMap.values() ){
						probs[i] = dur / total_duration;
						i++;
					}
					
					double timespan = (cs.endSec-cs.startSec)/3600/24; // days
					double entropy = MathUtils.shannonEntropy(probs);
					System.out.println(String.format("%.1f,%.3f", timespan, entropy));
					roiFW.write(String.format("%.1f,%.3f\n", timespan, entropy));
				}
			}
			
			roiFW.close();
		}
		
	}
}