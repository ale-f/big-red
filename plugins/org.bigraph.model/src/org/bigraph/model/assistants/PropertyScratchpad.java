package org.bigraph.model.assistants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bigraph.model.changes.IChange;

public class PropertyScratchpad {
	private PropertyScratchpad parent;
	
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
	
	/**
	 * Creates a new, blank {@link PropertyScratchpad}.
	 */
	public PropertyScratchpad() {
	}
	
	/**
	 * Creates a new, blank {@link PropertyScratchpad} with the given parent.
	 * <p>(Calls to the {@link #hasProperty(Object, String)} and {@link
	 * #getProperty(Object, String)} methods will be forwarded on to the parent
	 * when this PropertyScratchpad doesn't have a match.)
	 * @param parent
	 */
	public PropertyScratchpad(PropertyScratchpad parent) {
		this.parent = parent;
	}
	
	private Map<NNPair, Object> changes = new HashMap<NNPair, Object>();
	
	private NNPair getKey(Object target, String name) {
		return new NNPair(target, name);
	}
	
	public void setProperty(Object target, String name, Object newValue) {
		if (target != null && name != null)
			changes.put(getKey(target, name), newValue);
	}
	
	public void removeProperty(Object target, String name) {
		if (target != null && name != null)
			changes.remove(getKey(target, name));
	}
	
	public boolean hasProperty(Object target, String name) {
		if (target != null && name != null) {
			if (changes.containsKey(getKey(target, name))) {
				return true;
			} else if (parent != null) {
				return parent.hasProperty(target, name);
			} else return false;
		} else return false;
	}
	
	public Object getProperty(Object target, String name) {
		if (target != null && name != null) {
			if (changes.containsKey(getKey(target, name))) {
				return changes.get(getKey(target, name));
			} else if (parent != null) {
				return parent.getProperty(target, name);
			} else return null;
		} else return null;
	}
	
	public PropertyScratchpad clear() {
		changes.clear();
		return this;
	}
	
	public IChange executeChange(IChange change) {
		if (change != null)
			change.simulate(this);
		return change;
	}
	
	public <T> List<T> getModifiableList(
			Object target, String name, List<T> original) {
		@SuppressWarnings("unchecked")
		List<T> l = (List<T>)getProperty(target, name);
		if (l != null && !changes.containsKey(getKey(target, name))) {
			/* This list has come from the parent, so make a copy of it */
			original = l;
			l = null;
		}
		if (l == null)
			setProperty(target, name, l = new ArrayList<T>(original));
		return l;
	}
}
