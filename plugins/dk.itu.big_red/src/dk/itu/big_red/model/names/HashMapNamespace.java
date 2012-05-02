package dk.itu.big_red.model.names;

import java.util.HashMap;

public class HashMapNamespace<T> extends Namespace<T> {
	private HashMap<String, T> map = new HashMap<String, T>();
	
	protected HashMap<String, T> getMap() {
		return map;
	}
	
	@Override
	protected T getRaw(String name) {
		return getMap().get(name);
	}

	@Override
	protected void putRaw(String name, T value) {
		getMap().put(name, value);
	}

	@Override
	protected boolean removeRaw(String name) {
		return (getMap().remove(name) != null);
	}
	
	@Override
	public HashMapNamespace<T> clone() {
		HashMapNamespace<T> copy = new HashMapNamespace<T>();
		copy.getMap().putAll(getMap());
		copy.setPolicy(getPolicy());
		return copy;
	}
}
