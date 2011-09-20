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
	 * IChangeable}. If this function returns <code>false</code>, then the
	 * rejection object will be available from {@link #getLastRejection()}.
	 * <p>(This function is usually implemented as a wrapper around
	 * {@link #tryValidateChange(Change)}.)
	 * @param c a {@link Change}
	 * @return <code>true</code> if {@link #applyChange(Change)} will be
	 * allowed, or <code>false</code> otherwise
	 */
	public boolean validateChange(Change b);
	
	/**
	 * Returns the most recent {@link ChangeRejectedException} generated while
	 * executing {@link #validateChange(Change)}.
	 * @return a {@link ChangeRejectedException} explaining the rejection
	 */
	public ChangeRejectedException getLastRejection();
	
	/**
	 * Checks if the given {@link Change} can be applied to this {@link
	 * IChangeable}.
	 * @param c a {@link Change}
	 * @throws ChangeRejectedException if {@link #applyChange(Change)} will not
	 * be allowed
	 */
	public void tryValidateChange(Change b) throws ChangeRejectedException;
}
