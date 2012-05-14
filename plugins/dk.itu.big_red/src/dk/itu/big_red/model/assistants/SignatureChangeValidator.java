package dk.itu.big_red.model.assistants;

import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Control.ChangeAddPort;
import dk.itu.big_red.model.Control.ChangeDefaultSize;
import dk.itu.big_red.model.Control.ChangeKind;
import dk.itu.big_red.model.Control.ChangeLabel;
import dk.itu.big_red.model.Control.ChangeName;
import dk.itu.big_red.model.Control.ChangeRemovePort;
import dk.itu.big_red.model.Control.ChangeResizable;
import dk.itu.big_red.model.ModelObject.ChangeExtendedData;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Signature.ChangeAddControl;
import dk.itu.big_red.model.Signature.ChangeRemoveControl;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.ChangeValidator;

public class SignatureChangeValidator extends ChangeValidator<Signature> {
	private final PropertyScratchpad scratch;
	private Change activeChange = null;
	
	public SignatureChangeValidator(Signature changeable) {
		super(changeable);
		scratch = new PropertyScratchpad();
	}

	@Override
	public void tryValidateChange(Change b) throws ChangeRejectedException {
		activeChange = b;
		
		scratch.clear();
		_tryValidateChange(b);
		
		activeChange = null;
	}

	protected void rejectChange(String rationale)
			throws ChangeRejectedException {
		super.rejectChange(activeChange, rationale);
	}
	
	private void checkEligibility(Control c) throws ChangeRejectedException {
		if (c.getSignature(scratch) != getChangeable())
			rejectChange("The control " + c + " is not part of this Signature");
	}
	
	private void _tryValidateChange(Change b) throws ChangeRejectedException {
		if (!b.isReady()) {
			rejectChange("The Change is not ready");
		} else if (b instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)b)
				_tryValidateChange(c);
		} else if (b instanceof ChangeExtendedData) {
			ChangeExtendedData c = (ChangeExtendedData)b;
			if (c.validator != null) {
				String rationale = c.validator.validate(c, scratch);
				if (rationale != null)
					rejectChange(rationale);
			}
			scratch.setProperty(c.getCreator(), c.key, c.newValue);
		} else if (b instanceof ChangeAddControl) {
			ChangeAddControl c = (ChangeAddControl)b;
			getChangeable().addControl(scratch, c.control);
		} else if (b instanceof ChangeRemoveControl) {
			ChangeRemoveControl c = (ChangeRemoveControl)b;
			getChangeable().removeControl(scratch, c.control);
		} else if (b instanceof ChangeAddPort) {
			ChangeAddPort c = (ChangeAddPort)b;
			checkEligibility(c.getCreator());
			c.getCreator().addPort(scratch, c.port);
		} else if (b instanceof ChangeRemovePort) {
			ChangeRemovePort c = (ChangeRemovePort)b;
			checkEligibility(c.getCreator());
			c.getCreator().removePort(scratch, c.port);
		} else if (b instanceof ChangeLabel) {
			checkEligibility(((ChangeLabel)b).getCreator());
		} else if (b instanceof ChangeResizable ||
				b instanceof ChangeDefaultSize ||
				b instanceof ChangeKind) {
			/* do nothing, yet */
		} else if (b instanceof PortSpec.ChangeName) {
			PortSpec.ChangeName c = (PortSpec.ChangeName)b;
			if (c.name.trim().length() == 0)
				rejectChange(b, "Port names must not be empty");
		} else if (b instanceof ChangeName) {
			ChangeName c = (ChangeName)b;
			checkEligibility(c.getCreator());
			if (c.name.trim().length() == 0)
				rejectChange(b, "Control names must not be empty");
			c.getCreator().setName(scratch, c.name);
		} else rejectChange("The change was not recognised by the validator");
	}
}
