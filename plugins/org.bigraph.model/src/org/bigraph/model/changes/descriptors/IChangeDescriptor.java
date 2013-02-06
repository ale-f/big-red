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
	interface Group extends IChangeDescriptor, Iterable<IChangeDescriptor> {}
	
	IChangeDescriptor inverse();
	
	void simulate(PropertyScratchpad context, Resolver r)
			throws ChangeCreationException;
}