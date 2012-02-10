package dk.itu.big_red.utilities.names;

public abstract class Namespace<T> implements INamespace<T> {
	@Override
	public boolean has(String key) {
		return (get(key) != null);
	}

	@Override
	public String getNextName() {
		int i = 0;
		INamePolicy policy = getPolicy();
		String name;
		do {
			name = policy.getName(i++);
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

	protected boolean checkName(String name) {
		return (name != null &&
					(getPolicy() == null || getPolicy().validate(name)));
	}
	
	@Override
	public abstract Namespace<T> clone();
}
