package dk.itu.big_red.model.namespaces;

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
	 * @return <code>true</code> if the name is accepted, or <code>false</code>
	 * otherwise
	 */
	public boolean validate(String name);
	
	/**
	 * Returns a valid name corresponding to an integer.
	 * <p>There is no requirement that <i>all</i> names that would be accepted
	 * by this {@link INamePolicy} be returnable by this method, nor is there
	 * a requirement that <i>all</i> integers produce a unique name.
	 * @param value an integer specifying a name (must be &geq; 0)
	 * @return a name that would be accepted by this {@link INamePolicy}
	 */
	public String getName(int value);
}
