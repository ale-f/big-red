package dk.itu.big_red.model.changes;

/**
 * Objects implementing {@link IChangeable} can have {@link Change}s applied
 * to them to modify their state.
 * @author alec
 *
 */
public interface IChangeable extends IChangeValidator {
	/**
	 * Applies the given {@link Change} to this {@link IChangeable}.
	 * <p>This function will do nothing if the {@link Change} fails {@link
	 * #validateChange(Change) validation}.
	 * @param c a {@link Change}
	 */
	public void applyChange(Change b);
}
