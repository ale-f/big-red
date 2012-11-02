package org.bigraph.model;

import org.bigraph.model.Control.ChangeAddPort;
import org.bigraph.model.Control.ChangeKind;
import org.bigraph.model.Control.ChangeRemoveControl;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;
import org.bigraph.model.names.Namespace;

final class ControlHandler implements IStepExecutor, IStepValidator {
	@Override
	public boolean executeChange(IChange b) {
		if (b instanceof ChangeRemoveControl) {
			ChangeRemoveControl c = (ChangeRemoveControl)b;
			Namespace<Control> ns =
					c.getCreator().getSignature().getNamespace();
			c.getCreator().getSignature().removeControl(c.getCreator());
			ns.remove(c.getCreator().getName());
		} else if (b instanceof ChangeKind) {
			ChangeKind c = (ChangeKind)b;
			c.getCreator().setKind(c.kind);
		} else if (b instanceof ChangeAddPort) {
			ChangeAddPort c = (ChangeAddPort)b;
			c.getCreator().addPort(c.port);
			c.port.setName(
					c.getCreator().getNamespace().put(c.name, c.port));
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
		} else if (b instanceof ChangeAddPort) {
			ChangeAddPort c = (ChangeAddPort)b;
			NamedModelObjectHandler.checkName(context, c, c.port,
					c.getCreator().getNamespace(), c.name);
			if (c.port.getControl(context) != null)
				throw new ChangeRejectedException(b,
						"" + c.port + " already has a parent");
		} else if (b instanceof ChangeKind) {
			/* do nothing */
		} else return false;
		return true;
	}
}
