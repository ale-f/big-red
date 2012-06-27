package org.bigraph.model.names.policies;

import org.bigraph.model.names.INamespace;

/**
 * Classes implementing <strong>INamePolicy</strong> are name validators and
 * generators, used by {@link INamespace}s to check and create names.
 * @author alec
 * @see INamespace
 */
public interface INamePolicy {
	/**
	 * Indicates whether or not this {@link INamePolicy} accepts the given
	 * name.
	 * @param name a name to validate
	 * @return the normalised form of <code>name</code> if it is accepted, or
	 * <code>null</code> otherwise
	 */
	String normalise(String name);
	
	/**
	 * Returns a valid name corresponding to an integer.
	 * <p>There is no requirement that <i>all</i> names that would be accepted
	 * by this {@link INamePolicy} be returnable by this method, nor is there
	 * a requirement that <i>all</i> integers produce a unique name.
	 * @param value an integer specifying a name
	 * @return a name that would be accepted by this {@link INamePolicy}
	 */
	String get(int value);
}
