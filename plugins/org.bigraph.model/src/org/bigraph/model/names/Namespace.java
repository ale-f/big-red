package org.bigraph.model.names;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.names.policies.INamePolicy;
import org.bigraph.model.names.policies.StringNamePolicy;

public abstract class Namespace<T> implements INamespace<T> {
	private final INamePolicy policy;
	
	public Namespace() {
		this(null);
	}
	
	public Namespace(INamePolicy policy) {
		this.policy = policy;
	}
	
	@SuppressWarnings("unchecked")
	protected T getProperty(PropertyScratchpad context, String name) {
		if (context != null && context.hasProperty(this, name)) {
			return (T)context.getProperty(this, name);
		} else return getRaw(name);
	}
	
	protected void putProperty(
			PropertyScratchpad context, String name, T value) {
		if (context == null) {
			putRaw(name, value);
		} else context.setProperty(this, name, value);
	}
	
	protected boolean removeProperty(PropertyScratchpad context, String name) {
		if (context == null) {
			return removeRaw(name);
		} else {
			boolean success = has(context, name);
			if (success)
				context.setProperty(this, name, null);
			return success;
		}
	}
	
	protected abstract T getRaw(String name);
	protected abstract void putRaw(String name, T value);
	protected abstract boolean removeRaw(String name);
	
	@Override
	public boolean has(String key) {
		return has(null, key);
	}
	
	public boolean has(PropertyScratchpad context, String key) {
		return (get(context, key) != null);
	}
	
	@Override
	public T get(String name) {
		return get(null, name);
	}

	public T get(PropertyScratchpad context, String name) {
		if ((name = checkName(name)) != null) {
			return getProperty(context, name);
		} else return null;
	}
	
	@Override
	public String put(String name, T value) {
		return put(null, name, value);
	}
	
	public String put(PropertyScratchpad context, String name, T value) {
		if (value != null && (name = checkName(name)) != null &&
				!has(context, name)) {
			putProperty(context, name, value);
			return name;
		} else return null;
	}
	
	@Override
	public String rename(String name, String newName) {
		return rename(null, name, newName);
	}
	
	public String rename(
			PropertyScratchpad context, String name, String newName) {
		name = checkName(name);
		newName = checkName(newName);
		if ((name != null ? name.equals(newName) : name == newName) &&
				has(context, name))
			return newName;
		if (name != null && newName != null &&
				has(context, name) && !has(context, newName)) {
			T value = getProperty(context, name);
			removeProperty(context, name);
			putProperty(context, newName, value);
			return newName;
		} else return null;
	}
	
	@Override
	public boolean remove(String name) {
		return remove(null, name);
	}

	public boolean remove(PropertyScratchpad context, String name) {
		if ((name = checkName(name)) != null) {
			return removeProperty(context, name);
		} else return false;
	}
	
	@Override
	public String getNextName() {
		return getNextName(null);
	}
	
	public String getNextName(PropertyScratchpad context) {
		INamePolicy policy = getPolicy();
		if (policy == null)
			policy = new StringNamePolicy();
		
		int i = 0;
		String name;
		do {
			name = policy.get(i++);
		} while (has(context, name));
		return name;
	}
	
	@Override
	public INamePolicy getPolicy() {
		return policy;
	}

	protected String checkName(String name) {
		if (name != null) {
			if (getPolicy() == null) {
				return name;
			} else return getPolicy().normalise(name);
		} else return null;
	}
}
