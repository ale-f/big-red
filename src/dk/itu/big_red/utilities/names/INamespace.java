package dk.itu.big_red.utilities.names;

import dk.itu.big_red.utilities.ISafeCloneable;

public interface INamespace<T> extends ISafeCloneable {
	public boolean has(String key);
	public T get(String key);
	public boolean put(String key, T value);
	public boolean remove(String key);

	public String getNextName();
	
	public INamePolicy getPolicy();
	public INamespace<T> setPolicy(INamePolicy policy);
	
	@Override
	public INamespace<T> clone();
}
