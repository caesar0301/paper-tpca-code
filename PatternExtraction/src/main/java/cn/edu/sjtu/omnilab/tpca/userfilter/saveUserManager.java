package cn.edu.sjtu.omnilab.tpca.userfilter;

import java.io.IOException;

import cn.edu.sjtu.omnilab.tpca.locationprediction.PatternExtract;
import cn.edu.sjtu.omnilab.tpca.pattern.utils.ObjPersistence;
import cn.edu.sjtu.omnilab.tpca.pattern.utils.UserManager;

public class saveUserManager {
	public static void main(String[] args) throws IOException, ClassNotFoundException{	//filter the users with patterns
		//Pattern Extracting
		String INPUT_USER_FOLDER = "/home/ubuntu/workspace/SJTU/Location/";
		UserManager um = PatternExtract.patternExtract(INPUT_USER_FOLDER);
		// Serialize the user manager and apname helper
		ObjPersistence.toBinary(um, "/home/ubuntu/workspace/um.dat");
		// Deserialize the user manager and apname helper
		// UserManager dserUM = (UserManager) ObjPersistence.fromBinary("um.data");
		System.out.println("Pattern Extracting End!!!!!!!!!!!!!!!!!!!!!!");
	}
}
