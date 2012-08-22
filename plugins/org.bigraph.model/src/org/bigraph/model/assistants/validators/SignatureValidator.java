package org.bigraph.model.assistants.validators;

import org.bigraph.model.Control;
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
import org.bigraph.model.names.Namespace;
import org.bigraph.model.names.policies.INamePolicy;

public class SignatureValidator extends ModelObjectValidator<Signature> {
	public SignatureValidator(Signature changeable) {
		super(changeable);
	}
	
	private void checkEligibility(IChange b, Control c) throws ChangeRejectedException {
		if (c.getSignature(getScratch()) != getChangeable())
			throw new ChangeRejectedException(b,
					"The control " + c + " is not part of this Signature");
	}
	
	private void checkName(IChange b, Control c, String cdt)
			throws ChangeRejectedException {
		if (cdt == null || cdt.length() == 0)
			throw new ChangeRejectedException(b,
					"Control names cannot be empty");
		Namespace<Control> ns = getChangeable().getNamespace();
		Control co = null;
		if ((co = ns.get(getScratch(), cdt)) != null && co != c)
			throw new ChangeRejectedException(b, "Names must be unique");
		INamePolicy p = ns.getPolicy();
		if (p != null && p.normalise(cdt) == null)
			throw new ChangeRejectedException(b,
					"\"" + cdt + "\" is not a valid name for " + c);
	}
	
	@Override
	public IChange doValidateChange(IChange b) throws ChangeRejectedException {
		if (super.doValidateChange(b) == null) {
			return null;
		} else if (b instanceof ChangeAddControl) {
			ChangeAddControl c = (ChangeAddControl)b;
			checkName(c, c.control, c.name);
		} else if (b instanceof ChangeRemoveControl) {
			ChangeRemoveControl c = (ChangeRemoveControl)b;
			checkEligibility(b, c.getCreator());
		} else if (b instanceof ChangeAddPort) {
			ChangeAddPort c = (ChangeAddPort)b;
			checkEligibility(b, c.getCreator());
		} else if (b instanceof ChangeRemovePort) {
			ChangeRemovePort c = (ChangeRemovePort)b;
			Control co = c.getCreator().getControl();
			checkEligibility(b, co);
		} else if (b instanceof ChangeKind) {
			/* do nothing */
		} else if (b instanceof PortSpec.ChangeName) {
			PortSpec.ChangeName c = (PortSpec.ChangeName)b;
			if (c.name.trim().length() == 0)
				throw new ChangeRejectedException(b, "Port names must not be empty");
		} else if (b instanceof ChangeName) {
			ChangeName c = (ChangeName)b;
			checkEligibility(b, c.getCreator());
			checkName(c, c.getCreator(), c.name);
		} else if (b instanceof ChangeAddSignature) {
			ChangeAddSignature c = (ChangeAddSignature)b;
			if (c.signature.getParent(getScratch()) != null)
				throw new ChangeRejectedException(b,
						"Signature " + c.signature + " already has a parent");
		} else if (b instanceof ChangeRemoveSignature) {
			ChangeRemoveSignature c = (ChangeRemoveSignature)b;
			if (c.getCreator().getParent(getScratch()) == null)
				throw new ChangeRejectedException(b,
						"Signature " + c.getCreator() + " doesn't have a parent");
		} else return b;
		b.simulate(getScratch());
		return null;
	}
}
