package org.bigraph.model.changes;

import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

/**
 * Classes implementing <strong>IChange</strong> are <i>changes</i>: reversible
 * and validated modifications to model objects. 
 * @author alec
 * @see IChangeDescriptor
 */
public interface IChange extends IChangeDescriptor {
	/**
	 * Classes implementing <strong>Group</strong> are collections of {@link
	 * IChange}s. They receive special treatment from {@link ValidatorManager}s
	 * and {@link ExecutorManager}s, which will never directly validate,
	 * execute, or simulate them.
	 * @author alec
	 */
	interface Group extends IChange, Iterable<IChange> {
		int size();
	}
	
	/**
	 * Gets a new {@link IChange} which, when applied, will reverse this one.
	 * <p><strong>Depending on the {@link IChange}, it is possible that this
	 * function will only have a meaningful result <i>after</i> this {@link
	 * IChange} has been applied. See {@link #canInvert()}.</strong>
	 * @return this IChange's inverse
	 * @see #canInvert()
	 */
	@Override
	IChange inverse();
	
	/**
	 * Simulates the execution of this {@link IChange} in the given {@link
	 * PropertyScratchpad}. (No validation is performed by this method.)
	 * @param context a {@link PropertyScratchpad} to populate with
	 * modifications
	 * @param resolver TODO
	 */
	@Override
	void simulate(PropertyScratchpad context, Resolver resolver);
}
