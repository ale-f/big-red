package org.bigraph.model.changes;

/**
 * Objects implementing {@link IChangeExecutor} can have {@link IChange}s
 * applied to them to modify their state.
 * @author alec
 */
public interface IChangeExecutor {
	/**
	 * Checks if the given {@link IChange} can be applied to this {@link
	 * IChangeExecutor}.
	 * @param b a {@link IChange}
	 * @throws ChangeRejectedException if {@link #tryApplyChange(IChange)} will
	 * not be allowed
	 */
	void tryValidateChange(IChange b) throws ChangeRejectedException;
	
	/**
	 * Validates and applies the given {@link IChange} to this {@link
	 * IChangeExecutor}.
	 * @param b an {@link IChange}
	 * @throws ChangeRejectedException if {@link #tryValidateChange(IChange)}
	 * fails
	 */
	void tryApplyChange(IChange b) throws ChangeRejectedException;
}
