package org.bigraph.model.changes.descriptors;

import org.bigraph.model.ModelObject.Identifier.Resolver;

public interface IDescriptorStepExecutor {
	boolean executeChange(Resolver resolver, IChangeDescriptor change_);
}
