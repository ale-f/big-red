package dk.itu.big_red.model.assistants;

import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Signature;
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
		} else if (b instanceof Colourable.ChangeFillColour ||
				b instanceof Colourable.ChangeOutlineColour) {
			/* do nothing */
		} else {
			rejectChange("The change was not recognised by the validator");
		}
	}
	
}
