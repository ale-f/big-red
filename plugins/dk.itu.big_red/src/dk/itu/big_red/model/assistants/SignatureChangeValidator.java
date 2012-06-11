package dk.itu.big_red.model.assistants;

import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeRejectedException;

import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Control.ChangeAddPort;
import dk.itu.big_red.model.Control.ChangeKind;
import dk.itu.big_red.model.Control.ChangeName;
import dk.itu.big_red.model.Control.ChangeRemovePort;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Signature.ChangeAddControl;
import dk.itu.big_red.model.Signature.ChangeRemoveControl;

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
			checkEligibility(b, c.getCreator());
			c.getCreator().removePort(getScratch(), c.port);
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
