package dk.itu.big_red.model.assistants;

import java.util.ArrayList;
import java.util.Map;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.ChangeValidator;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeEdgeReposition;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeLayout;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeName;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeOutlineColour;
import dk.itu.big_red.util.geometry.ReadonlyRectangle;
import dk.itu.big_red.util.geometry.Rectangle;

/**
 * The <strong>BigraphIntegrityValidator</strong> is the basic validator that
 * all changes to {@link Bigraph}s must go through; it checks for both model
 * consistency and visual sensibleness.
 * @author alec
 *
 */
public class BigraphIntegrityValidator extends ChangeValidator<Bigraph> {
	private BigraphScratchpad scratch = null;
	
	public BigraphIntegrityValidator(Bigraph changeable) {
		super(changeable);
		scratch = new BigraphScratchpad(changeable);
	}
	
	private class QueuedLayoutableCheck {
		public Change c;
		public Layoutable l;
		public QueuedLayoutableCheck(Change c, Layoutable l) {
			this.c = c; this.l = l;
		}
	}
	private ArrayList<QueuedLayoutableCheck> layoutChecks =
		new ArrayList<QueuedLayoutableCheck>();
	
	private void runLayoutChecks() throws ChangeRejectedException {
		for (QueuedLayoutableCheck i : layoutChecks) {
			Container parent = scratch.getParentFor(i.l);
			ReadonlyRectangle layout = scratch.getLayoutFor(i.l);
			checkObjectCanContain(i.c, parent, layout);
			if (i.l instanceof Container)
				checkLayoutCanContainChildren(i.c, (Container)i.l, layout);
		}
	}
	
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
	
	private boolean checkNames = false;
	
	private void recursivelyCheckNames(Change b, Container c) throws ChangeRejectedException {
		for (Layoutable i : scratch.getChildrenFor(c)) {
			Map<String, Layoutable> ns = scratch.getNamespaceFor(i);
			String name = i.getName();
			if (name == null) {
				if (!ns.containsValue(i))
					rejectChange(b, i.toString() + " doesn't have a name");
			} else {
				if (!ns.get(name).equals(i))
					rejectChange(b, "The name \"" + name + "\" is already in use");
			}
			
			if (i instanceof Container)
				recursivelyCheckNames(b, (Container)i);
		}
	}
	
	@Override
	public void tryValidateChange(Change b)
			throws ChangeRejectedException {
		scratch.clear();
		
		layoutChecks.clear();
		checkNames = false;
		
		_tryValidateChange(b);
		
		runLayoutChecks();
		if (checkNames)
			recursivelyCheckNames(b, getChangeable());
	}
	
	protected void _tryValidateChange(Change b)
			throws ChangeRejectedException {
		if (!b.isReady()) {
			rejectChange(b, "The Change is not ready");
		} else if (b instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)b)
				_tryValidateChange(c);
		} else if (b instanceof Point.ChangeConnect) {
			Point.ChangeConnect c = (Point.ChangeConnect)b;
			if (scratch.getLinkFor(c.point) != null)
				rejectChange(b,
					"Connections can only be established to Points that " +
					"aren't already connected");
			scratch.addPointFor(c.link, c.point);
		} else if (b instanceof Point.ChangeDisconnect) {
			Point.ChangeDisconnect c = (Point.ChangeDisconnect)b;
			if (scratch.getLinkFor(c.point) == null)
				rejectChange(b, "The Point is already disconnected");
			scratch.removePointFor(c.link, c.point);
		} else if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			if (c.child instanceof Edge) {
				if (!(c.parent instanceof Bigraph))
					rejectChange(b, "Edges must be children of the top-level Bigraph");
			} else {
				if (!c.parent.canContain(c.child))
					rejectChange(b,
						c.parent.getClass().getSimpleName() + "s can't contain " +
						c.child.getClass().getSimpleName() + "s");
				layoutChecks.add(new QueuedLayoutableCheck(b, c.child));
			}
			scratch.addChildFor(c.parent, c.child);
			checkNames = true;
		} else if (b instanceof Container.ChangeRemoveChild) {
			Container.ChangeRemoveChild c = (Container.ChangeRemoveChild)b;
			scratch.removeChildFor(c.parent, c.child);
		} else if (b instanceof BigraphChangeLayout) {
			BigraphChangeLayout c = (BigraphChangeLayout)b;
			if (c.model instanceof Bigraph)
				rejectChange(b, "Bigraphs cannot be moved or resized");
			layoutChecks.add(new QueuedLayoutableCheck(b, c.model));
			scratch.setLayoutFor(c.model, c.newLayout);
		} else if (b instanceof BigraphChangeEdgeReposition) {
			/* nothing to do? */
		} else if (b instanceof BigraphChangeOutlineColour) {
			/* totally nothing to do */
		} else if (b instanceof BigraphChangeName) {
			BigraphChangeName c = (BigraphChangeName)b;
			Map<String, Layoutable> ns = scratch.getNamespaceFor(c.model);
			if (c.newName != null && ns.get(c.newName) != null)
				if (!ns.get(c.newName).equals(c.model))
					rejectChange(b, "Names must be unique");
			scratch.setNameFor(c.model, c.newName);
			checkNames = true;
		} else {
			rejectChange(b, "The change was not recognised by the validator");
		}
	}
}
