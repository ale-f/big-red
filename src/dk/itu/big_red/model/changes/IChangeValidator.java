package dk.itu.big_red.model.changes;

/**
 * Objects implementing {@link IChangeValidator} can inspect, and possibly
 * reject, {@link Change}s.
 * @author alec
 *
 */
public interface IChangeValidator {
	/**
	 * Checks if the given {@link Change} can be applied to this {@link
	 * IChangeable}.
	 * @param b a {@link Change}
	 * @throws ChangeRejectedException if {@link #applyChange(Change)} will not
	 * be allowed
	 */
	public void tryValidateChange(Change b) throws ChangeRejectedException;
}
