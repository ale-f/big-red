package dk.itu.big_red.model.assistants;

import dk.itu.big_red.model.Colourable.ChangeFillColour;
import dk.itu.big_red.model.Colourable.ChangeOutlineColour;
import dk.itu.big_red.model.Control.ChangeAddPort;
import dk.itu.big_red.model.Control.ChangeDefaultSize;
import dk.itu.big_red.model.Control.ChangeKind;
import dk.itu.big_red.model.Control.ChangeLabel;
import dk.itu.big_red.model.Control.ChangeName;
import dk.itu.big_red.model.Control.ChangePoints;
import dk.itu.big_red.model.Control.ChangeRemovePort;
import dk.itu.big_red.model.Control.ChangeResizable;
import dk.itu.big_red.model.Control.ChangeShape;
import dk.itu.big_red.model.ModelObject.ChangeComment;
import dk.itu.big_red.model.PortSpec.ChangeDistance;
import dk.itu.big_red.model.PortSpec.ChangeSegment;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Signature.ChangeAddControl;
import dk.itu.big_red.model.Signature.ChangeRemoveControl;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.ChangeValidator;

public class SignatureChangeValidator extends ChangeValidator<Signature> {
	private final SignatureScratchpad scratch;
	private Change activeChange = null;
	
	public SignatureChangeValidator(Signature changeable) {
		super(changeable);
		scratch = new SignatureScratchpad(changeable);
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
	
	private void _tryValidateChange(Change b) throws ChangeRejectedException {
		if (!b.isReady()) {
			rejectChange("The Change is not ready");
		} else if (b instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)b)
				tryValidateChange(c);
		} else if (b instanceof ChangeFillColour ||
				b instanceof ChangeOutlineColour ||
				b instanceof ChangeComment) {
			/* do nothing */
		} else if (b instanceof ChangeAddControl) {
			ChangeAddControl c = (ChangeAddControl)b;
			scratch.addControl(c.control);
		} else if (b instanceof ChangeRemoveControl) {
			ChangeRemoveControl c = (ChangeRemoveControl)b;
			scratch.removeControl(c.control);
		} else if (b instanceof ChangeAddPort) {
			ChangeAddPort c = (ChangeAddPort)b;
			scratch.addPortFor(c.getCreator(), c.port);
		} else if (b instanceof ChangeRemovePort) {
			ChangeRemovePort c = (ChangeRemovePort)b;
			scratch.removePortFor(c.getCreator(), c.port);
		} else if (b instanceof ChangeShape ||
				b instanceof ChangeLabel ||
				b instanceof ChangeResizable ||
				b instanceof ChangeDefaultSize ||
				b instanceof ChangeKind ||
				b instanceof ChangeSegment ||
				b instanceof ChangePoints ||
				b instanceof PortSpec.ChangeName) {
			/* do nothing, yet */
		} else if (b instanceof ChangeDistance) {
			ChangeDistance c = (ChangeDistance)b;
			if (c.distance < 0 || c.distance >= 1.0)
				rejectChange(b, "The distance value is invalid");
		} else if (b instanceof ChangeName) {
			ChangeName c = (ChangeName)b;
			if (c.name.trim().length() == 0)
				rejectChange(b, "Control names must not be empty");
		} else rejectChange("The change was not recognised by the validator");
	}
}
