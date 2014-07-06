package omnilab.bd.pattern.clustering;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


public class Dataset {
	// all instances stored in this dataset
	private List<Instance> instances = new LinkedList<Instance>();
	
	/**
	 * Creates an empty data set.
	 */
    public Dataset() {}

	/**
	 * Creates a data set that contains the provided instances
	 * @param coll collection with instances
	 */
	public Dataset(Collection<Instance> coll) {
		this.instances.addAll(coll);
	}

	/**
	 * Get the count of instances of this dataset
	 * @return The count of instances of this dataset.
	 */
	public int size(){
		return instances.size();
	}
	
	/**
	 * Sort the instances of the dataset by instance ID
	 */
	public void sort(){
		Collections.sort(instances, new Comparator<Instance>() {
			public int compare(Instance i1, Instance i2){
				return i1.getID() - i2.getID();
			}
		});
	}

	/**
	 * Clear this dataset
	 */
	public void clear() {
		instances.clear();
	}

	/**
	 * Add an instance to this dataset
	 * @param e The instance to add.s
	 */
	public void add(Instance e) {
		instances.add(e);
	}

	/**
	 * Add an instance at specific index.
	 * @param index The position to add instance.
	 * @param e The instance to add.
	 */
	public void add(int index, Instance e) {
		instances.add(index, e);
	}

	/**
	 * Get the instance at specific position.
	 * @param index The position of the instance.
	 * @return The instance at position index.
	 */
	public Instance getInstance(int index) {
		return instances.get(index);
	}
	
	/**
	 * Plain representation of this dataset.
	 */
	public String toString(){
		this.sort();
		StringBuffer r = new StringBuffer("");
		r.append("****** Cluster *******\n");
		for ( Instance i : instances ){
			r.append(i.hashCode());
			r.append('\n');
		}
		r.append("********");
		return r.toString();
	}
}
