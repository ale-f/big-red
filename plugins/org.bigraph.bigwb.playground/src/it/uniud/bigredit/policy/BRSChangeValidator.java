package it.uniud.bigredit.policy;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.Control.Kind;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.ChangeValidator;


/**
 * The <strong>BigraphIntegrityValidator</strong> is the basic validator that
 * all changes to {@link Bigraph}s must go through; it checks for both model
 * consistency and visual sensibleness.
 * @author alec
 *
 */
public class BRSChangeValidator extends ChangeValidator<Bigraph> {
	//private BigraphScratchpad scratch = null;
	private Change activeChange = null;
	
	public BRSChangeValidator(Bigraph changeable) {
		super(changeable);
		//scratch = new BigraphScratchpad(changeable);
	}
	
	private ArrayList<Layoutable> layoutChecks =
		new ArrayList<Layoutable>();
	
	protected void rejectChange(String rationale)
			throws ChangeRejectedException {
		super.rejectChange(activeChange, rationale);
	}
	
	private void runLayoutChecks() throws ChangeRejectedException {
		/*for (Layoutable i : layoutChecks) {
			Container parent = scratch.getParentFor(i);
			ReadonlyRectangle layout = scratch.getLayoutFor(i);
			checkObjectCanContain(parent, layout);
			if (i instanceof Container)
				checkLayoutCanContainChildren((Container)i, layout);
		}*/
	}
	
	private void checkObjectCanContain(Layoutable o, Rectangle nl) throws ChangeRejectedException {
		/*if (o != null && !(o instanceof Bigraph)) {
			Rectangle tr =
				scratch.getLayoutFor(o).getCopy().setLocation(0, 0);
			if (!tr.contains(nl))
				rejectChange(
					"The object can no longer fit into its container");
		}*/
	}
	
	private void checkLayoutCanContainChildren(Container c, Rectangle nl) throws ChangeRejectedException {
		/*nl = nl.getCopy().setLocation(0, 0);
		for (Layoutable i : c.getChildren()) {
			ReadonlyRectangle layout = scratch.getLayoutFor(i);
			if (!nl.contains(layout))
				rejectChange("The new size is too small");
		}*/
	}
	
	private void checkEligibility(Layoutable... l) throws ChangeRejectedException {
		/*for (Layoutable i : l)
			if (scratch.getBigraphFor(i) != getChangeable())
				rejectChange(i + " is not part of this Bigraph");*/
	}
	
	@Override
	public void tryValidateChange(Change b)
			throws ChangeRejectedException {
		activeChange = b;
		
		//scratch.clear();
		
		layoutChecks.clear();
		
		_tryValidateChange(b);
		
		runLayoutChecks();
		
		activeChange = null;
	}
	
	protected void _tryValidateChange(Change b)
			throws ChangeRejectedException {
		if (!b.isReady()) {
			rejectChange("The Change is not ready");
		} else if (b instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)b)
				_tryValidateChange(c);
		
		} else if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			
			if (c.getCreator() instanceof Node &&
				((Node)c.getCreator()).getControl().getKind() == Kind.ATOMIC)
				rejectChange(
						((Node)c.getCreator()).getControl().getName() +
						" is an atomic control");
			

		} else {
			rejectChange("The change was not recognised by the validator");
		}
	}
}
