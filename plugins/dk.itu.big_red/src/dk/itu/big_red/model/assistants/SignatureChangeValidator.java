package dk.itu.big_red.model.assistants;

import dk.itu.big_red.model.Colourable.ChangeFillColour;
import dk.itu.big_red.model.Colourable.ChangeOutlineColour;
import dk.itu.big_red.model.Control.ChangeLabel;
import dk.itu.big_red.model.Control.ChangeName;
import dk.itu.big_red.model.Control.ChangeShape;
import dk.itu.big_red.model.ModelObject.ChangeComment;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Signature.ChangeAddControl;
import dk.itu.big_red.model.Signature.ChangeRemoveControl;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.ChangeValidator;

public class SignatureChangeValidator extends ChangeValidator<Signature> {
	private Change activeChange = null;
	
	public SignatureChangeValidator(Signature changeable) {
		super(changeable);
	}

	@Override
	public void tryValidateChange(Change b) throws ChangeRejectedException {
		activeChange = b;
		
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
		} else if (b instanceof ChangeAddControl ||
				b instanceof ChangeRemoveControl ||
				b instanceof ChangeShape ||
				b instanceof ChangeLabel) {
			/* do nothing, yet */
		} else if (b instanceof ChangeName) {
			ChangeName c = (ChangeName)b;
			if (c.name.trim().length() == 0)
				rejectChange(b, "Control names must not be empty");
		} else rejectChange("The change was not recognised by the validator");
	}
	
}
