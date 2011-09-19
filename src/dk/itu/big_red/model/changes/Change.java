package dk.itu.big_red.model.changes;

/**
 * A Change is a reversible modification.
 * @author alec
 *
 */
public abstract class Change {
	/**
	 * Gets a new Change which, when applied, will reverse this Change.
	 * @return this Change's inverse
	 */
	public abstract Change inverse();
}
