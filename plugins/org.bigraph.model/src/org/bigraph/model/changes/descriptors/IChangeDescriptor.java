package org.bigraph.model.changes.descriptors;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.Identifier;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.IChange;

/**
 * Classes implementing <strong>IChangeDescriptor</strong> are <i>change
 * descriptors</i>: objects that know how to construct an {@link IChange}.
 * They're essentially just changes which target {@link Identifier}s rather
 * than {@link ModelObject}s.
 * @author alec
 * @see IChange
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
	
	void simulate(PropertyScratchpad context, Resolver r)
			throws ChangeCreationException;
}