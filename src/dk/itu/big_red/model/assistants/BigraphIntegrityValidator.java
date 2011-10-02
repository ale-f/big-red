package dk.itu.big_red.model.assistants;

import java.util.ArrayList;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;
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
import dk.itu.big_red.model.changes.bigraph.BigraphChangeOutlineColour;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeRemoveChild;
import dk.itu.big_red.util.geometry.ReadonlyRectangle;
import dk.itu.big_red.util.geometry.Rectangle;

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
	
	private class QueuedLayoutCheck {
		public Change c;
		public Layoutable l;
		public QueuedLayoutCheck(Change c, Layoutable l) {
			this.c = c; this.l = l;
		}
	}
	private ArrayList<QueuedLayoutCheck> layoutChecks =
		new ArrayList<QueuedLayoutCheck>();
	
	private void runLayoutChecks() throws ChangeRejectedException {
		for (QueuedLayoutCheck i : layoutChecks) {
			Container parent = scratch.getParentFor(i.l);
			ReadonlyRectangle layout = scratch.getLayoutFor(i.l);
			checkObjectCanContain(i.c, parent, layout);
			if (i.l instanceof Container)
				checkLayoutCanContainChildren(i.c, (Container)i.l, layout);
		}
	}
	
	private BigraphScratchpad scratch = new BigraphScratchpad();
	
	private void checkObjectCanContain(Change b, Layoutable o, ReadonlyRectangle nl) throws ChangeRejectedException {
		if (o != null && !(o instanceof Bigraph)) {
			Rectangle tr =
				scratch.getLayoutFor(o).getCopy().setLocation(0, 0);
			if (!tr.contains(nl))
				rejectChange(b,
					"The object can no longer fit into its container");
		}
	}
	
	private void checkLayoutCanContainChildren(Change b, Container c, ReadonlyRectangle nl) throws ChangeRejectedException {
		nl = nl.getCopy().setLocation(0, 0);
		for (Layoutable i : c.getChildren()) {
			ReadonlyRectangle layout = scratch.getLayoutFor(i);
			if (!nl.contains(layout))
				rejectChange(b, "The new size is too small");
		}
	}
	
	@Override
	public void tryValidateChange(Change b)
			throws ChangeRejectedException {
		scratch.clear();
		_tryValidateChange(b);
		runLayoutChecks();
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
			if (c.child instanceof Edge) {
				if (!(c.parent instanceof Bigraph))
					rejectChange(b, "Edges must be children of the top-level Bigraph");
			} else {
				if (!c.parent.canContain(c.child))
					rejectChange(b,
						c.parent.getClass().getSimpleName() + "s can't contain " +
						c.child.getClass().getSimpleName() + "s");
				layoutChecks.add(new QueuedLayoutCheck(b, c.child));
			}
			scratch.setLayoutFor(c.child, c.newLayout);
			scratch.addChildFor(c.parent, c.child);
		} else if (b instanceof BigraphChangeRemoveChild) {
			BigraphChangeRemoveChild c = (BigraphChangeRemoveChild)b;
			scratch.removeChildFor(c.parent, c.child);
		} else if (b instanceof BigraphChangeLayout) {
			BigraphChangeLayout c = (BigraphChangeLayout)b;
			if (c.model instanceof Bigraph)
				rejectChange(b, "Bigraphs cannot be moved or resized");
			layoutChecks.add(new QueuedLayoutCheck(b, c.model));
			scratch.setLayoutFor(c.model, c.newLayout);
		} else if (b instanceof BigraphChangeEdgeReposition) {
			/* nothing to do? */
		} else if (b instanceof BigraphChangeOutlineColour) {
			/* totally nothing to do */
		} else {
			rejectChange(b, "The change was not recognised by the validator");
		}
	}
}
