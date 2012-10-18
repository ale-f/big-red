package org.bigraph.model;

import org.bigraph.model.Edit.ChangeDescriptorAdd;
import org.bigraph.model.Edit.ChangeDescriptorRemove;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;

final class EditHandler implements IStepExecutor, IStepValidator {
	@Override
	public boolean executeChange(IChange c_) {
		if (c_ instanceof ChangeDescriptorAdd) {
			ChangeDescriptorAdd c = (ChangeDescriptorAdd)c_;
			c.getCreator().addDescriptor(c.index, c.descriptor);
		} else if (c_ instanceof ChangeDescriptorRemove) {
			ChangeDescriptorRemove c = (ChangeDescriptorRemove)c_;
			c.getCreator().removeDescriptor(c.index);
		} else return false;
		return true;
	}
	
	@Override
	public boolean tryValidateChange(Process process, IChange b)
			throws ChangeRejectedException {
		if (b instanceof ChangeDescriptorAdd) {
			/* do nothing, yet */
		} else if (b instanceof ChangeDescriptorRemove) {
			/* do nothing, yet */
		} else return false;
		return true;
	}
}
