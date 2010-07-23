package dk.itu.big_red.model;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

/**
 * NamespaceManagers manage {@link Class}-specific <i>namespaces</i>, groups of
 * unique-name-to-object mappings.
 * @author alec
 *
 */
public class NamespaceManager {
	private HashMap<Class<?>, HashMap<String, Object>> names =
		new HashMap<Class<?>, HashMap<String, Object>>();
	
	/**
	 * Returns the name given to a particular object.
	 * @param klass the {@link Class} whose namespace should be searched
	 * @param object an object
	 * @return the object's name, if it has one, or <code>null</code> otherwise
	 */
	public String getName(Class<?> klass, Object object) {
		HashMap<String, Object> subspace = names.get(klass);
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
		return names.get(klass).get(name);
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
		HashMap<String, Object> subspace = names.get(klass);
		
		Object current = subspace.get(name);
		if (current == object) // object already has name
			return true;
		else if (current != null) // name is already taken
			return false;
		
		String currentName = getName(klass, object);
		if (!currentName.equals(name)) // object already has different name
			subspace.remove(currentName);
		
		subspace.put(name, object);
		return true;
	}
	
	private static Random random = new Random();
	
	/**
	 * Finds a unique name for <code>object</code> in the
	 * <code>klass</code>-specific namespace, registers it with that name, and
	 * returns that name.
	 * @param klass the {@link Class} whose namespace should be searched
	 * @param object an object
	 * @return the unique name with which <code>object</code> was registered
	 *         (not necessarily randomly generated - it might already have had
	 *         one!)
	 */
	public String newName(Class<?> klass, Object object) {
		HashMap<String, Object> subspace = names.get(klass);
		
		String prospectiveName = getName(klass, object);
		if (prospectiveName != null)
			return prospectiveName;
		
		do {
			prospectiveName = new BigInteger(64, random).toString(36);
		} while (!subspace.containsKey(prospectiveName));
		
		subspace.put(prospectiveName, object);
		return prospectiveName;
	}
}
