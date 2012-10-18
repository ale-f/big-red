package org.bigraph.model;

import org.bigraph.model.SimulationSpec.ChangeAddRule;
import org.bigraph.model.SimulationSpec.ChangeModel;
import org.bigraph.model.SimulationSpec.ChangeRemoveRule;
import org.bigraph.model.SimulationSpec.ChangeSignature;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;

class SimulationSpecHandler implements IStepExecutor, IStepValidator {
	@Override
	public boolean executeChange(IChange b) {
		if (b instanceof ChangeSignature) {
			ChangeSignature c = (ChangeSignature)b;
			c.getCreator().setSignature(c.signature);
		} else if (b instanceof ChangeAddRule) {
			ChangeAddRule c = (ChangeAddRule)b;
			c.getCreator().addRule(c.position, c.rule);
		} else if (b instanceof ChangeRemoveRule) {
			ChangeRemoveRule c = (ChangeRemoveRule)b;
			c.getCreator().removeRule(c.rule);
		} else if (b instanceof ChangeModel) {
			ChangeModel c = (ChangeModel)b;
			c.getCreator().setModel(c.model);
		} else return false;
		return true;
	}
	
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
