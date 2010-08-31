package dk.itu.big_red.model;

import java.util.HashMap;
import java.util.Map.Entry;

import dk.itu.big_red.model.interfaces.internal.INameable;

/**
 * NamespaceManagers manage {@link Class}-specific <i>namespaces</i>, groups of
 * unique-name-to-object mappings.
 * @author alec
 *
 */
public class NamespaceManager {
	public enum NameType {
		NAME_ALPHABETIC,
		NAME_NUMERIC
	};
	
	private HashMap<Class<?>, HashMap<String, Object>> names =
		new HashMap<Class<?>, HashMap<String, Object>>();
	
	protected HashMap<String, Object> getSubspace(Class<?> klass) {
		HashMap<String, Object> subspace = names.get(klass);
		if (subspace == null) {
			subspace = new HashMap<String, Object>();
			names.put(klass, subspace);
		}
		return subspace;
	}
	
	/**
	 * Returns the name given to a particular object.
	 * @param klass the {@link Class} whose namespace should be searched
	 * @param object an object
	 * @return the object's name, if it has one, or <code>null</code> otherwise
	 */
	public String getName(Class<?> klass, Object object) {
		HashMap<String, Object> subspace = getSubspace(klass);
		if (subspace.containsValue(object)) {
			for (Entry<String, Object> i : subspace.entrySet())
				if (i.getValue() == object)
					return i.getKey();
		}
		return null;
	}
	
	/**
	 * Returns the object with a particular name.
	 * @param klass the {@link Class} whose namespace should be searched
	 * @param name a name
	 * @return the named object, if there is one, or <code>null</code>
	 *         otherwise
	 */
	public Object getObject(Class<?> klass, String name) {
		return getSubspace(klass).get(name);
	}
	
	/**
	 * Adds a mapping from <code>name</code> to <code>object</code> to the
	 * <code>klass</code>-specific namespace.
	 * @param klass the {@link Class} whose namespace should be searched
	 * @param name a name
	 * @param object an object
	 * @return <code>true</code> if the mapping was registered successfully (or
	 *         if it already existed), or <code>false</code> if <code>name</code>
	 *         is already in use by a different object
	 */
	public boolean setName(Class<?> klass, String name, Object object) {
		HashMap<String, Object> subspace = getSubspace(klass);
		
		Object current = subspace.get(name);
		if (current == object) // object already has name
			return true;
		else if (current != null) // name is already taken
			return false;
		
		String currentName = getName(klass, object);
		if (currentName != null && !currentName.equals(name)) // object already has different name
			subspace.remove(currentName);
		
		subspace.put(name, object);
		return true;
	}
	
	/**
	 * Finds a unique name for <code>object</code> in the
	 * <code>klass</code>-specific namespace, registers it with that name, and
	 * returns that name.
	 * @param klass the {@link Class} whose namespace should be searched
	 * @param object an object
	 * @param t the type of name to generate
	 * @return the unique name with which <code>object</code> was registered
	 *         (not necessarily randomly generated - it might already have had
	 *         one!)
	 */
	public String newName(Class<?> klass, Object object, NameType t) {
		HashMap<String, Object> subspace = getSubspace(klass);
		
		String prospectiveName = getName(klass, object);
		if (prospectiveName != null)
			return prospectiveName;
		
		if (t == NameType.NAME_NUMERIC) {
			int i = 0;
			do {
				prospectiveName = Integer.toString(i++);
			} while (subspace.containsKey(prospectiveName));
		} else if (t == NameType.NAME_ALPHABETIC) {
			int i = 0;
			do {
				prospectiveName = "";
				int j = i;
				do {
					int lastPart = j % 26;
					prospectiveName = (char)('a' + lastPart) + prospectiveName;
					j /= 26;
				} while (j != 0);
				i++;
			} while (subspace.containsKey(prospectiveName));
		}
		
		subspace.put(prospectiveName, object);
		return prospectiveName;
	}
	
	/**
	 * Removes the named object from the <code>klass</code>-specific namespace.
	 * @param klass the {@link Class} whose namespace should be searched
	 * @param name a name
	 * @return the formerly-named object, if there was one, or
	 *         <code>null</code> if there wasn't
	 */
	public Object removeObject(Class<?> klass, String name) {
		return getSubspace(klass).remove(name);
	}
	
	/**
	 * Returns the name registered in <code>nm</code> for <code>nameable</code>.
	 * If it doesn't have a name, then <code>nameable.setName(null)</code> will
	 * be called to create one.
	 * @param klass the {@link Class} whose namespace should be used
	 * @param nameable an {@link INameable}
	 * @param nm a {@link NamespaceManager}
	 * @return the registered name of <code>nameable</code>
	 */
	public String getRequiredName(Class<?> klass, INameable nameable) {
		String name = getName(klass, nameable);
		if (name == null) {
			nameable.setName(null);
			name = getName(klass, nameable);
		}
		return name;
	}
}
