package org.bigraph.model.changes.descriptors;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.Identifier;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.Change;

public interface IChangeDescriptor {
	Change createChange(PropertyScratchpad context, Resolver r);
}