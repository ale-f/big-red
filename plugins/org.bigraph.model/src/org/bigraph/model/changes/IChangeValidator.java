package org.bigraph.model.changes;

/**
 * Objects implementing {@link IChangeValidator} can inspect, and possibly
 * reject, {@link Change}s.
 * @author alec
 *
 */
public interface IChangeValidator {
	/**
	 * Checks if the given {@link Change} can be applied to this {@link
	 * IChangeExecutor}.
	 * @param b a {@link Change}
	 * @throws ChangeRejectedException if {@link #applyChange(Change)} will not
	 * be allowed
	 */
	void tryValidateChange(Change b) throws ChangeRejectedException;
}
