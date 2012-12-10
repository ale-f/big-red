package org.bigraph.model;

import org.bigraph.model.Signature.ChangeAddControl;
import org.bigraph.model.Signature.ChangeAddSignature;
import org.bigraph.model.Signature.ChangeRemoveSignature;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;
import org.bigraph.model.names.Namespace;

final class SignatureHandler implements IStepExecutor, IStepValidator {
	@Override
	public boolean executeChange(IChange b) {
		if (b instanceof ChangeAddControl) {
			ChangeAddControl c = (ChangeAddControl)b;
			Namespace<Control> ns = c.getCreator().getNamespace();
			c.control.setName(ns.put(c.name, c.control));
			c.getCreator().addControl(-1, c.control);
		} else if (b instanceof ChangeAddSignature) {
			ChangeAddSignature c = (ChangeAddSignature)b;
			c.getCreator().addSignature(-1, c.signature);
		} else if (b instanceof ChangeRemoveSignature) {
			ChangeRemoveSignature c = (ChangeRemoveSignature)b;
			c.getCreator().getParent().removeSignature(c.getCreator());
		} else return false;
		return true;
	}
	
	@Override
	public boolean tryValidateChange(Process process, IChange b)
			throws ChangeRejectedException {
		final PropertyScratchpad context = process.getScratch();
		if (b instanceof ChangeAddControl) {
			ChangeAddControl c = (ChangeAddControl)b;
			NamedModelObjectHandler.checkName(context, c, c.control,
					c.getCreator().getNamespace(), c.name);
			if (c.control.getSignature(context) != null)
				throw new ChangeRejectedException(b,
						"" + c.control + " already has a parent");
		} else if (b instanceof ChangeAddSignature) {
			ChangeAddSignature c = (ChangeAddSignature)b;
			if (c.signature.getParent(context) != null)
				throw new ChangeRejectedException(b,
						"Signature " + c.signature + " already has a parent");
		} else if (b instanceof ChangeRemoveSignature) {
			ChangeRemoveSignature c = (ChangeRemoveSignature)b;
			if (c.getCreator().getParent(context) == null)
				throw new ChangeRejectedException(b,
						"Signature " + c.getCreator() + " doesn't have a parent");
		} else return false;
		return true;
	}
}
