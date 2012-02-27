package dk.itu.big_red.model.namespaces;

import java.util.HashMap;
import java.util.Map.Entry;

public class NamespaceGroup<T> {
	private HashMap<Object, HashMapNamespace<T>> namespaces =
		new HashMap<Object, HashMapNamespace<T>>();
	
	public INamespace<T> createNamespace(Object i) {
		HashMapNamespace<T> namespace = null;
		if (i != null)
			namespaces.put(i, (namespace = new HashMapNamespace<T>()));
		return namespace;
	}
	
	public INamespace<T> getNamespace(Object i) {
		return namespaces.get(i);
	}
	
	@Override
	public NamespaceGroup<T> clone() {
		NamespaceGroup<T> r = new NamespaceGroup<T>();
		for (Entry<Object, HashMapNamespace<T>> i : namespaces.entrySet())
			r.namespaces.put(i.getKey(), i.getValue().clone());
		return r;
	}
}
