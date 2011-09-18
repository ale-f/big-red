package dk.itu.big_red.model.assistants;

import java.util.HashMap;
import java.util.Map.Entry;

import dk.itu.big_red.model.interfaces.internal.INameable;

/**
 * NamespaceManagers manage context-specific <i>namespaces</i>, groups of
 * unique-name-to-object mappings.
 * @author alec
 *
 */
public class NamespaceManager {
	private HashMap<Object, HashMap<INameable, String>> names =
		new HashMap<Object, HashMap<INameable, String>>();
	
	protected HashMap<INameable, String> getSubspace(Object context) {
		HashMap<INameable, String> subspace = names.get(context);
		if (subspace == null) {
			subspace = new HashMap<INameable, String>();
			names.put(context, subspace);
		}
		return subspace;
	}
	
	public void destroyContext(Object context) {
		if (names.containsKey(context))
			names.remove(context);
	}

	public String getName(Object context, INameable object) {
		return getSubspace(context).get(object);
	}
	
	public boolean hasName(Object context, INameable object) {
		return getSubspace(context).containsKey(object);
	}
	
	public boolean setName(Object context, INameable object, String name) {
		HashMap<INameable, String> subspace = getSubspace(context);
		if (subspace.containsValue(name))
			return false;
		
		if (hasName(context, object))
			removeName(context, object);
		
		subspace.put(object, name);
		return true;
	}
	
	public boolean removeName(Object context, INameable object) {
		return (getSubspace(context).remove(object) != null);
	}
	
	private final static String _IAS_ALPHA = "abcdefghijklmnopqrstuvwxyz";
	
	private static String intAsString(int x) {
		String s = "";
		boolean nonZeroEncountered = false;
		for (int i = 6; i >= 0; i--) {
			int y = (int)Math.pow(26, i);
			int z = x / y;

			if (z == 0 && !nonZeroEncountered && i != 0)
				continue;

			nonZeroEncountered = true;
			s += _IAS_ALPHA.charAt(z);

			x -= y * z;
		}
		return s;
	}
	
	public String requireName(Object context, INameable object) {
		String name = getName(context, object);
		if (name != null) {
			return name;
		} else {
			HashMap<INameable, String> subspace = getSubspace(context);
			int i = 0;
			switch (object.getNameType()) {
			case NAME_NUMERIC:
				do {
					name = Integer.toString(i++);
				} while (subspace.containsValue(name));
				break;
			case NAME_ALPHABETIC:
				do {
					name = intAsString(i++);
				} while (subspace.containsValue(name));
				break;
			default:
				break;
			}
			if (setName(context, object, name)) {
				System.out.println("Succeeded in giving " + object + " name \"" + name + "\" (context " + context + ")");
				return name;
			} else return null;
		}
	}
	
	public INameable getObject(Object context, String name) {
		HashMap<INameable, String> subspace = getSubspace(context);
		if (subspace.containsValue(name)) {
			for (Entry<INameable, String> i : subspace.entrySet())
				if (i.getValue().equals(name))
					return i.getKey();
			return null;
		} else return null;
	}
}
