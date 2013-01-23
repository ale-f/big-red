package org.bigraph.model.changes;

import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

/**
 * Classes implementing <strong>IChange</strong> are <i>changes</i>: reversible
 * and validated modifications to model objects. 
 * @author alec
 * @see IChangeDescriptor
 */
public interface IChange {
	/**
	 * Gets a new {@link IChange} which, when applied, will reverse this one.
	 * <p><strong>Depending on the {@link IChange}, it is possible that this
	 * function will only have a meaningful result <i>after</i> this {@link
	 * IChange} has been applied. See {@link #canInvert()}.</strong>
	 * @return this IChange's inverse
	 * @see #canInvert()
	 */
	IChange inverse();
	
	/**
	 * Indicates whether or not this {@link IChange} needs more information to
	 * be reversible. For example, inverting the change "resize X to 40x40"
	 * requires knowledge of the size of X before the change was made.
	 * @return <code>true</code> if {@link #inverse()} will work, or
	 * <code>false</code> if more information is needed first
	 * @see #beforeApply()
	 */
	boolean canInvert();
	
	/**
	 * Called by {@link ExecutorManager} just before this {@link IChange} is
	 * applied.
	 * <p>(Subclasses should override this method if they need to save some
	 * properties of an object before a change in order to be able to {@link
	 * #inverse() reverse} it.)
	 */
	void beforeApply();
	
	/**
	 * Indicates whether or not this {@link IChange} has all the information it
	 * needs to be applied.
	 * @return <code>true</code> if this {@link IChange} is ready to apply
	 */
	boolean isReady();
	
	/**
	 * Simulates the execution of this {@link IChange} in the given {@link
	 * PropertyScratchpad}. (No validation is performed by this method.)
	 * @param context a {@link PropertyScratchpad} to populate with
	 * modifications
	 */
	void simulate(PropertyScratchpad context);
}
