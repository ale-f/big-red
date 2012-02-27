package dk.itu.big_red.model.assistants;

import java.util.ArrayList;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Control.Kind;
import dk.itu.big_red.model.Site.ChangeAlias;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.ChangeValidator;
import dk.itu.big_red.model.namespaces.INamePolicy;
import dk.itu.big_red.model.namespaces.INamespace;
import dk.itu.big_red.utilities.geometry.ReadonlyRectangle;
import dk.itu.big_red.utilities.geometry.Rectangle;

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
	
	private void checkEligibility(Layoutable... l) throws ChangeRejectedException {
		for (Layoutable i : l)
			if (scratch.getBigraphFor(i) != getChangeable())
				rejectChange(i + " is not part of this Bigraph");
	}
	
	@Override
	public void tryValidateChange(Change b)
			throws ChangeRejectedException {
		activeChange = b;
		
		scratch.clear();
		
		layoutChecks.clear();
		
		_tryValidateChange(b);
		
		runLayoutChecks();
		
		activeChange = null;
	}
	
	private void checkName(Change b, Layoutable l, String cdt) throws ChangeRejectedException {
		if (cdt == null)
			rejectChange(b, "Setting an object's name to null is no longer supported");
		INamespace<Layoutable> ns = scratch.getNamespaceFor(l);
		if (ns == null)
			return; /* not subject to any checks */
		if (ns.get(cdt) != null)
			if (!ns.get(cdt).equals(l))
				rejectChange("Names must be unique");
		if (!ns.getPolicy().validate(cdt))
			rejectChange(b, "\"" + cdt + "\" is not a valid name for " + l);
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
			checkEligibility(c.link, c.getCreator());
			if (scratch.getLinkFor(c.getCreator()) != null)
				rejectChange(b,
					"Connections can only be established to Points that " +
					"aren't already connected");
			scratch.addPointFor(c.link, c.getCreator());
		} else if (b instanceof Point.ChangeDisconnect) {
			Point.ChangeDisconnect c = (Point.ChangeDisconnect)b;
			checkEligibility(c.link, c.getCreator());
			if (scratch.getLinkFor(c.getCreator()) == null)
				rejectChange("The Point is already disconnected");
			scratch.removePointFor(c.link, c.getCreator());
		} else if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			
			if (c.getCreator() instanceof Node &&
				((Node)c.getCreator()).getControl().getKind() == Kind.ATOMIC)
				rejectChange(
						((Node)c.getCreator()).getControl().getName() +
						" is an atomic control");
			
			checkName(b, c.child, c.name);

			if (c.child instanceof Edge) {
				if (!(c.getCreator() instanceof Bigraph))
					rejectChange("Edges must be children of the top-level Bigraph");
			} else {
				if (c.child instanceof Container)
					if (scratch.getChildrenFor((Container)c.child).size() != 0)
						rejectChange(b, c.child + " already has child objects");
				if (!c.getCreator().canContain(c.child))
					rejectChange(b,
						c.getCreator().getType() + "s can't contain " +
						c.child.getType() + "s");
				if (!layoutChecks.contains(c.child))
					layoutChecks.add(c.child);
			}
			
			scratch.addChildFor(c.getCreator(), c.child, c.name);
		} else if (b instanceof Container.ChangeRemoveChild) {
			Container.ChangeRemoveChild c = (Container.ChangeRemoveChild)b;
			checkEligibility(c.child, c.getCreator());
			if (c.child instanceof Container)
				if (scratch.getChildrenFor((Container)c.child).size() != 0)
					rejectChange(b, c.child + " has child objects which must be removed first");
			if (scratch.getParentFor(c.child) != c.getCreator())
				rejectChange(c.getCreator() + " is not the parent of " + c.child);
			scratch.removeChildFor(c.getCreator(), c.child);
			
			INamespace<Layoutable> ns = scratch.getNamespaceFor(c.child);
			ns.remove(c.child.getName());
		} else if (b instanceof Layoutable.ChangeLayout) {
			Layoutable.ChangeLayout c = (Layoutable.ChangeLayout)b;
			checkEligibility(c.getCreator());
			if (c.getCreator() instanceof Bigraph)
				rejectChange("Bigraphs cannot be moved or resized");
			if (!layoutChecks.contains(c.getCreator()))
				layoutChecks.add(c.getCreator());
			scratch.setLayoutFor(c.getCreator(), c.newLayout);
		} else if (b instanceof Edge.ChangeReposition) {
			Edge.ChangeReposition c = (Edge.ChangeReposition)b;
			checkEligibility(c.getCreator());
		} else if (b instanceof Colourable.ChangeOutlineColour ||
				b instanceof Colourable.ChangeFillColour ||
				b instanceof ModelObject.ChangeComment) {
			/* totally nothing to do */
		} else if (b instanceof Layoutable.ChangeName) {
			Layoutable.ChangeName c = (Layoutable.ChangeName)b;
			checkEligibility(c.getCreator());
			checkName(b, c.getCreator(), c.newName);
			scratch.setNameFor(c.getCreator(), c.newName);
		} else if (b instanceof ChangeAlias) {
			ChangeAlias c = (ChangeAlias)b;
			/* Although Site aliases don't have to be unique, they should still
			 * be valid (or null) */
			INamePolicy siteNamePolicy =
				scratch.getBigraph().getNamespace(Site.class).getPolicy();
			if (siteNamePolicy != null && c.alias != null)
				if (!siteNamePolicy.validate(c.alias))
					rejectChange("\"" + c.alias + "\" is not a valid alias " +
							"for " + c.getCreator());
		} else {
			rejectChange("The change was not recognised by the validator");
		}
	}
}
