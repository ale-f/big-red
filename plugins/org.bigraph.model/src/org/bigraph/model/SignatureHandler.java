package org.bigraph.model;

import org.bigraph.model.Signature.ChangeAddControl;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.names.Namespace;

final class SignatureHandler extends HandlerUtilities.HandlerImpl {
	@Override
	public boolean executeChange(IChange b) {
		if (b instanceof ChangeAddControl) {
			ChangeAddControl c = (ChangeAddControl)b;
			Namespace<Control> ns = c.getCreator().getNamespace();
			c.control.setName(ns.put(c.name, c.control));
			c.getCreator().addControl(c.control);
		} else return false;
		return true;
	}
	
	@Override
	public boolean tryValidateChange(Process process, IChange b)
			throws ChangeRejectedException {
		final PropertyScratchpad context = process.getScratch();
		if (b instanceof ChangeAddControl) {
			ChangeAddControl c = (ChangeAddControl)b;
			HandlerUtilities.checkName(context, c, c.control,
					c.getCreator().getNamespace(), c.name);
			if (c.control.getSignature(context) != null)
				throw new ChangeRejectedException(b,
						"" + c.control + " already has a parent");
		} else return false;
		return true;
	}
}
