package it.uniud.bigredit.policy;

import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.model.Reaction;

import java.util.ArrayList;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Control.Kind;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.ChangeValidator;


/**
 * The <strong>BRSChangeValidator</strong> is the basic validator that
 * all changes to {@link Bigraph}s must go through; it checks for both model
 * consistency and visual sensibleness.
 * @author carlo
 *
 */
public class ReactionChangeValidator extends ChangeValidator<Reaction> {
	//private BigraphScratchpad scratch = null;
	private Change activeChange = null;
	
	public ReactionChangeValidator(Reaction changeable) {
		super(changeable);
		//scratch = new BigraphScratchpad(changeable);
	}
	
	private ArrayList<Layoutable> layoutChecks =
		new ArrayList<Layoutable>();
	
	protected void rejectChange(String rationale)
			throws ChangeRejectedException {
		super.rejectChange(activeChange, rationale);
	}

	
	@Override
	public void tryValidateChange(Change b)
			throws ChangeRejectedException {
		activeChange = b;
		
		//scratch.clear();
		
		//layoutChecks.clear();
		
		_tryValidateChange(b);
		b.isReady();
	
		
		activeChange = null;
	}
	
	protected void _tryValidateChange(Change b)
			throws ChangeRejectedException {
		//System.out.println("called _tryValidateChange BRSChangeValidator");
		if (!b.isReady()) {
			rejectChange("The Change is not ready");
		} else if (b instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)b)
				_tryValidateChange(c);
		
		} else if (b instanceof BRS.ChangeAddChild) {
			BRS.ChangeAddChild c = (BRS.ChangeAddChild)b;
			
			if (c.getCreator() instanceof Node &&
				((Node)c.getCreator()).getControl().getKind() == Kind.ATOMIC)
				rejectChange(
						((Node)c.getCreator()).getControl().getName() +
						" is an atomic control");
			

		} else {
			//rejectChange("The change was not recognised by the validator");
		}
	}
}
