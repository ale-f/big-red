package org.bigraph.model;

import org.bigraph.model.NamedModelObject.ChangeName;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;
import org.bigraph.model.names.Namespace;
import org.bigraph.model.names.policies.INamePolicy;

final class NamedModelObjectHandler implements IStepExecutor, IStepValidator {
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
			checkName(scratch, change, change.getCreator(),
					change.getCreator().getGoverningNamespace(scratch),
					change.name);
		} else return false;
		return true;
	}

	protected static <V> void checkName(
			PropertyScratchpad context, IChange c, V object,
			Namespace<? extends V> ns, String cdt)
			throws ChangeRejectedException {
		if (cdt == null || cdt.length() == 0)
			throw new ChangeRejectedException(c, "Names cannot be empty");
		if (ns == null)
			return;
		INamePolicy p = ns.getPolicy();
		String mcdt = (p != null ? p.normalise(cdt) : cdt);
		if (mcdt == null)
			throw new ChangeRejectedException(c,
					"\"" + cdt + "\" is not a valid name for " + object);
		V current = ns.get(context, mcdt);
		if (current != null && current != object)
			throw new ChangeRejectedException(c, "Names must be unique");
	}
}
