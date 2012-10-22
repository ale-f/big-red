package org.bigraph.model;

import org.bigraph.model.Control.ChangeAddPort;
import org.bigraph.model.Control.ChangeKind;
import org.bigraph.model.Control.ChangeName;
import org.bigraph.model.Control.ChangeRemoveControl;
import org.bigraph.model.PortSpec.ChangeRemovePort;
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
			c.getCreator().addControl(c.control);
		} else if (b instanceof ChangeRemoveControl) {
			ChangeRemoveControl c = (ChangeRemoveControl)b;
			Namespace<Control> ns =
					c.getCreator().getSignature().getNamespace();
			c.getCreator().getSignature().removeControl(c.getCreator());
			ns.remove(c.getCreator().getName());
		} else if (b instanceof ChangeName) {
			ChangeName c = (ChangeName)b;
			Namespace<Control> ns =
					c.getCreator().getSignature().getNamespace();
			c.getCreator().setName(
					ns.rename(c.getCreator().getName(), c.name));
		} else if (b instanceof ChangeKind) {
			ChangeKind c = (ChangeKind)b;
			c.getCreator().setKind(c.kind);
		} else if (b instanceof ChangeAddPort) {
			ChangeAddPort c = (ChangeAddPort)b;
			c.getCreator().addPort(c.port);
			c.port.setName(
					c.getCreator().getNamespace().put(c.name, c.port));
		} else if (b instanceof ChangeRemovePort) {
			ChangeRemovePort c = (ChangeRemovePort)b;
			Namespace<PortSpec> ns =
					c.getCreator().getControl().getNamespace();
			c.getCreator().getControl().removePort(c.getCreator());
			ns.remove(c.getCreator().getName());
		} else if (b instanceof PortSpec.ChangeName) {
			PortSpec.ChangeName c = (PortSpec.ChangeName)b;
			PortSpec p = c.getCreator();
			p.setName(p.getControl().getNamespace().rename(
					p.getName(), c.name));
		} else if (b instanceof ChangeAddSignature) {
			ChangeAddSignature c = (ChangeAddSignature)b;
			c.getCreator().addSignature(c.signature);
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
			ModelObjectHandler.checkName(context, c, c.control,
					c.getCreator().getNamespace(), c.name);
			if (c.control.getSignature(context) != null)
				throw new ChangeRejectedException(b,
						"" + c.control + " already has a parent");
		} else if (b instanceof ChangeRemoveControl) {
			Control co = ((ChangeRemoveControl)b).getCreator();
			if (co.getSignature(context) == null)
				throw new ChangeRejectedException(b,
						"" + co + " doesn't have a parent");
		} else if (b instanceof ChangeAddPort) {
			ChangeAddPort c = (ChangeAddPort)b;
			ModelObjectHandler.checkName(context, c, c.port,
					c.getCreator().getNamespace(), c.name);
			if (c.port.getControl(context) != null)
				throw new ChangeRejectedException(b,
						"" + c.port + " already has a parent");
		} else if (b instanceof ChangeRemovePort) {
			PortSpec po = ((ChangeRemovePort)b).getCreator();
			if (po.getControl(context) == null)
				throw new ChangeRejectedException(b,
						"" + po + " doesn't have a parent");
		} else if (b instanceof ChangeKind) {
			/* do nothing */
		} else if (b instanceof PortSpec.ChangeName) {
			PortSpec.ChangeName c = (PortSpec.ChangeName)b;
			ModelObjectHandler.checkName(context, c, c.getCreator(),
					c.getCreator().getControl(context).getNamespace(),
					c.name);
		} else if (b instanceof ChangeName) {
			ChangeName c = (ChangeName)b;
			Signature signature = c.getCreator().getSignature(context);
			ModelObjectHandler.checkName(context, c, c.getCreator(),
					signature.getNamespace(), c.name);
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
