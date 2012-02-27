package dk.itu.big_red.model.namespaces;

import java.util.HashMap;

public class HashMapNamespace<T> extends Namespace<T> {
	private HashMap<String, T> map = new HashMap<String, T>();
	
	@Override
	public T get(String name) {
		if (checkName(name)) {
			return map.get(name);
		} else return null;
	}

	@Override
	public boolean put(String name, T value) {
		if (value != null && checkName(name) && !has(name)) {
			map.put(name, value);
			return true;
		} else return false;
	}
	
	@Override
	public boolean remove(String name) {
		if (checkName(name)) {
			return (map.remove(name) != null);
		} else return false;
	}
	
	@Override
	public HashMapNamespace<T> clone() {
		HashMapNamespace<T> copy = new HashMapNamespace<T>();
		copy.map.putAll(map);
		copy.setPolicy(getPolicy());
		return copy;
	}
}
