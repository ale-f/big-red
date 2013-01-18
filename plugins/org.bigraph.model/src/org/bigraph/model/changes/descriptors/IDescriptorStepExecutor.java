package org.bigraph.model.changes.descriptors;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.process.IParticipant;

public interface IDescriptorStepExecutor extends IParticipant {
	boolean executeChange(Resolver resolver, IChangeDescriptor change_);
}
