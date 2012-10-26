package org.bigraph.model;

import org.bigraph.model.NamedModelObject.ChangeName;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;

public class NamedModelObjectHandler implements IStepExecutor, IStepValidator {
	@Override
	public boolean executeChange(IChange change_) {
		if (change_ instanceof NamedModelObject.ChangeName) {
			ChangeName change = (ChangeName)change_;
			change.getCreator().applyRename(change.name);
		} else return false;
		return true;
	}

	@Override
	public boolean tryValidateChange(Process context, IChange change_)
			throws ChangeRejectedException {
		final PropertyScratchpad scratch = context.getScratch();
		if (change_ instanceof NamedModelObject.ChangeName) {
			ChangeName change = (ChangeName)change_;
			ModelObjectHandler.checkName(scratch, change, change.getCreator(),
					change.getCreator().getGoverningNamespace(scratch),
					change.name);
		} else return false;
		return true;
	}
}
