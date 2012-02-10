package dk.itu.big_red.utilities.names;

import java.util.HashMap;

public class HashMapNamespace<T> extends Namespace<T> {
	private HashMap<String, T> map = new HashMap<String, T>();
	
	@Override
	public T get(String key) {
		if (checkName(key)) {
			return map.get(key);
		} else return null;
	}

	@Override
	public boolean put(String key, T value) {
		if (checkName(key) && !has(key)) {
			map.put(key, value);
			return true;
		} else return false;
	}
	
	@Override
	public boolean remove(String key) {
		if (checkName(key)) {
			return (map.remove(key) != null);
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
