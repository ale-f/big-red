package dk.itu.big_red.model.assistants;

import java.util.ArrayList;
import java.util.Map;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.ChangeValidator;
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
	private Change activeChange = null;
	
	public BigraphIntegrityValidator(Bigraph changeable) {
		super(changeable);
		scratch = new BigraphScratchpad(changeable);
	}
	
	private ArrayList<Layoutable> layoutChecks =
		new ArrayList<Layoutable>();
	
	protected void rejectChange(String rationale)
			throws ChangeRejectedException {
		super.rejectChange(activeChange, rationale);
	}
	
	private void runLayoutChecks() throws ChangeRejectedException {
		for (Layoutable i : layoutChecks) {
			Container parent = scratch.getParentFor(i);
			ReadonlyRectangle layout = scratch.getLayoutFor(i);
			checkObjectCanContain(parent, layout);
			if (i instanceof Container)
				checkLayoutCanContainChildren((Container)i, layout);
		}
	}
	
	private void checkObjectCanContain(Layoutable o, ReadonlyRectangle nl) throws ChangeRejectedException {
		if (o != null && !(o instanceof Bigraph)) {
			Rectangle tr =
				scratch.getLayoutFor(o).getCopy().setLocation(0, 0);
			if (!tr.contains(nl))
				rejectChange(
					"The object can no longer fit into its container");
		}
	}
	
	private void checkLayoutCanContainChildren(Container c, ReadonlyRectangle nl) throws ChangeRejectedException {
		nl = nl.getCopy().setLocation(0, 0);
		for (Layoutable i : c.getChildren()) {
			ReadonlyRectangle layout = scratch.getLayoutFor(i);
			if (!nl.contains(layout))
				rejectChange("The new size is too small");
		}
	}
	
	private boolean checkNames = false;
	
	private void recursivelyCheckNames(Container c) throws ChangeRejectedException {
		for (Layoutable i : scratch.getChildrenFor(c)) {
			Map<String, Layoutable> ns = scratch.getNamespaceFor(i);
			String name = i.getName();
			if (name == null) {
				if (!ns.containsValue(i))
					rejectChange(i.toString() + " doesn't have a name");
			} else {
				if (!ns.get(name).equals(i))
					rejectChange("The name \"" + name + "\" is already in use");
			}
			
			if (i instanceof Container)
				recursivelyCheckNames((Container)i);
		}
	}
	
	@Override
	public void tryValidateChange(Change b)
			throws ChangeRejectedException {
		activeChange = b;
		
		scratch.clear();
		
		layoutChecks.clear();
		checkNames = false;
		
		_tryValidateChange(b);
		
		runLayoutChecks();
		if (checkNames)
			recursivelyCheckNames(getChangeable());
		
		activeChange = null;
	}
	
	protected void _tryValidateChange(Change b)
			throws ChangeRejectedException {
		if (!b.isReady()) {
			rejectChange("The Change is not ready");
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
				rejectChange("The Point is already disconnected");
			scratch.removePointFor(c.link, c.point);
		} else if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			if (c.child instanceof Edge) {
				if (!(c.parent instanceof Bigraph))
					rejectChange("Edges must be children of the top-level Bigraph");
			} else {
				if (!c.parent.canContain(c.child))
					rejectChange(b,
						c.parent.getClass().getSimpleName() + "s can't contain " +
						c.child.getClass().getSimpleName() + "s");
				if (!layoutChecks.contains(c.child))
					layoutChecks.add(c.child);
			}
			scratch.addChildFor(c.parent, c.child);
			checkNames = true;
		} else if (b instanceof Container.ChangeRemoveChild) {
			Container.ChangeRemoveChild c = (Container.ChangeRemoveChild)b;
			if (scratch.getParentFor(c.child) != c.parent)
				rejectChange(c.parent + " is not the parent of " + c.child);
			scratch.removeChildFor(c.parent, c.child);
		} else if (b instanceof Layoutable.ChangeLayout) {
			Layoutable.ChangeLayout c = (Layoutable.ChangeLayout)b;
			if (c.model instanceof Bigraph)
				rejectChange("Bigraphs cannot be moved or resized");
			if (!layoutChecks.contains(c.model))
				layoutChecks.add(c.model);
			scratch.setLayoutFor(c.model, c.newLayout);
		} else if (b instanceof Edge.ChangeReposition) {
			/* nothing to do? */
		} else if (b instanceof Colourable.ChangeOutlineColour ||
				b instanceof Colourable.ChangeFillColour) {
			/* totally nothing to do */
		} else if (b instanceof Layoutable.ChangeName) {
			Layoutable.ChangeName c = (Layoutable.ChangeName)b;
			Map<String, Layoutable> ns = scratch.getNamespaceFor(c.model);
			if (c.newName != null && ns.get(c.newName) != null)
				if (!ns.get(c.newName).equals(c.model))
					rejectChange("Names must be unique");
			scratch.setNameFor(c.model, c.newName);
			checkNames = true;
		} else {
			rejectChange("The change was not recognised by the validator");
		}
	}
}
