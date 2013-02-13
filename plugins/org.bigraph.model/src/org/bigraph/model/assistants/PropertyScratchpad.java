package org.bigraph.model.assistants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The <strong>PropertyScratchpad</strong> is used to track simulated updates
 * to the model.
 * @author alec
 */
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
		public String toString() {
			return "(" + target + ", " + name + ")";
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
	 * when this PropertyScratchpad doesn't have a property value.)
	 * @param parent
	 */
	public PropertyScratchpad(PropertyScratchpad parent) {
		this.parent = parent;
	}
	
	private Map<NNPair, Object> changes = new HashMap<NNPair, Object>();
	
	private static NNPair getKey(Object target, String name) {
		return new NNPair(target, name);
	}
	
	/**
	 * Sets a property value for the given object.
	 * @param target an object
	 * @param name a property name
	 * @param newValue the value of the property
	 */
	public void setProperty(Object target, String name, Object newValue) {
		if (target != null && name != null)
			changes.put(getKey(target, name), newValue);
	}
	
	/**
	 * Removes an existing property value.
	 * @param target an object
	 * @param name a property name
	 */
	public void removeProperty(Object target, String name) {
		if (target != null && name != null)
			changes.remove(getKey(target, name));
	}
	
	/**
	 * Indicates whether or not this {@link PropertyScratchpad} contains a
	 * property value.
	 * @param target the object to check
	 * @param name a property name
	 * @return <code>true</code> if this {@link PropertyScratchpad} contains a
	 * value for the given property of the given object, or <code>false</code>
	 * otherwise
	 */
	public boolean hasProperty(Object target, String name) {
		if (target != null && name != null) {
			if (changes.containsKey(getKey(target, name))) {
				return true;
			} else if (parent != null) {
				return parent.hasProperty(target, name);
			} else return false;
		} else return false;
	}
	
	/**
	 * Retrieves a property value.
	 * @param target an object
	 * @param name a property name
	 * @return the value associated with the given property of the given
	 * object, or <code>null</code>
	 */
	public Object getProperty(Object target, String name) {
		if (target != null && name != null) {
			if (changes.containsKey(getKey(target, name))) {
				return changes.get(getKey(target, name));
			} else if (parent != null) {
				return parent.getProperty(target, name);
			} else return null;
		} else return null;
	}
	
	/**
	 * Removes all property values from this {@link PropertyScratchpad}.
	 * @return {@code this}, for convenience
	 */
	public PropertyScratchpad clear() {
		changes.clear();
		return this;
	}
	
	public interface Helper<T, V extends T> {
		V newInstance(T in);
	}
	
	class ListHelper<T> implements Helper<Collection<? extends T>, List<T>> {
		@Override
		public List<T> newInstance(Collection<? extends T> in) {
			return new ArrayList<T>(in);
		}
	}
	
	class MapHelper<T, V>
			implements Helper<Map<? extends T, ? extends V>, Map<T, V>> {
		@Override
		public Map<T, V> newInstance(Map<? extends T, ? extends V> in) {
			return new HashMap<T, V>(in);
		}
	}
	
	class SetHelper<T> implements Helper<Collection<? extends T>, Set<T>> {
		@Override
		public Set<T> newInstance(Collection<? extends T> in) {
			return new HashSet<T>(in);
		}
	}
	
	public <T, V extends T> V getModifiableComplexObject(
			Helper<T, V> helper, Object target, String name, T original) {
		@SuppressWarnings("unchecked")
		V l = (V)getProperty(target, name);
		if (l != null && !changes.containsKey(getKey(target, name))) {
			/* This object has come from the parent, so make a copy of it */
			original = l;
			l = null;
		}
		if (l == null)
			setProperty(target, name, l = helper.newInstance(original));
		return l;
	}
	
	public <T> List<T> getModifiableList(
			Object target, String name, Collection<? extends T> original) {
		return getModifiableComplexObject(
				new ListHelper<T>(), target, name, original);
	}
	
	public <T, V> Map<T, V> getModifiableMap(Object target,
			String name, Map<? extends T, ? extends V> original) {
		return getModifiableComplexObject(
				new MapHelper<T, V>(), target, name, original);
	}
	
	public <T> Set<T> getModifiableSet(
			Object target, String name, Collection<? extends T> original) {
		return getModifiableComplexObject(
				new SetHelper<T>(), target, name, original);
	}
}
