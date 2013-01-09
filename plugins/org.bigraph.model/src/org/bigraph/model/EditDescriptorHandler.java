package org.bigraph.model;

import org.bigraph.model.Edit.ChangeDescriptorAddDescriptor;
import org.bigraph.model.Edit.ChangeDescriptorRemoveDescriptor;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.experimental.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.experimental.IDescriptorStepValidator;

final class EditDescriptorHandler
		implements IDescriptorStepExecutor, IDescriptorStepValidator {
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final PropertyScratchpad scratch = context.getScratch();
		final Resolver resolver = context.getResolver();
		if (change instanceof ChangeDescriptorAddDescriptor) {
			ChangeDescriptorAddDescriptor cd =
					(ChangeDescriptorAddDescriptor)change;
			Edit edit = cd.getTarget().lookup(scratch, resolver);
			
			if (edit == null)
				throw new ChangeCreationException(cd,
						"" + cd.getTarget() + ": lookup failed");
			
			if (cd.getDescriptor() == null)
				throw new ChangeCreationException(cd,
						"Can't insert a null change descriptor");
			
			int position = cd.getPosition();
			if (position < 0 ||
					position > edit.getDescriptors(scratch).size())
				throw new ChangeCreationException(cd,
						"" + position + " is not a valid position");
		} else if (change instanceof ChangeDescriptorRemoveDescriptor) {
			ChangeDescriptorRemoveDescriptor cd =
					(ChangeDescriptorRemoveDescriptor)change;
			Edit edit = cd.getTarget().lookup(scratch, resolver);
			
			if (edit == null)
				throw new ChangeCreationException(cd,
						"" + cd.getTarget() + ": lookup failed");
			
			ChangeDescriptorGroup cdg = edit.getDescriptors(scratch);
			int position = cd.getPosition();
			if (position < 0 || position >= cdg.size())
				throw new ChangeCreationException(cd,
						"" + position + " is not a valid position");
			
			if (!cdg.get(position).equals(cd.getDescriptor()))
				throw new ChangeCreationException(cd,
						"" + cd.getDescriptor() +
						" is not at position " + position);
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
