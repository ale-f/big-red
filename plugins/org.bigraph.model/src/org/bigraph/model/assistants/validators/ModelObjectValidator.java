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
	
	private PropertyScratchpad scratch = null;
	
	protected PropertyScratchpad getScratch() {
		return scratch;
	}
	
	private ArrayList<FinalCheck> finalChecks = new ArrayList<FinalCheck>();
	
	private final T changeExecutor;
	
	public ModelObjectValidator(T changeExecutor) {
		this.changeExecutor = changeExecutor;
	}

	protected T getChangeable() {
		return changeExecutor;
	}
	
	protected <V> void checkName(
			IChange c, V object, Namespace<? extends V> ns, String cdt)
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
		V current = ns.get(getScratch(), mcdt);
		if (current != null && current != object)
			throw new ChangeRejectedException(c, "Names must be unique");
	}
	
	protected <V extends ModelObjectChange> FinalCheck makeFinalCheck(
			final V change, final ModelObject.FinalValidator<V> validator) {
		return new FinalCheck() {
			@Override
			public void run() throws ChangeRejectedException {
				validator.finalValidate(change, getScratch());
			}
		};
	}
	
	protected <V extends ModelObjectChange> void doExternalValidation(
			final V change, final ModelObject.Validator<V> validator)
			throws ChangeRejectedException {
		if (validator != null) {
			validator.validate(change, getScratch());
			if (validator instanceof ModelObject.FinalValidator<?>)
				finalChecks.add(makeFinalCheck(change,
						(ModelObject.FinalValidator<V>)validator));
		}
	}
	
	protected IChange doValidateChange(IChange b)
			throws ChangeRejectedException {
		if (!b.isReady()) {
			throw new ChangeRejectedException(b, "The Change is not ready");
		} else if (b instanceof ChangeGroup) {
			for (IChange c : (ChangeGroup)b)
				if ((c = doValidateChange(c)) != null)
					return c;
			/* All changes will have been individually simulated */
			return null;
		} else if (b instanceof ChangeExtendedData) {
			ChangeExtendedData c = (ChangeExtendedData)b;
			doExternalValidation(c, c.validator);
		} else return b;
		b.simulate(getScratch());
		return null;
	}
	
	@Override
	public void tryValidateChange(IChange b) throws ChangeRejectedException {
		tryValidateChange(null, b);
	}
	
	public void tryValidateChange(PropertyScratchpad context, IChange b)
			throws ChangeRejectedException {
		scratch = new PropertyScratchpad(context);
		finalChecks.clear();
		
		IChange c = doValidateChange(b);
		if (c != null)
			throw new ChangeRejectedException(c,
					"The change was not recognised by the validator");
		
		for (FinalCheck i : finalChecks)
			i.run();
		
		scratch.clear();
		scratch = null;
	}
}
