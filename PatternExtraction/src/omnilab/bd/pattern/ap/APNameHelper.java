package omnilab.bd.pattern.ap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class APNameHelper implements Serializable{
	private static final long serialVersionUID = -8660533705329521475L;
	private Map<String, Integer> map_apname_id;
	private Map<Integer, String> map_id_apname;
	
	public APNameHelper(){
		this.map_apname_id = new HashMap<String, Integer>();
		this.map_id_apname = new HashMap<Integer, String>();
	}
	
	public int getCurrentMaxId(){
		return this.map_apname_id.size();
	}
	
	public int getIdByName(String apname){
		if (!this.map_apname_id.containsKey(apname)){
			int new_id = this.getCurrentMaxId()+1;
			this.map_apname_id.put(apname, new_id);
			//System.out.println(new_id+apname);
			this.map_id_apname.put(new_id, apname);
		}
		return map_apname_id.get(apname);
	}
	
	public String getNameById(int id){
		if ( !this.map_id_apname.containsKey(id)){
			System.err.println("Key Error for ID: " + id);
			System.exit(-1);
		}
		return map_id_apname.get(id);
	}
}
