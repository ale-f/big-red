package org.bigraph.model;

import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.ModelObject.ModelObjectChange;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;

final class ModelObjectHandler implements IStepExecutor, IStepValidator {
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
	public boolean executeChange(IChange c_) {
		if (c_ instanceof ChangeExtendedData) {
			ChangeExtendedData c = (ChangeExtendedData)c_;
			c.getCreator().setExtendedData(c.key, (c.normaliser == null ?
					c.newValue : c.normaliser.normalise(c, c.newValue)));
		} else return false;
		return true;
	}
	
	@Override
	public boolean tryValidateChange(Process process, IChange change)
			throws ChangeRejectedException {
		if (change instanceof ChangeExtendedData) {
			ChangeExtendedData c = (ChangeExtendedData)change;
			doExternalValidation(process, c, c.validator);
		} else return false;
		return true;
	}
}
