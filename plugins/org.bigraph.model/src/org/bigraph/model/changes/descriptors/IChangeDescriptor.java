package org.bigraph.model.changes.descriptors;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;

/**
 * Classes implementing <strong>IChangeDescriptor</strong> are <i>change
 * descriptors</i>: reversible, validated changes to model objects.
 * @author alec
 */
public interface IChangeDescriptor {
	/**
	 * Classes implementing <strong>Group</strong> are collections of {@link
	 * IChangeDescriptor}s. They receive special treatment from {@link
	 * DescriptorValidatorManager}s and {@link DescriptorExecutorManager}s,
	 * which will never directly validate, execute, or simulate them.
	 * @author alec
	 */
	interface Group extends IChangeDescriptor, Iterable<IChangeDescriptor> {
		int size();
	}
	
	IChangeDescriptor inverse();
	
	void simulate(PropertyScratchpad context, Resolver r);
}