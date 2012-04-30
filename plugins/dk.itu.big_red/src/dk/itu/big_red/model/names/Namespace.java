package dk.itu.big_red.model.names;

import dk.itu.big_red.model.names.policies.INamePolicy;

public abstract class Namespace<T> implements INamespace<T> {
	protected abstract T getRaw(String name);
	protected abstract void putRaw(String name, T value);
	protected abstract boolean removeRaw(String name);
	
	@Override
	public boolean has(String key) {
		return (get(key) != null);
	}
	
	@Override
	public T get(String name) {
		if ((name = checkName(name)) != null) {
			return getRaw(name);
		} else return null;
	}

	@Override
	public String put(String name, T value) {
		if (value != null && (name = checkName(name)) != null && !has(name)) {
			putRaw(name, value);
			return name;
		} else return null;
	}
	
	@Override
	public boolean remove(String name) {
		if ((name = checkName(name)) != null) {
			return removeRaw(name);
		} else return false;
	}

	@Override
	public String getNextName() {
		INamePolicy policy = getPolicy();
		if (policy == null)
			return null;
		
		int i = 0;
		String name;
		do {
			name = policy.get(i++);
		} while (has(name));
		return name;
	}
	
	private INamePolicy policy;
	
	@Override
	public INamePolicy getPolicy() {
		return policy;
	}

	@Override
	public INamespace<T> setPolicy(INamePolicy policy) {
		this.policy = policy;
		return this;
	}

	protected String checkName(String name) {
		if (name != null) {
			if (getPolicy() == null) {
				return name;
			} else return getPolicy().normalise(name);
		} else return null;
	}
	
	@Override
	public abstract Namespace<T> clone();
}
