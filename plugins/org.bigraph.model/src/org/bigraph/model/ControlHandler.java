package org.bigraph.model;

import org.bigraph.model.Control.ChangeRemoveControl;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.names.Namespace;

final class ControlHandler extends HandlerUtilities.HandlerImpl {
	@Override
	public boolean executeChange(IChange b) {
		if (b instanceof ChangeRemoveControl) {
			ChangeRemoveControl c = (ChangeRemoveControl)b;
			Namespace<Control> ns =
					c.getCreator().getSignature().getNamespace();
			c.getCreator().getSignature().removeControl(c.getCreator());
			ns.remove(c.getCreator().getName());
		} else return false;
		return true;
	}
	
	@Override
	public boolean tryValidateChange(Process process, IChange b)
			throws ChangeRejectedException {
		final PropertyScratchpad context = process.getScratch();
		if (b instanceof ChangeRemoveControl) {
			Control co = ((ChangeRemoveControl)b).getCreator();
			if (co.getSignature(context) == null)
				throw new ChangeRejectedException(b,
						"" + co + " doesn't have a parent");
		} else return false;
		return true;
	}
}
