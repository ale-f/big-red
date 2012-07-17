package org.bigraph.model.assistants.validators;

import org.bigraph.model.Control;
import org.bigraph.model.PortSpec;
import org.bigraph.model.PortSpec.ChangeRemovePort;
import org.bigraph.model.Signature;
import org.bigraph.model.Control.ChangeAddPort;
import org.bigraph.model.Control.ChangeKind;
import org.bigraph.model.Control.ChangeName;
import org.bigraph.model.Signature.ChangeAddControl;
import org.bigraph.model.Control.ChangeRemoveControl;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;

public class SignatureChangeValidator extends ModelObjectValidator<Signature> {
	public SignatureChangeValidator(Signature changeable) {
		super(changeable);
	}
	
	private void checkEligibility(IChange b, Control c) throws ChangeRejectedException {
		if (c.getSignature(getScratch()) != getChangeable())
			throw new ChangeRejectedException(b,
					"The control " + c + " is not part of this Signature");
	}
	
	@Override
	public IChange doValidateChange(IChange b) throws ChangeRejectedException {
		if (super.doValidateChange(b) == null) {
			return null;
		} else if (b instanceof ChangeAddControl) {
			/* do nothing? */
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
			if (c.name.trim().length() == 0)
				throw new ChangeRejectedException(b, "Control names must not be empty");
		} else return b;
		b.simulate(getScratch());
		return null;
	}
}
