/**
 * This file is part of the Java Machine Learning Library
 * 
 * Copyright (c) 2006-2012, Thomas Abeel
 * Project: http://java-ml.sourceforge.net/
 *  
 */
package cn.edu.sjtu.omnilab.tpca.pattern.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;


class DataObject {
	int clusterIndex = -1;
	boolean processed=false;
	Instance instance;

	/** Holds the coreDistance for this DataObject */
	double c_dist;
	/** Holds the reachabilityDistance for this DataObject */
	double r_dist;

	static final int UNCLASSIFIED = -1;
	static final int UNDEFINED = Integer.MAX_VALUE;
	static final int NOISE = -2;
 
	public DataObject(Instance inst) {
		this.instance = inst;
	}

	@Override
	public boolean equals(Object obj) {
		DataObject tmp = (DataObject) obj;
		return tmp.instance.equals(this.instance);
	}

	@Override
	public int hashCode() {
		return this.instance.hashCode();
	}

	public String getKey() {
		return instance.toString();
	}
}

/**
 * Provides the density-based-spatial-scanning clustering algorithm.
 * 
 * @author Thomas Abeel
 * 
 */
public class DBSCAN {
	private Dataset originalData = null;
	private double epsilon;
	private int minPoints;
	private int clusterID;
	private DistanceMeasure dm;
	private Vector<DataObject> dataObjects = null;

	/**
	 * Creates a density based clusterer with default parameters. Epsilon = 0.1,
	 * minpoints = 6 and a normalized version of the euclidean distance.
	 */
	public DBSCAN(DistanceMeasure dm) {
		this(0.1, 6, dm);
	}

	/**
	 * Create a new Density based clusterer with the provided parameters.
	 * 
	 * @param epsilon
	 *			the epsilon value for the epsilon range query to determining
	 *			whether the density is high engou
	 * @param minPoints
	 *			the minimum number of points that should fall within the
	 *			epsilon range query.
	 * @param dm
	 *			the distance measure to use for the epsilon range query.
	 */
	public DBSCAN(double epsilon, int minPoints, DistanceMeasure dm) {
		this.dm = dm;
		this.epsilon = epsilon;
		this.minPoints = minPoints;
	}
	
	
	public Dataset[] cluster(Dataset data) {
		originalData = data;
		if (dm == null) {
			throw new RuntimeException("Distance measure can not be null.");
		}
		this.clusterID = 0;
		dataObjects = new Vector<DataObject>();
		for (int i = 0; i < originalData.size(); i++) {
			dataObjects.add(new DataObject(originalData.getInstance(i)));
		}

		Collections.shuffle(dataObjects);// make clustering algorithm random
		ArrayList<Dataset> output = new ArrayList<Dataset>();
		for (DataObject dataObject : dataObjects) {
			if (dataObject.clusterIndex == DataObject.UNCLASSIFIED) {
				if (expandCluster(dataObject)) {
					/* Extract cluster here */
					/* Cluster ids may be overwritten in further iterations */
					output.add(extract(clusterID));
					clusterID++;
				}
			}
		}
		return output.toArray(new Dataset[0]);
	}

	/**
	 * Assigns this dataObject to a cluster or remains it as NOISE
	 * 
	 * @param instance
	 *			The DataObject that needs to be assigned
	 * @return true, if the DataObject could be assigned, else false
	 */
	private boolean expandCluster(DataObject dataObject) {
		HashSet<DataObject> usedSeeds = new HashSet<DataObject>();
		List<DataObject> seedList = epsilonRangeQuery(epsilon, dataObject);
		usedSeeds.addAll(seedList);

		/** dataObject is NO coreObject */
		if (seedList.size() < minPoints) {
//			System.out.println("This is noise...");
			dataObject.clusterIndex = DataObject.NOISE;
			return false;
		}

//		 System.out.println("Object is core object");
		/** dataObject is coreObject, it has sufficient neighboring points */
		for (int i = 0; i < seedList.size(); i++) {
			DataObject seedListDataObject = seedList.get(i);
			/* Label seedListDataObject with the current clusterID */
			seedListDataObject.clusterIndex = clusterID;
			if (seedListDataObject.equals(dataObject)) {
				seedList.remove(i);
				i--;
			}
		}

//		 System.out.println("Seedlist is labeled and pruned");
		/** Iterate the seedList of the startDataObject */
		while (seedList.size() > 0) {
			DataObject seedListDataObject = seedList.get(0);
			List<DataObject> seedListDataObject_Neighbourhood = epsilonRangeQuery(epsilon, seedListDataObject);

			/** seedListDataObject is coreObject */
			if (seedListDataObject_Neighbourhood.size() >= minPoints) {
				for (int i = 0; i < seedListDataObject_Neighbourhood.size(); i++) {
					DataObject p = seedListDataObject_Neighbourhood.get(i);
					if (p.clusterIndex == DataObject.UNCLASSIFIED || p.clusterIndex == DataObject.NOISE) {
						if (p.clusterIndex == DataObject.UNCLASSIFIED) {
							if (!usedSeeds.contains(p)) {
								seedList.add(p);
								usedSeeds.add(p);
							}
						}
					}
					p.clusterIndex = clusterID;
				}
			}
			seedList.remove(0);
		}
		return true;
	}

	
	/**
	 * 
	 * @param epsilon
	 * @param inst
	 * @return
	 */
	private List<DataObject> epsilonRangeQuery(double epsilon, DataObject inst) {
		ArrayList<DataObject> epsilonRange_List = new ArrayList<DataObject>();
		for (int i = 0; i < dataObjects.size(); i++) {
			DataObject tmp = dataObjects.get(i);
			double distance = dm.measure(tmp.instance, inst.instance);
			if (distance < epsilon) {
				epsilonRange_List.add(tmp);
			}
		}
		return epsilonRange_List;
	}
	

	/* Extract a cluster from the DataObject vector */
	private Dataset extract(int clusterID) {
		Dataset cluster = new Dataset();
		for (DataObject dataObject : dataObjects) {
			if (dataObject.clusterIndex == clusterID)
				cluster.add(dataObject.instance);

		}
		return cluster;
	}

}
