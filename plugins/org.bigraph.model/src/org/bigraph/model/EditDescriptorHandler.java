package org.bigraph.model;

import org.bigraph.model.Edit.ChangeDescriptorAddDescriptor;
import org.bigraph.model.Edit.ChangeDescriptorRemoveDescriptor;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

final class EditDescriptorHandler
		extends HandlerUtilities.DescriptorHandlerImpl {
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final PropertyScratchpad scratch = context.getScratch();
		final Resolver resolver = context.getResolver();
		if (change instanceof ChangeDescriptorAddDescriptor) {
			ChangeDescriptorAddDescriptor cd =
					(ChangeDescriptorAddDescriptor)change;
			Edit edit = tryLookup(cd,
					cd.getTarget(), scratch, resolver, Edit.class);
			
			if (cd.getDescriptor() == null)
				throw new ChangeCreationException(cd,
						"Can't insert a null change descriptor");
			
			HandlerUtilities.checkAddBounds(
					cd, edit.getDescriptors(scratch), cd.getPosition());
		} else if (change instanceof ChangeDescriptorRemoveDescriptor) {
			ChangeDescriptorRemoveDescriptor cd =
					(ChangeDescriptorRemoveDescriptor)change;
			Edit edit = tryLookup(cd,
					cd.getTarget(), scratch, resolver, Edit.class);
			
			HandlerUtilities.checkRemove(cd,
					edit.getDescriptors(scratch),
					cd.getDescriptor(), cd.getPosition());
		} else return false;
		return true;
	}

	@Override
	public boolean executeChange(Resolver resolver, IChangeDescriptor change) {
		if (change instanceof ChangeDescriptorAddDescriptor) {
			ChangeDescriptorAddDescriptor cd =
					(ChangeDescriptorAddDescriptor)change;
			Edit edit = cd.getTarget().lookup(null, resolver);
			edit.addDescriptor(cd.getPosition(), cd.getDescriptor());
		} else if (change instanceof ChangeDescriptorRemoveDescriptor) {
			ChangeDescriptorRemoveDescriptor cd =
					(ChangeDescriptorRemoveDescriptor)change;
			Edit edit = cd.getTarget().lookup(null, resolver);
			edit.removeDescriptor(cd.getPosition());
		} else return false;
		return true;
	}
}
