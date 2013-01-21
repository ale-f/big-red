package org.bigraph.model;

import org.bigraph.model.ModelObject.ChangeMoveExtendedDataDescriptor;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

final class ModelObjectDescriptorHandler
		extends HandlerUtilities.DescriptorHandlerImpl {
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final PropertyScratchpad scratch = context.getScratch();
		final Resolver resolver = context.getResolver();
		if (change instanceof ChangeMoveExtendedDataDescriptor) {
			ChangeMoveExtendedDataDescriptor cd =
					(ChangeMoveExtendedDataDescriptor)change;
			ModelObject
				source = cd.getSource().lookup(scratch, resolver),
				target = cd.getTarget().lookup(scratch, resolver);
			if (source == null)
				throw new ChangeCreationException(cd,
						"" + cd.getSource() + ": lookup failed");
			if (target == null)
				throw new ChangeCreationException(cd,
						"" + cd.getTarget() + ": lookup failed");
			
			if (!target.getExtendedDataMap(scratch).isEmpty())
				throw new ChangeCreationException(cd,
						"" + cd.getTarget() +
						" mustn't have any extended data");
		} else return false;
		return true;
	}

	@Override
	public boolean executeChange(Resolver resolver, IChangeDescriptor change) {
		if (change instanceof ChangeMoveExtendedDataDescriptor) {
			ChangeMoveExtendedDataDescriptor cd =
					(ChangeMoveExtendedDataDescriptor)change;
			
			ModelObject
				source = cd.getSource().lookup(null, resolver),
				target = cd.getTarget().lookup(null, resolver);
			
			source.getExtendedDataMap().putAll(
					target.getExtendedDataMap());
			target.getExtendedDataMap().clear();
		} else return false;
		return true;
	}
}
