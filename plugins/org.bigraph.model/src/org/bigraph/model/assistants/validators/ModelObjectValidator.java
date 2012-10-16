package org.bigraph.model.assistants.validators;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.ModelObject.ModelObjectChange;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeValidator;
import org.bigraph.model.changes.IChangeValidator2;
import org.bigraph.model.names.Namespace;
import org.bigraph.model.names.policies.INamePolicy;

public final class ModelObjectValidator
		implements IChangeValidator, IChangeValidator2 {
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
	public void tryValidateChange(IChange b) throws ChangeRejectedException {
		tryValidateChange((PropertyScratchpad)null, b);
	}
	
	public void tryValidateChange(PropertyScratchpad context, IChange change)
			throws ChangeRejectedException {
		ValidatorManager vm = new ValidatorManager();
		vm.addValidator(this);
		vm.addValidator(new BigraphValidator());
		vm.addValidator(new EditValidator());
		vm.addValidator(new SignatureValidator());
		if (!vm.tryValidateChange(context, change))
			throw new ChangeRejectedException(change,
					"The Change was not recognised by the validator");
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
