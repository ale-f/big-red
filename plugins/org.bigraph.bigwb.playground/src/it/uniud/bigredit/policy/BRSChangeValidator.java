package it.uniud.bigredit.policy;

import it.uniud.bigredit.model.BRS;

import java.util.ArrayList;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Node;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;



/**
 * The <strong>BRSChangeValidator</strong> is the basic validator that
 * all changes to {@link Bigraph}s must go through; it checks for both model
 * consistency and visual sensibleness.
 * @author carlo
 *
 */
public class BRSChangeValidator {
	//private BigraphScratchpad scratch = null;
	private IChange activeChange = null;
	
	public BRSChangeValidator(BRS changeable) {
		//scratch = new BigraphScratchpad(changeable);
	}
	
	private ArrayList<Layoutable> layoutChecks =
		new ArrayList<Layoutable>();
	
	protected void rejectChange(String rationale)
			throws ChangeRejectedException {
		throw new ChangeRejectedException(activeChange, rationale);
	}

	
	public void tryValidateChange(IChange b)
			throws ChangeRejectedException {
		activeChange = b;
		
		//scratch.clear();
		
		//layoutChecks.clear();
		
		_tryValidateChange(b);
		b.isReady();
	
		
		activeChange = null;
	}
	
	protected void _tryValidateChange(IChange b)
			throws ChangeRejectedException {
		//System.out.println("called _tryValidateChange BRSChangeValidator");
		if (!b.isReady()) {
			rejectChange("The Change is not ready");
		} else if (b instanceof ChangeGroup) {
			for (IChange c : (ChangeGroup)b)
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
