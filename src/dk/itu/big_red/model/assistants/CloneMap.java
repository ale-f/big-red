package dk.itu.big_red.model.assistants;

import java.util.HashMap;

import dk.itu.big_red.model.ModelObject;

public class CloneMap {
	private HashMap<ModelObject, ModelObject> objects =
		new HashMap<ModelObject, ModelObject>();
	
	public void clear() {
		objects.clear();
	}
	
	public ModelObject addCloneOf(ModelObject original, ModelObject clone) {
		objects.put(original, clone);
		return clone;
	}
	
	public ModelObject getCloneOf(ModelObject original) {
		return objects.get(original);
	}
}
