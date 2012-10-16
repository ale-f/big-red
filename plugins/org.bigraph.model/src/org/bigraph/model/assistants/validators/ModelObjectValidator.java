package org.bigraph.model.assistants.validators;

import java.util.ArrayList;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.ModelObject.ModelObjectChange;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.changes.IChangeValidator;
import org.bigraph.model.names.Namespace;
import org.bigraph.model.names.policies.INamePolicy;

abstract class ModelObjectValidator<T extends ModelObject & IChangeExecutor>
		implements IChangeValidator {
	private interface FinalCheck {
		void run() throws ChangeRejectedException;
	}
	
	private ArrayList<FinalCheck> finalChecks = new ArrayList<FinalCheck>();
	
	private final T changeExecutor;
	
	public ModelObjectValidator(T changeExecutor) {
		this.changeExecutor = changeExecutor;
	}

	protected T getChangeable() {
		return changeExecutor;
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
	
	protected static <V extends ModelObjectChange> FinalCheck makeFinalCheck(
			final PropertyScratchpad context,
			final V change, final ModelObject.FinalValidator<V> validator) {
		return new FinalCheck() {
			@Override
			public void run() throws ChangeRejectedException {
				validator.finalValidate(change, context);
			}
		};
	}
	
	protected <V extends ModelObjectChange> void doExternalValidation(
			final PropertyScratchpad context, final V change,
			final ModelObject.Validator<V> validator)
			throws ChangeRejectedException {
		if (validator != null) {
			validator.validate(change, context);
			if (validator instanceof ModelObject.FinalValidator<?>)
				finalChecks.add(makeFinalCheck(context, change,
						(ModelObject.FinalValidator<V>)validator));
		}
	}
	
	protected IChange doValidateChange(PropertyScratchpad context, IChange b)
			throws ChangeRejectedException {
		if (!b.isReady()) {
			throw new ChangeRejectedException(b, "The Change is not ready");
		} else if (b instanceof ChangeGroup) {
			for (IChange c : (ChangeGroup)b)
				if ((c = doValidateChange(context, c)) != null)
					return c;
			/* All changes will have been individually simulated */
			return null;
		} else if (b instanceof ChangeExtendedData) {
			ChangeExtendedData c = (ChangeExtendedData)b;
			doExternalValidation(context, c, c.validator);
		} else return b;
		b.simulate(context);
		return null;
	}
	
	@Override
	public void tryValidateChange(IChange b) throws ChangeRejectedException {
		tryValidateChange(null, b);
	}
	
	public void tryValidateChange(PropertyScratchpad context, IChange b)
			throws ChangeRejectedException {
		context = new PropertyScratchpad(context);
		finalChecks.clear();
		
		IChange c = doValidateChange(context, b);
		if (c != null)
			throw new ChangeRejectedException(c,
					"The change was not recognised by the validator");
		
		for (FinalCheck i : finalChecks)
			i.run();
		
		context.clear();
	}
}
