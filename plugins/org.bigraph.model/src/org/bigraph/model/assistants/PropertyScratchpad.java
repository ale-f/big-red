package org.bigraph.model.assistants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PropertyScratchpad implements IPropertyProvider {
	private static final class NNPair {
		private Object target;
		private String name;
		
		private NNPair(Object target, String name) {
			this.target = target;
			this.name = name;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof NNPair) {
				NNPair p = (NNPair)obj;
				return
					(target.equals(p.target) && name.equals(p.name));
			} else return false;
		}
		
		@Override
		public int hashCode() {
			return target.hashCode() ^ name.hashCode();
		}
	}
	
	private Map<NNPair, Object> changes = new HashMap<NNPair, Object>();
	
	private NNPair getKey(Object target, String name) {
		return new NNPair(target, name);
	}
	
	@Override
	public void setProperty(Object target, String name, Object newValue) {
		if (target != null && name != null)
			changes.put(getKey(target, name), newValue);
	}
	
	public void removeProperty(Object target, String name) {
		if (target != null && name != null)
			changes.remove(getKey(target, name));
	}
	
	@Override
	public boolean hasProperty(Object target, String name) {
		if (target != null && name != null) {
			return changes.containsKey(getKey(target, name));
		} else return false;
	}
	
	public PropertyScratchpad clear() {
		changes.clear();
		return this;
	}
	
	@Override
	public Object getProperty(Object target, String name) {
		if (target != null && name != null) {
			return changes.get(getKey(target, name));
		} else return null;
	}
	
	public <T> List<T> getModifiableList(
			Object target, String name, List<T> original) {
		@SuppressWarnings("unchecked")
		List<T> l = (List<T>)getProperty(target, name);
		if (l == null)
			setProperty(target, name, l = new ArrayList<T>(original));
		return l;
	}
}
