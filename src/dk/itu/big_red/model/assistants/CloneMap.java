package dk.itu.big_red.model.assistants;

import java.util.HashMap;

public class CloneMap {
	private HashMap<Object, Object> objects =
		new HashMap<Object, Object>();
	
	public void clear() {
		objects.clear();
	}
	
	public Object addCloneOf(Object original, Object clone) {
		objects.put(original, clone);
		return clone;
	}
	
	public Object getCloneOf(Object original) {
		return objects.get(original);
	}
}
