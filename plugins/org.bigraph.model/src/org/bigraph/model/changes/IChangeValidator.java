package org.bigraph.model.changes;

/**
 * Objects implementing {@link IChangeValidator} can inspect, and possibly
 * reject, {@link IChange}s.
 * @author alec
 */
public interface IChangeValidator {
	/**
	 * Checks if the given {@link IChange} can be applied to this {@link
	 * IChangeExecutor}.
	 * @param b a {@link IChange}
	 * @throws ChangeRejectedException if {@link #tryApplyChange(IChange)} will
	 * not be allowed
	 */
	void tryValidateChange(IChange b) throws ChangeRejectedException;
}
