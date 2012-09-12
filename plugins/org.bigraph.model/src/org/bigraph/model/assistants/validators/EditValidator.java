package org.bigraph.model.assistants.validators;

import org.bigraph.model.Edit;
import org.bigraph.model.Edit.ChangeDescriptorAdd;
import org.bigraph.model.Edit.ChangeDescriptorRemove;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;

public class EditValidator extends ModelObjectValidator<Edit> {
	public EditValidator(Edit changeExecutor) {
		super(changeExecutor);
	}

	@Override
	protected IChange doValidateChange(IChange b)
			throws ChangeRejectedException {
		if (super.doValidateChange(b) == null) {
			return null;
		} else if (b instanceof ChangeDescriptorAdd) {
			/* do nothing, yet */
		} else if (b instanceof ChangeDescriptorRemove) {
			/* do nothing, yet */
		} else return b;
		b.simulate(getScratch());
		return null;
	}
}
