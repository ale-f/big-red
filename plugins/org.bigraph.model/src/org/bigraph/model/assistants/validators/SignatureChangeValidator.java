package org.bigraph.model.assistants.validators;

import org.bigraph.model.Control;
import org.bigraph.model.PortSpec;
import org.bigraph.model.PortSpec.ChangeRemovePort;
import org.bigraph.model.Signature;
import org.bigraph.model.Control.ChangeAddPort;
import org.bigraph.model.Control.ChangeKind;
import org.bigraph.model.Control.ChangeName;
import org.bigraph.model.Signature.ChangeAddControl;
import org.bigraph.model.Signature.ChangeRemoveControl;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeRejectedException;

public class SignatureChangeValidator extends ModelObjectValidator<Signature> {
	public SignatureChangeValidator(Signature changeable) {
		super(changeable);
	}
	
	private void checkEligibility(Change b, Control c) throws ChangeRejectedException {
		if (c.getSignature(getScratch()) != getChangeable())
			rejectChange(b, "The control " + c + " is not part of this Signature");
	}
	
	@Override
	public Change doValidateChange(Change b) throws ChangeRejectedException {
		if (super.doValidateChange(b) == null) {
			/* do nothing */
		} else if (b instanceof ChangeAddControl) {
			ChangeAddControl c = (ChangeAddControl)b;
			getChangeable().addControl(getScratch(), c.control);
		} else if (b instanceof ChangeRemoveControl) {
			ChangeRemoveControl c = (ChangeRemoveControl)b;
			getChangeable().removeControl(getScratch(), c.control);
		} else if (b instanceof ChangeAddPort) {
			ChangeAddPort c = (ChangeAddPort)b;
			checkEligibility(b, c.getCreator());
			c.getCreator().addPort(getScratch(), c.port);
		} else if (b instanceof ChangeRemovePort) {
			ChangeRemovePort c = (ChangeRemovePort)b;
			Control co = c.getCreator().getControl();
			checkEligibility(b, co);
			co.removePort(getScratch(), c.getCreator());
		} else if (b instanceof ChangeKind) {
			/* do nothing, yet */
		} else if (b instanceof PortSpec.ChangeName) {
			PortSpec.ChangeName c = (PortSpec.ChangeName)b;
			if (c.name.trim().length() == 0)
				rejectChange(b, "Port names must not be empty");
		} else if (b instanceof ChangeName) {
			ChangeName c = (ChangeName)b;
			checkEligibility(b, c.getCreator());
			if (c.name.trim().length() == 0)
				rejectChange(b, "Control names must not be empty");
			c.getCreator().setName(getScratch(), c.name);
		} else return b;
		return null;
	}
}
