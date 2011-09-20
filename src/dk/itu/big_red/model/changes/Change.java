package dk.itu.big_red.model.changes;

/**
 * A Change is a reversible modification.
 * @author alec
 *
 */
public abstract class Change {
	/**
	 * Gets a new {@link Change} which, when applied, will reverse this one.
	 * <p><strong>Depending on the {@link Change}, it is possible that this
	 * function will only have a meaningful result <i>after</i> this {@link
	 * Change} has been applied. See {@link #canInvert()}.</strong>
	 * @return this Change's inverse
	 * @see #canInvert()
	 */
	public abstract Change inverse();
	
	/**
	 * Indicates whether or not this {@link Change} needs more information to
	 * be reversible. For example, inverting the change "resize X to 40x40"
	 * requires knowledge of the size of X before the change was made. (How the
	 * additional information is supposed to be specified depends on the {@link
	 * Change}.)
	 * @return <code>true</code> if {@link #inverse()} will work, or
	 * <code>false</code> if more information is needed first
	 */
	public boolean canInvert() {
		return true /* by default; subclasses can override */;
	}
}
