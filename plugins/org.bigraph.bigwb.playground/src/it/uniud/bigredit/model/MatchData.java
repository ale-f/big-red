package it.uniud.bigredit.model;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bigraph.model.ModelObject;


public class MatchData {

	
	/**
	 * ModelObject in Redex, ModelObject in Agent
	 */
	private HashMap <ModelObject,ModelObject> map;

	public void addRootMatch(ModelObject rootR, ModelObject elementA){
		map.put(rootR, elementA);
	}
	
	public HashMap <ModelObject,ModelObject> getMappingData(){
		return map;
	}
	
	public MatchData(){
		this.map= new HashMap<ModelObject,ModelObject>();
	}
	
	public ModelObject getRoot(){
		for (Entry<ModelObject,ModelObject> m: map.entrySet()){
			return m.getKey();
		}
		
		return null;
	}
	
}
