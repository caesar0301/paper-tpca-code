package omnilab.bd.pattern.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import omnilab.bd.pattern.core.PatternSet;

public class UserManager implements Serializable{
	/**
	 * auto generated UID for this class
	 */
	private static final long serialVersionUID = 6211004409002980337L;
	// mapping between user ID and his sequence helper
	private Map<String, SequencesHelper> userHelperMap = new HashMap<String, SequencesHelper>();
	// mapping between user ID and his pattern sets
	private Map<String, List<PatternSet>> userPatternsetsMap = new HashMap<String, List<PatternSet>>();
	
	/**
	 * Default constructor.
	 */
	public UserManager(){}
	
	/**
	 * Set the sequence helper of specific user.
	 * @param user The target user.
	 * @param helper The sequence helper to add.
	 */
	public void setSequencesHelper(String user, SequencesHelper helper){
		if ( ! this.userHelperMap.containsKey(user) )
			this.userHelperMap.put(user, helper);
		else{
			System.err.println("Duplicate SequencesHelper added to user " + user);
			this.userHelperMap.put(user, helper);
		}
	}
	
	
	/**
	 * Get all users from manager.
	 * @return A set of all user IDs.
	 */
	public Set<String> getAllUsers(){
		Set<String> helperKeySet = userPatternsetsMap.keySet();
		Set<String> psKeysSet = userHelperMap.keySet();
		return (helperKeySet.size() == 0 ? psKeysSet : helperKeySet);
	}
	
	
	/**
	 * Get the sequence helper of specific user.
	 * @param user The target user.
	 * @return The sequence helper of the user.
	 */
	public SequencesHelper getSequencesHelper(String user){
		if ( ! userHelperMap.containsKey(user) ){
			System.err.println("Null SequencesHelper for user " + user);
			return null;
		} else{
			return userHelperMap.get(user);
		}
	}
	
	/**
	 * Add a pattern set to specific user.
	 * @param user The user who owns the pattern set.
	 * @param ps The pattern set to add.
	 */
	public void addPatternSet(String user, PatternSet ps){
		if ( ! this.userPatternsetsMap.containsKey(user) ){
			LinkedList<PatternSet> list = new LinkedList<PatternSet>();
			list.add(ps);
			this.userPatternsetsMap.put(user, list);
		} else{
			this.userPatternsetsMap.get(user).add(ps);
		}
	}
	
	/**
	 * Get the list of pattern sets of specific user.
	 * @param user The target user.
	 * @return The list of pattern sets of the user.
	 */
	public List<PatternSet> getPatternSetList(String user){
		List<PatternSet> list = new LinkedList<>();
		if ( this.userPatternsetsMap.containsKey(user) ){
			list = this.userPatternsetsMap.get(user);
		}
		return list;
	}
	
	/**
	 * Get all pattern sets of users.
	 * @return A list of pattern sets of all users.
	 */
	public List<PatternSet> getAllPatternSets(){
		List<PatternSet> list = new LinkedList<>();
		for( String user : this.userPatternsetsMap.keySet()){
			list.addAll(this.userPatternsetsMap.get(user));
		}
		return list;
	}

	/**
	 * Clear data of all users.
	 */
	public void clear(){
		this.userHelperMap.clear();
		this.userPatternsetsMap.clear();
	}
	
	
	public int size(){
		return this.userPatternsetsMap.size();
	}
}
