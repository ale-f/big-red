package dk.itu.big_red.utilities.names;

/**
 * Classes implementing <strong>INamespace</strong> are <i>namespaces</i>,
 * maps from {@link String}s to another type.
 * @author alec
 * @param <T> the target of the map
 * @see INamePolicy
 */
public interface INamespace<T> {
	public boolean has(String name);
	public T get(String name);
	public boolean put(String name, T value);
	public boolean remove(String name);

	/**
	 * Returns a valid name not presently in use in this {@link INamespace}.
	 * @see #getPolicy()
	 * @return a valid name, or <code>null</code> if no name policy is set
	 */
	public String getNextName();
	
	/**
	 * Returns this {@link INamespace}'s name policy. The name policy decides
	 * which names this namespace will accept as valid, and is also used by
	 * {@link #getNextName()} to return new valid names.
	 * @return an {@link INamePolicy} (can be <code>null</code>)
	 */
	public INamePolicy getPolicy();
	
	/**
	 * Sets this {@link INamespace}'s name policy. Setting the name policy to
	 * <code>null</code> will cause this namespace to accept all names &mdash;
	 * apart from <code>null</code> &mdash; as valid (but see {@link
	 * #getNextName()}).
	 * <p>(Note that <i>all</i> uses of a name are validated by the name
	 * policy, including {@link #get(String)}; in particular, note that
	 * switching to a less permissive name policy may cause existing mappings
	 * to become hidden.)
	 * @param policy an {@link INamePolicy} (can be <code>null</code>)
	 * @return <code>this</code>, for convenience
	 * @see #getPolicy()
	 */
	public INamespace<T> setPolicy(INamePolicy policy);
	
	public INamespace<T> clone();
}
