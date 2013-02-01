package org.bigraph.model;

import org.bigraph.model.Control.ChangeKindDescriptor;
import org.bigraph.model.HandlerUtilities.DescriptorHandlerImpl;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

final class ControlDescriptorHandler extends DescriptorHandlerImpl {
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final PropertyScratchpad scratch = context.getScratch();
		final Resolver resolver = context.getResolver();
		if (change instanceof ChangeKindDescriptor) {
			ChangeKindDescriptor cd = (ChangeKindDescriptor)change;
			Control c = cd.getTarget().lookup(scratch, resolver);
			
			if (c == null)
				throw new ChangeCreationException(cd,
						"" + cd.getTarget() + ": lookup failed");
		} else return false;
		return true;
	}
	
	@Override
	public boolean executeChange(Resolver resolver, IChangeDescriptor change) {
		if (change instanceof ChangeKindDescriptor) {
			ChangeKindDescriptor cd = (ChangeKindDescriptor)change;
			cd.getTarget().lookup(null, resolver).setKind(cd.getNewValue());
		} else return false;
		return true;
	}
}
