package org.bigraph.model;

import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.ModelObject.FinalExtendedDataValidator;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;

final class ModelObjectHandler extends HandlerUtilities.HandlerImpl {
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
	public boolean tryValidateChange(final Process process, IChange change)
			throws ChangeRejectedException {
		if (change instanceof ChangeExtendedData) {
			final ChangeExtendedData c = (ChangeExtendedData)change;
			if (c.validator != null) {
				c.validator.validate(c, process.getScratch());
				if (c.validator instanceof FinalExtendedDataValidator) {
					process.addCallback(new Callback() {
						@Override
						public void run() throws ChangeRejectedException {
							((FinalExtendedDataValidator)c.validator).
									finalValidate(c, process.getScratch());
						}
					});
				}
			}
		} else return false;
		return true;
	}
}
