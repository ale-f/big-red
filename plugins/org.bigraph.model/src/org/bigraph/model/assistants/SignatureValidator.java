package org.bigraph.model.assistants;

import org.bigraph.model.PortSpec;
import org.bigraph.model.PortSpec.ChangeRemovePort;
import org.bigraph.model.Signature;
import org.bigraph.model.Control.ChangeAddPort;
import org.bigraph.model.Control.ChangeKind;
import org.bigraph.model.Control.ChangeName;
import org.bigraph.model.Signature.ChangeAddControl;
import org.bigraph.model.Signature.ChangeAddSignature;
import org.bigraph.model.Signature.ChangeRemoveSignature;
import org.bigraph.model.Control.ChangeRemoveControl;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeValidator2;

class SignatureValidator implements IChangeValidator2 {
	@Override
	public boolean tryValidateChange(Process process, IChange b)
			throws ChangeRejectedException {
		final PropertyScratchpad context = process.getScratch();
		if (b instanceof ChangeAddControl) {
			ChangeAddControl c = (ChangeAddControl)b;
			ModelObjectValidator.checkName(context, c, c.control,
					c.getCreator().getNamespace(), c.name);
			/* XXX: parent check */
		} else if (b instanceof ChangeRemoveControl) {
			/* XXX: parent check */
		} else if (b instanceof ChangeAddPort) {
			ChangeAddPort c = (ChangeAddPort)b;
			ModelObjectValidator.checkName(context, c, c.port,
					c.getCreator().getNamespace(), c.name);
		} else if (b instanceof ChangeRemovePort) {
			/* XXX: parent check */
		} else if (b instanceof ChangeKind) {
			/* do nothing */
		} else if (b instanceof PortSpec.ChangeName) {
			PortSpec.ChangeName c = (PortSpec.ChangeName)b;
			ModelObjectValidator.checkName(context, c, c.getCreator(),
					c.getCreator().getControl(context).getNamespace(),
					c.name);
		} else if (b instanceof ChangeName) {
			ChangeName c = (ChangeName)b;
			Signature signature = c.getCreator().getSignature(context);
			ModelObjectValidator.checkName(context, c, c.getCreator(),
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
		b.simulate(context);
		return true;
	}
}
