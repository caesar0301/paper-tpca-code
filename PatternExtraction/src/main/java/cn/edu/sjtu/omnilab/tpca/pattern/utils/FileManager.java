package cn.edu.sjtu.omnilab.tpca.pattern.utils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileManager {

	/**
	 * Search all files in given directory.
	 * @param dir
	 * @return A list of file names (without path).
	 */
    public List<String> searchFiles(String dir) {
        File root = new File(dir);
        if ( !root.exists() ){
        	System.out.println(String.format("ERROR: %s does not exist!", dir));
        	System.exit(-1);
        }
        File[] filesOrDirs = root.listFiles();
        List<String> result = new LinkedList<>();
        
        for (int i = 0; i < filesOrDirs.length; i++) {
            if (filesOrDirs[i].isDirectory()) {
            	result.addAll(searchFiles(filesOrDirs[i].getAbsolutePath()));
            } else {
                result.add(filesOrDirs[i].getName());
            }
        }
        return result;
    }
    
    
    /**
     * Search all files that match regular expression under given directory.
     * @param dir
     * @param regex
     * @param flags
     * @return
     */
    public List<String> searchFilesRegex(String dir, String regex){
    	List<String> allfiles = searchFiles(dir);
    	List<String> filtered = new LinkedList<String>();
    	
    	Pattern p = Pattern.compile(regex);
    	for ( String nameString : allfiles){
    		Matcher m = p.matcher(nameString);
    		if ( m.find())
    			filtered.add(nameString);
    	}
    	return filtered;
    }
}

