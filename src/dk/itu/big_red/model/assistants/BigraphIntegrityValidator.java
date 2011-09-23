package dk.itu.big_red.model.assistants;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.ChangeValidator;
import dk.itu.big_red.model.changes.IChangeable;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeAddChild;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeConnect;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeDisconnect;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeEdgeReposition;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeLayout;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeRemoveChild;

/**
 * The <strong>BigraphIntegrityValidator</strong> is the basic validator that
 * all changes to {@link Bigraph}s must go through; it checks for both model
 * consistency and visual sensibleness.
 * @author alec
 *
 */
public class BigraphIntegrityValidator extends ChangeValidator {
	
	public BigraphIntegrityValidator(IChangeable changeable) {
		super(changeable);
	}
	
	private BigraphScratchpad scratch = new BigraphScratchpad();
	
	private void checkObjectCanContain(Change b, LayoutableModelObject o, Rectangle nl) throws ChangeRejectedException {
		if (o != null && !(o instanceof Bigraph)) {
			Rectangle tr =
				scratch.getLayoutFor(o).getCopy().setLocation(0, 0);
			if (!tr.contains(nl))
				rejectChange(b,
					"The object can no longer fit into its container");
		}
	}
	
	private void checkLayoutCanContainChildren(Change b, Container c, Rectangle nl) throws ChangeRejectedException {
		nl = nl.getCopy().setLocation(0, 0);
		for (LayoutableModelObject i : c.getChildren()) {
			Rectangle layout = scratch.getLayoutFor(i);
			if (!nl.contains(layout))
				rejectChange(b, "The new size is too small");
		}
	}
	
	@Override
	public void tryValidateChange(Change b)
			throws ChangeRejectedException {
		scratch.clear();
		_tryValidateChange(b);
	}
	
	protected void _tryValidateChange(Change b)
			throws ChangeRejectedException {
		if (!b.isReady()) {
			rejectChange(b, "The Change is not ready");
		} else if (b instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)b)
				_tryValidateChange(c);
		} else if (b instanceof BigraphChangeConnect) {
			BigraphChangeConnect c = (BigraphChangeConnect)b;
			if (scratch.getLinkFor(c.point) != null)
				rejectChange(b,
					"Connections can only be established to Points that " +
					"aren't already connected");
			scratch.addPointFor(c.link, c.point);
		} else if (b instanceof BigraphChangeDisconnect) {
			BigraphChangeDisconnect c = (BigraphChangeDisconnect)b;
			if (scratch.getLinkFor(c.point) == null)
				rejectChange(b, "The Point is already disconnected");
			scratch.removePointFor(c.link, c.point);
		} else if (b instanceof BigraphChangeAddChild) {
			BigraphChangeAddChild c = (BigraphChangeAddChild)b;
			if (!c.parent.canContain(c.child))
				rejectChange(b,
					c.parent.getClass().getSimpleName() + "s can't contain " +
					c.child.getClass().getSimpleName() + "s");
			checkObjectCanContain(b, c.parent, c.newLayout);
			scratch.setLayoutFor(c.child, c.newLayout);
			scratch.addChildFor(c.parent, c.child);
		} else if (b instanceof BigraphChangeRemoveChild) {
			BigraphChangeRemoveChild c = (BigraphChangeRemoveChild)b;
			scratch.removeChildFor(c.parent, c.child);
		} else if (b instanceof BigraphChangeLayout) {
			BigraphChangeLayout c = (BigraphChangeLayout)b;
			if (c.model instanceof Bigraph)
				return;
			if (c.model instanceof Container)
				checkLayoutCanContainChildren(b, (Container)c.model, c.newLayout);
			checkObjectCanContain(b, scratch.getParentFor(c.model), c.newLayout);
			scratch.setLayoutFor(c.model, c.newLayout);
		} else if (b instanceof BigraphChangeEdgeReposition) {
			/* nothing to do? */
		} else {
			rejectChange(b, "The change was not recognised by the validator");
		}
	}
}
