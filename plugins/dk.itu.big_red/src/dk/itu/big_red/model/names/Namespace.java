package dk.itu.big_red.model.names;

public abstract class Namespace<T> implements INamespace<T> {
	@Override
	public boolean has(String key) {
		return (get(key) != null);
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
