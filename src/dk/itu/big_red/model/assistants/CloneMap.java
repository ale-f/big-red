package dk.itu.big_red.model.assistants;

import java.util.HashMap;
import java.util.Map;

public class CloneMap {
	private HashMap<Object, Object> objects =
		new HashMap<Object, Object>();
	
	public void clear() {
		objects.clear();
	}
	
	/**
	 * Sets an object's clone.
	 * @param original the original object
	 * @param clone its clone
	 * @return <code>clone</code>, for convenience
	 */
	public <T> T setCloneOf(T original, T clone) {
		objects.put(original, clone);
		return clone;
	}
	
	/**
	 * Gets an object's clone.
	 * @param original the original object
	 * @return its clone, if this {@link CloneMap} knows of one, or
	 * <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getCloneOf(T original) {
		return (T)objects.get(original);
	}
	
	/**
	 * Returns the underlying {@link Map} object.
	 * @return the {@link Map} from originals to clones
	 */
	public Map<Object, Object> getMap() {
		return objects;
	}
}
