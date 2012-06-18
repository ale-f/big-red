package org.bigraph.model.names;

import org.bigraph.model.names.policies.INamePolicy;

/**
 * Classes implementing <strong>INamespace</strong> are <i>namespaces</i>,
 * maps from {@link String}s to another type.
 * @author alec
 * @param <T> the target of the map
 * @see INamePolicy
 */
public interface INamespace<T> {
	/**
	 * Indicates whether or not the given name is valid and in use.
	 * @param name a name
	 * @return <code>true</code> if the name is valid and in use, or
	 * <code>false</code> otherwise
	 */
	public boolean has(String name);
	
	/**
	 * Returns the object in this {@link INamespace} with the given name, if
	 * there is one.
	 * @param name a name, which must be valid and in use
	 * @return an object, or <code>null</code>
	 */
	public T get(String name);
	
	/**
	 * Adds an object to this {@link INamespace} with the given name.
	 * @param name a name, which must be valid and not already in use
	 * @param value an object, which must not be <code>null</code>
	 * @return the normalised form of <code>name</code> if the object was
	 * added, or <code>null</code> otherwise
	 */
	public String put(String name, T value);
	
	/**
	 * Removes a mapping from this {@link INamespace}.
	 * @param name a name, which must be valid and in use
	 * @return <code>true</code> if the mapping was removed, or
	 * <code>false</code> otherwise
	 */
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
