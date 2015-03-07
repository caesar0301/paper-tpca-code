package cn.edu.sjtu.omnilab.tpca.pattern.clustering;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import cn.edu.sjtu.omnilab.tpca.pattern.core.PatternSet;
import cn.edu.sjtu.omnilab.tpca.pattern.core.PatternSetSimilarity;
import cn.edu.sjtu.omnilab.tpca.pattern.utils.CommonUtils;

public class PatternSetDistance extends DistanceMeasure {
	class Pair{
		PatternSet ps1 = null;
		PatternSet ps2 = null;
		
		public Pair(PatternSet ps1, PatternSet ps2){
			this.ps1 = ps1; this.ps2 = ps2;
		}
		
		public int hashCode(){
			int c1 = this.ps1.hashCode();
			int c2 = this.ps2.hashCode();
			int lower = c1 < c2 ? c1 : c2;
			int upper = c1 < c2 ? c2 : c1;
			return lower * 3 + upper * 7;
		}
		
		public boolean equals(Object pair){
			if ( this == pair)
				return true;
			if ( !(pair instanceof Pair))
				return false;
			Pair that = (Pair) pair;
			if ( (that.ps1.equals(this.ps1) && that.ps2.equals(this.ps2)) ||
					(that.ps1.equals(this.ps2) && that.ps2.equals(this.ps1)) )
				return true;
			return false;
		}
	}
	
	// Map storing PatternSet pair and their distance measure
	private static Map<Pair, Double> speedUpMap = null;
			
	public PatternSetDistance(){
		speedUpMap = new HashMap<Pair, Double>();;
	}

	@Override
	// measure in [1/3, 1]
	public double measure(Instance x, Instance y) {
		PatternSet px = (PatternSet) x.dataValue();
		PatternSet py = (PatternSet) y.dataValue();
		Pair new_pair = new Pair(px, py);
		if ( !speedUpMap.containsKey(new_pair) ){
			double similarity = PatternSetSimilarity.similarityWeightedBySupport(new_pair.ps1, new_pair.ps2);
			speedUpMap.put(new_pair, CommonUtils.similarity2Distance(similarity));
		}
		return speedUpMap.get(new_pair);
	}
	
	public static void distanceWrite() throws IOException{
		//prepare to write the distance in
		String output_file_location = "C:/Users/gwj/Desktop/distance";
		File output_file = new File(output_file_location);
		if(!output_file.exists()){
			output_file.createNewFile();
		}
		//clear the file
		FileOutputStream fos = new FileOutputStream("C:/Users/gwj/Desktop/distance"); 
		fos.close();
		
		FileWriter output_filewriter =new FileWriter(output_file, true);
		
		Iterator<Pair> speedUpMap_iter = speedUpMap.keySet().iterator();
		Pair now_pair = null;
		while(speedUpMap_iter.hasNext()){
			now_pair = speedUpMap_iter.next();
			Double distance = speedUpMap.get(now_pair);					
			PatternSet px = now_pair.ps1;
			PatternSet py = now_pair.ps2;
			output_filewriter.write("x: "+ px.getUserID() + "," + px.getTransIDs().toString() + "\t" + "y: " + py.getUserID() + "," + py.getTransIDs().toString() + "\t" + "Dis: " + distance + "\n");
		}
		output_filewriter.close();
	}
	
	public static void distanceDistributionAnalysis(){
		TreeMap<Integer, Integer> distance_distribution = new TreeMap<Integer, Integer>();	//Key: Lower distance	Value: count
		
		Iterator<Pair> speedUpMap_iter = speedUpMap.keySet().iterator();
		Pair now_pair = null;
		while(speedUpMap_iter.hasNext()){
			now_pair = speedUpMap_iter.next();
			Double distance = speedUpMap.get(now_pair);
			int distance_scope = 0;
			if(distance == 1.0){
				distance_scope = 10;
			}
			else{
				String distance_string = distance.toString();
				String distance_scope_string = distance_string.substring(2,3);
				distance_scope = Integer.parseInt(distance_scope_string);
			}
			if(distance_distribution.containsKey(distance_scope)){
				int count = distance_distribution.get(distance_scope);
				count ++;
				distance_distribution.put(distance_scope, count);
			}
			else{
				distance_distribution.put(distance_scope, (int)1);
			}
		}
		System.out.println("Distance distribution~~~~~~~~~~~~~~~~~~~~~~~~~");
		Iterator<Integer> distance_distribution_Iterator = distance_distribution.keySet().iterator();
		int scope = 0;
		while(distance_distribution_Iterator.hasNext()){
			scope = distance_distribution_Iterator.next();
			int count = distance_distribution.get(scope);
			System.out.println(scope + "\t" + count);
		}
	}
	
	public static void kDistanceGraph(int k) throws IOException{	//K >= 2
		//Add distance list
		TreeMap<String, ArrayList<Double>> alldistance_tmap = new TreeMap<String, ArrayList<Double>>();		//Key: uid+","+transid		Value: all distance list
		
		Iterator<Pair> speedUpMap_iter = speedUpMap.keySet().iterator();
		Pair now_pair = null;
		while(speedUpMap_iter.hasNext()){
			now_pair = speedUpMap_iter.next();
			Double distance = speedUpMap.get(now_pair);
			PatternSet px = now_pair.ps1;
			PatternSet py = now_pair.ps2;
			String px_id = px.getUserID() + "," + px.getTransIDs().toString();
			String py_id = py.getUserID() + "," + py.getTransIDs().toString();
			if(px_id.equals(py_id)){	//remove yourself
				continue;
			}
            distanceListAdd(px_id, distance, alldistance_tmap);
            distanceListAdd(py_id, distance, alldistance_tmap);
		}
		
		//to store the k-th distance for every instance
		TreeMap<String, Double> kdistance_tmap = new TreeMap<String, Double>();		//Key: uid+","+transid		Value: k-th distance
		
		//prepare file for k-distance list writing
		String output_file_location = "C:/Users/gwj/Desktop/distancegraph" + k;
		File output_file = new File(output_file_location);
		if(!output_file.exists()){
			output_file.createNewFile();
		}
		//clear the file
		FileOutputStream fos = new FileOutputStream("C:/Users/gwj/Desktop/distancegraph" + k); 
		fos.close();
		FileWriter output_filewriter =new FileWriter(output_file, true);
		
		DoubleComparator comparator = new DoubleComparator();
		
		Iterator<String> kdistance_iter = alldistance_tmap.keySet().iterator();
		String id = "";
		while(kdistance_iter.hasNext()){
			id = kdistance_iter.next();
			ArrayList<Double> distance_list = alldistance_tmap.get(id);
			//distance sort
			Collections.sort(distance_list , comparator);
			
			double kdistance = distance_list.get(k-2);
			kdistance_tmap.put(id, kdistance);
			
			//export k-distance list
			output_filewriter.write(kdistance + "\n");
		}	
		output_filewriter.close();
	}
	
	public static void distanceListAdd(String id, Double distance, TreeMap<String, ArrayList<Double>> kdistance_tmap){		
		ArrayList<Double> distance_list = new ArrayList<Double>();
		if(kdistance_tmap.containsKey(id)){
			distance_list = kdistance_tmap.get(id);
		}
		distance_list.add(distance);
		kdistance_tmap.put(id, distance_list);
	}
	
}

class DoubleComparator implements Comparator<Double>{
	@Override
	public int compare(Double distance1, Double distance2) {
		if(distance1 < distance2)
			return -1;
		else if(distance1 > distance2)
			return 1;
		return 0;
	}
}
