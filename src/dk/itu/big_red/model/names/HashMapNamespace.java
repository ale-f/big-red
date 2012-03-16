package dk.itu.big_red.model.names;

import java.util.HashMap;

public class HashMapNamespace<T> extends Namespace<T> {
	private HashMap<String, T> map = new HashMap<String, T>();
	
	protected HashMap<String, T> getMap() {
		return map;
	}
	
	@Override
	public T get(String name) {
		if ((name = checkName(name)) != null) {
			return getMap().get(name);
		} else return null;
	}

	@Override
	public String put(String name, T value) {
		if (value != null && (name = checkName(name)) != null && !has(name)) {
			getMap().put(name, value);
			return name;
		} else return null;
	}
	
	@Override
	public boolean remove(String name) {
		if ((name = checkName(name)) != null) {
			return (getMap().remove(name) != null);
		} else return false;
	}
	
	@Override
	public HashMapNamespace<T> clone() {
		HashMapNamespace<T> copy = new HashMapNamespace<T>();
		copy.getMap().putAll(getMap());
		copy.setPolicy(getPolicy());
		return copy;
	}
}
