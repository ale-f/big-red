package org.bigraph.model;

import org.bigraph.model.PortSpec.ChangeRemovePort;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.names.Namespace;

final class PortSpecHandler extends DescriptorHandlerUtilities.HandlerImpl {
	@Override
	public boolean executeChange(IChange b) {
		if (b instanceof ChangeRemovePort) {
			ChangeRemovePort c = (ChangeRemovePort)b;
			Namespace<PortSpec> ns =
					c.getCreator().getControl().getNamespace();
			c.getCreator().getControl().removePort(c.getCreator());
			ns.remove(c.getCreator().getName());
		} else return false;
		return true;
	}

	@Override
	public boolean tryValidateChange(Process process, IChange b)
			throws ChangeRejectedException {
		final PropertyScratchpad context = process.getScratch();
		if (b instanceof ChangeRemovePort) {
			PortSpec po = ((ChangeRemovePort)b).getCreator();
			if (po.getControl(context) == null)
				throw new ChangeRejectedException(b,
						"" + po + " doesn't have a parent");
		} else return false;
		return true;
	}
}
