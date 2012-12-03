package org.bigraph.model.changes.descriptors.experimental;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

public interface IDescriptorStepExecutor {
	boolean executeChange(Resolver resolver, IChangeDescriptor change_);
}
