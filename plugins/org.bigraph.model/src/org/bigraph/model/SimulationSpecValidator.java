package org.bigraph.model;

import org.bigraph.model.SimulationSpec.ChangeAddRule;
import org.bigraph.model.SimulationSpec.ChangeModel;
import org.bigraph.model.SimulationSpec.ChangeRemoveRule;
import org.bigraph.model.SimulationSpec.ChangeSignature;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepValidator;

class SimulationSpecValidator implements IStepValidator {
	@Override
	public boolean tryValidateChange(Process context, IChange b)
			throws ChangeRejectedException {
		if (b instanceof ChangeSignature ||
				b instanceof ChangeAddRule ||
				b instanceof ChangeRemoveRule ||
				b instanceof ChangeModel) {
			return true;
		} else return false;
	}
}
