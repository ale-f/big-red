package org.bigraph.model.assistants;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.ModelObject.ModelObjectChange;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeValidator2;
import org.bigraph.model.names.Namespace;
import org.bigraph.model.names.policies.INamePolicy;

class ModelObjectValidator implements IChangeValidator2 {
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
	
	protected static <V extends ModelObjectChange> Callback makeCallback(
			final Process process, final V change,
			final ModelObject.FinalValidator<V> validator) {
		return new Callback() {
			@Override
			public void run() throws ChangeRejectedException {
				validator.finalValidate(change, process.getScratch());
			}
		};
	}
	
	protected <V extends ModelObjectChange> void doExternalValidation(
			Process process, V change, ModelObject.Validator<V> validator)
			throws ChangeRejectedException {
		if (validator != null) {
			validator.validate(change, process.getScratch());
			if (validator instanceof ModelObject.FinalValidator<?>)
				process.addCallback(makeCallback(process, change,
						(ModelObject.FinalValidator<V>)validator));
		}
	}
	
	@Override
	public boolean tryValidateChange(Process process, IChange change)
			throws ChangeRejectedException {
		final PropertyScratchpad context = process.getScratch();
		if (change instanceof ChangeExtendedData) {
			ChangeExtendedData c = (ChangeExtendedData)change;
			doExternalValidation(process, c, c.validator);
		} else return false;
		change.simulate(context);
		return true;
	}
}
