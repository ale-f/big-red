package org.bigraph.model.changes;

/**
 * Objects implementing {@link IChangeExecutor} can have {@link Change}s applied
 * to them to modify their state.
 * @author alec
 *
 */
public interface IChangeExecutor extends IChangeValidator {
	/**
	 * Validates and applies the given {@link Change} to this {@link
	 * IChangeExecutor}.
	 * @param b a {@link Change}
	 * @throws ChangeRejectedException if {@link #tryValidateChange(Change)}
	 * fails
	 */
	void tryApplyChange(Change b) throws ChangeRejectedException;
}
