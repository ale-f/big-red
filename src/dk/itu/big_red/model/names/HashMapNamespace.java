package dk.itu.big_red.model.names;

import java.util.HashMap;

public class HashMapNamespace<T> extends Namespace<T> {
	private HashMap<String, T> map = new HashMap<String, T>();
	
	@Override
	public T get(String name) {
		if ((name = checkName(name)) != null) {
			return map.get(name);
		} else return null;
	}

	@Override
	public String put(String name, T value) {
		if (value != null && (name = checkName(name)) != null && !has(name)) {
			map.put(name, value);
			return name;
		} else return null;
	}
	
	@Override
	public boolean remove(String name) {
		if ((name = checkName(name)) != null) {
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
