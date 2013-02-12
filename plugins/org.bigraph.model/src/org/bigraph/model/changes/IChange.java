package org.bigraph.model.changes;

import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

/**
 * Classes implementing <strong>IChange</strong> are <i>changes</i>: reversible
 * and validated modifications to model objects. 
 * @author alec
 * @see IChangeDescriptor
 */
@Deprecated
public interface IChange extends IChangeDescriptor {
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
