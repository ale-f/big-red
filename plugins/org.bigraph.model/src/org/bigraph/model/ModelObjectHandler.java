package org.bigraph.model;

import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;

final class ModelObjectHandler extends HandlerUtilities.HandlerImpl {
	@Override
	public boolean executeChange(IChange c_) {
		if (c_ instanceof ChangeExtendedData) {
			ChangeExtendedData c = (ChangeExtendedData)c_;
			c.getCreator().setExtendedData(c.key, c.newValue);
		} else return false;
		return true;
	}
	
	@Override
	public boolean tryValidateChange(final Process process, IChange change)
			throws ChangeRejectedException {
		return (change instanceof ChangeExtendedData);
	}
}
