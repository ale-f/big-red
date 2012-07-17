package org.bigraph.model.assistants.validators;

import java.util.ArrayList;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.ModelObject.ExtendedDataValidator;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.changes.IChangeValidator;

abstract class ModelObjectValidator<T extends ModelObject & IChangeExecutor>
		implements IChangeValidator {
	private PropertyScratchpad scratch = new PropertyScratchpad();
	
	protected PropertyScratchpad getScratch() {
		return scratch;
	}
	
	private ArrayList<ChangeExtendedData> finalChecks =
			new ArrayList<ChangeExtendedData>();
	
	private final T changeExecutor;
	
	public ModelObjectValidator(T changeExecutor) {
		this.changeExecutor = changeExecutor;
	}

	protected T getChangeable() {
		return changeExecutor;
	}
	
	protected Change doValidateChange(Change b)
			throws ChangeRejectedException {
		if (!b.isReady()) {
			throw new ChangeRejectedException(b, "The Change is not ready");
		} else if (b instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)b)
				if ((c = doValidateChange(c)) != null)
					return c;
			/* All changes will have been individually simulated */
			return null;
		} else if (b instanceof ChangeExtendedData) {
			ChangeExtendedData c = (ChangeExtendedData)b;
			ExtendedDataValidator v = c.immediateValidator;
			if (v != null)
				v.validate(c, getScratch());
			if (c.finalValidator != null)
				finalChecks.add(c);
		} else return b;
		b.simulate(getScratch());
		return null;
	}
	
	@Override
	public void tryValidateChange(Change b) throws ChangeRejectedException {
		getScratch().clear();
		finalChecks.clear();
		
		b = doValidateChange(b);
		if (b != null)
			throw new ChangeRejectedException(b, "The change was not recognised by the validator");
		
		for (ChangeExtendedData i : finalChecks)
			i.finalValidator.validate(i, scratch);
	}
}
