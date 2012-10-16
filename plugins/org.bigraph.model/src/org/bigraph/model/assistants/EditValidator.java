package org.bigraph.model.assistants;

import org.bigraph.model.Edit.ChangeDescriptorAdd;
import org.bigraph.model.Edit.ChangeDescriptorRemove;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeValidator2;

class EditValidator implements IChangeValidator2 {
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
