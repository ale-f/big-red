package org.bigraph.model;

import java.util.List;

import org.bigraph.model.SimulationSpec.ChangeAddRule;
import org.bigraph.model.SimulationSpec.ChangeModel;
import org.bigraph.model.SimulationSpec.ChangeRemoveRule;
import org.bigraph.model.SimulationSpec.ChangeSignature;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;

final class SimulationSpecHandler implements IStepExecutor, IStepValidator {
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
		final PropertyScratchpad scratch = context.getScratch();
		if (b instanceof ChangeSignature ||
				b instanceof ChangeModel) {
		} else if (b instanceof ChangeAddRule) {
			ChangeAddRule c = (ChangeAddRule)b;
			SimulationSpec container = c.getCreator();
			
			List<? extends ReactionRule> siblings =
					container.getRules(scratch);
			if (c.position < -1 || c.position > siblings.size())
				throw new ChangeRejectedException(b,
						"" + c.position + " is not a valid position for " +
						c.rule);
		} else if (b instanceof ChangeRemoveRule) {
			ChangeRemoveRule c = (ChangeRemoveRule)b;
			List<? extends ReactionRule> siblings =
					c.getCreator().getRules(scratch);
			if (!siblings.contains(c.rule))
				throw new ChangeRejectedException(b,
						"" + c.rule + " is not present in " + c.getCreator());
		} else return false;
		return true;
	}
}
