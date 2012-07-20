package it.uniud.bigredit.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bigraph.model.Link;
import org.bigraph.model.ModelObject;
import org.bigraph.uniud.bigraph.match.BidiMap;

public class MatchData {

	/**
	 * ModelObject in Redex, ModelObject in Agent
	 */
	private HashMap<ModelObject, ModelObject> map;
	private HashMap<ModelObject, ArrayList<ModelObject>> mapSites;
	private BidiMap<Link, Link> mapLink;

	public void addRootMatch(ModelObject rootR, ModelObject elementA) {
		map.put(rootR, elementA);
	}
	
	public void addSiteMatch(ModelObject rootR, ModelObject elementA) {
		if (mapSites.get(rootR)==null){
			mapSites.put(rootR, new ArrayList<ModelObject>());
		}
		mapSites.get(rootR).add(elementA);
	}
	
	public ArrayList<ModelObject> getSiteMapping(ModelObject siteR){
		return mapSites.get(siteR);
	}

	public HashMap<ModelObject, ModelObject> getMappingData() {
		return map;
	}

	public MatchData() {
		this.map = new HashMap<ModelObject, ModelObject>();
		this.mapLink = new BidiMap<Link, Link>();
		this.mapSites= new HashMap <ModelObject, ArrayList<ModelObject>> ();
	}

	public ModelObject getRoot() {
		for (Entry<ModelObject, ModelObject> m : map.entrySet()) {
			return m.getKey();
		}

		return null;
	}

	public void setLinkMap(BidiMap<Link, Link> mapLink) {
		this.mapLink = mapLink;
	}

	public BidiMap<Link, Link> getLinkMap() {
		return this.mapLink;
	}

}
