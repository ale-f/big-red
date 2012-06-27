package org.bigraph.model.names;

import java.util.HashMap;

public class NamespaceGroup<T> {
	private HashMap<Object, HashMapNamespace<T>> namespaces =
		new HashMap<Object, HashMapNamespace<T>>();
	
	public Namespace<T> createNamespace(Object i) {
		HashMapNamespace<T> namespace = null;
		if (i != null)
			namespaces.put(i, (namespace = new HashMapNamespace<T>()));
		return namespace;
	}
	
	public Namespace<T> getNamespace(Object i) {
		return namespaces.get(i);
	}
}
