package org.bigraph.model.assistants.validators;

import org.bigraph.model.Edit;
import org.bigraph.model.Edit.ChangeDescriptorAdd;
import org.bigraph.model.Edit.ChangeDescriptorRemove;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;

public class EditValidator extends ModelObjectValidator {
	public EditValidator(Edit edit) {
	}

	@Override
	public boolean tryValidateChange(Process process, IChange b)
			throws ChangeRejectedException {
		final PropertyScratchpad context = process.getScratch();
		if (super.tryValidateChange(process, b)) {
			return true;
		} else if (b instanceof ChangeDescriptorAdd) {
			/* do nothing, yet */
		} else if (b instanceof ChangeDescriptorRemove) {
			/* do nothing, yet */
		} else return false;
		b.simulate(context);
		return true;
	}
}
