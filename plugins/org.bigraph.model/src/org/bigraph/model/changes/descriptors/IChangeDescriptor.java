package org.bigraph.model.changes.descriptors;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.IChange;

public interface IChangeDescriptor {
	IChange createChange(PropertyScratchpad context, Resolver r)
			throws ChangeCreationException;
}