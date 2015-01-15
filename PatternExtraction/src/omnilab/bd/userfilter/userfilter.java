package omnilab.bd.userfilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import omnilab.bd.locationprediction.PatternExtract;
import omnilab.bd.pattern.core.PatternSet;
import omnilab.bd.pattern.utils.ObjPersistence;
import omnilab.bd.pattern.utils.UserManager;

public class userfilter {
	public static void main(String[] args) throws IOException, ClassNotFoundException{	//filter the users with patterns
		//Pattern Extracting
		String INPUT_USER_FOLDER = "I:/LocationPrediction/SJTUArubaSyslog/SJTUPartitionedLocationDataWithSession/";
		String OUTPUT_USER_FOLDER = "I:/LocationPrediction/SJTUArubaSyslog/SJTU4Exper/";
		UserManager um = PatternExtract.patternExtract(INPUT_USER_FOLDER);
		// Serialize the user manager and apname helper
		ObjPersistence.toBinary(um, "um.data");
		// Deserialize the user manager and apname helper
		// UserManager dserUM = (UserManager) ObjPersistence.fromBinary("um.data");
		System.out.println("Pattern Extracting End!!!!!!!!!!!!!!!!!!!!!!");
		
		for ( String uid : um.getAllUsers() ) {
			//Pattern get
			List<PatternSet> patternset_list = um.getPatternSetList(uid);
			if(patternset_list.size() == 0)		//This user has no pattern, continue!
				continue;
			else{	//copy the file
				File input_file = new File(INPUT_USER_FOLDER + uid);
				FileInputStream fis = new FileInputStream(input_file);

				File output_file = new File(OUTPUT_USER_FOLDER + uid);
				if(!output_file.exists()){  
	    			output_file.createNewFile();  
	    		}
				FileOutputStream fos = new FileOutputStream(output_file);
				
				byte[] buff = new byte[104857600]; //100M
				int readed = -1;
				while((readed = fis.read(buff)) > 0)
					fos.write(buff, 0, readed);
				fis.close();
				fos.close(); 
			}
		}
	}
}
