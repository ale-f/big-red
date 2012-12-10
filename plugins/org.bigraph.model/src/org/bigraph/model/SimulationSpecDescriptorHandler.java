package org.bigraph.model;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.SimulationSpec.ChangeAddRuleDescriptor;
import org.bigraph.model.SimulationSpec.ChangeRemoveRuleDescriptor;
import org.bigraph.model.SimulationSpec.ChangeSetModelDescriptor;
import org.bigraph.model.SimulationSpec.ChangeSetSignatureDescriptor;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.experimental.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.experimental.IDescriptorStepValidator;

final class SimulationSpecDescriptorHandler implements
		IDescriptorStepExecutor, IDescriptorStepValidator {
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final Resolver resolver = context.getResolver();
		final PropertyScratchpad scratch = context.getScratch();
		if (change instanceof ChangeSetModelDescriptor) {
			ChangeSetModelDescriptor cd = (ChangeSetModelDescriptor)change;
			SimulationSpec ss = cd.getTarget().lookup(scratch, resolver);
			if (ss == null)
				throw new ChangeCreationException(cd,
						"" + cd.getTarget() + ": lookup failed");
		} else if (change instanceof ChangeSetSignatureDescriptor) {
			ChangeSetSignatureDescriptor cd =
					(ChangeSetSignatureDescriptor)change;
			SimulationSpec ss = cd.getTarget().lookup(scratch, resolver);
			if (ss == null)
				throw new ChangeCreationException(cd,
						"" + cd.getTarget() + ": lookup failed");
		} else if (change instanceof ChangeAddRuleDescriptor) {
			ChangeAddRuleDescriptor cd = (ChangeAddRuleDescriptor)change;
			SimulationSpec ss = cd.getTarget().lookup(scratch, resolver);
			if (ss == null)
				throw new ChangeCreationException(cd,
						"" + cd.getTarget() + ": lookup failed");
		} else if (change instanceof ChangeRemoveRuleDescriptor) {
			ChangeRemoveRuleDescriptor cd = (ChangeRemoveRuleDescriptor)change;
			SimulationSpec ss = cd.getTarget().lookup(scratch, resolver);
			if (ss == null)
				throw new ChangeCreationException(cd,
						"" + cd.getTarget() + ": lookup failed");
		} else return false;
		return true;
	}

	@Override
	public boolean executeChange(Resolver resolver, IChangeDescriptor change) {
		if (change instanceof ChangeSetModelDescriptor) {
			ChangeSetModelDescriptor cd = (ChangeSetModelDescriptor)change;
			cd.getTarget().lookup(null, resolver).setModel(cd.getNewModel());
		} else if (change instanceof ChangeSetSignatureDescriptor) {
			ChangeSetSignatureDescriptor cd =
					(ChangeSetSignatureDescriptor)change;
			cd.getTarget().lookup(null, resolver).setSignature(
					cd.getNewSignature());
		} else if (change instanceof ChangeAddRuleDescriptor) {
			ChangeAddRuleDescriptor cd = (ChangeAddRuleDescriptor)change;
			cd.getTarget().lookup(null, resolver).addRule(
					cd.getPosition(), cd.getRule());
		} else if (change instanceof ChangeRemoveRuleDescriptor) {
			ChangeRemoveRuleDescriptor cd = (ChangeRemoveRuleDescriptor)change;
			cd.getTarget().lookup(null, resolver).removeRule(cd.getRule());
		} else return false;
		return true;
	}
}
