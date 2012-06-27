package org.bigraph.model.names;

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
}
