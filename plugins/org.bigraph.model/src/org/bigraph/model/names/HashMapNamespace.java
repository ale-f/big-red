package org.bigraph.model.names;

import java.util.HashMap;

import org.bigraph.model.names.policies.INamePolicy;

public class HashMapNamespace<T> extends Namespace<T> {
	public HashMapNamespace() {
		super();
	}
	
	public HashMapNamespace(INamePolicy policy) {
		super(policy);
	}
	
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
