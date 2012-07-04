package org.bigraph.model.assistants.validators;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.Node;
import org.bigraph.model.Point;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.names.Namespace;

/**
 * The <strong>BigraphIntegrityValidator</strong> is the basic validator that
 * all changes to {@link Bigraph}s must go through; it checks for both model
 * consistency and visual sensibleness.
 * @author alec
 *
 */
public class BigraphIntegrityValidator extends ModelObjectValidator<Bigraph> {
	public BigraphIntegrityValidator(Bigraph changeable) {
		super(changeable);
	}
	
	private void checkEligibility(Change b, Layoutable... l)
			throws ChangeRejectedException {
		for (Layoutable i : l)
			if (i.getBigraph(getScratch()) != getChangeable())
				rejectChange(b, i + " is not part of this Bigraph");
	}
	
	private void checkName(Change b, Layoutable l, String cdt)
			throws ChangeRejectedException {
		if (cdt == null)
			rejectChange(b, "Setting an object's name to null is no longer supported");
		Namespace<Layoutable> ns =
				getChangeable().getNamespace(Bigraph.getNSI(l));
		if (ns == null)
			return; /* not subject to any checks */
		if (ns.get(getScratch(), cdt) != null)
			if (!ns.get(getScratch(), cdt).equals(l))
				rejectChange(b, "Names must be unique");
		if (ns.getPolicy().normalise(cdt) == null)
			rejectChange(b, "\"" + cdt + "\" is not a valid name for " + l);
	}
	
	@Override
	protected Change doValidateChange(Change b)
			throws ChangeRejectedException {
		if (super.doValidateChange(b) == null) {
			/* do nothing */
		} else if (b instanceof Point.ChangeConnect) {
			Point.ChangeConnect c = (Point.ChangeConnect)b;
			checkEligibility(b, c.link, c.getCreator());
			if (c.getCreator().getLink(getScratch()) != null)
				rejectChange(b,
					"Connections can only be established to Points that " +
					"aren't already connected");
			c.link.addPoint(getScratch(), c.getCreator());
		} else if (b instanceof Point.ChangeDisconnect) {
			Point.ChangeDisconnect c = (Point.ChangeDisconnect)b;
			checkEligibility(b, c.getCreator());
			Link l = c.getCreator().getLink(getScratch());
			if (l == null)
				rejectChange(b, "The Point is already disconnected");
			l.removePoint(getScratch(), c.getCreator());
		} else if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			
			if (c.getCreator() instanceof Node &&
				((Node)c.getCreator()).getControl().getKind() == Kind.ATOMIC)
				rejectChange(b,
						((Node)c.getCreator()).getControl().getName() +
						" is an atomic control");
			
			checkName(b, c.child, c.name);

			if (c.child instanceof Edge) {
				if (!(c.getCreator() instanceof Bigraph))
					rejectChange(b,
						"Edges must be children of the top-level Bigraph");
			} else {
				if (c.child instanceof Container)
					if (((Container)c.child).getChildren(getScratch()).size() != 0)
						rejectChange(b, c.child + " already has child objects");
				if (!c.getCreator().canContain(c.child))
					rejectChange(b,
						c.getCreator().getType() + "s can't contain " +
						c.child.getType() + "s");
			}
			
			Container existingParent = c.child.getParent(getScratch());
			if (existingParent != null)
				rejectChange(b, c.child +
						" already has a parent (" + existingParent + ")");
			
			c.getCreator().addChild(getScratch(), c.child, c.name);
		} else if (b instanceof Layoutable.ChangeRemove) {
			Layoutable.ChangeRemove c = (Layoutable.ChangeRemove)b;
			Layoutable ch = c.getCreator();
			checkEligibility(b, ch);
			if (ch instanceof Container)
				if (((Container)ch).getChildren(getScratch()).size() != 0)
					rejectChange(b, ch + " has child objects which must be removed first");
			Container cp = ch.getParent(getScratch());
			if (cp == null)
				rejectChange(b, ch + " has no parent");
			cp.removeChild(getScratch(), ch);
			getChangeable().getNamespace(Bigraph.getNSI(ch)).
				remove(getScratch(), ch.getName());
		} else if (b instanceof Layoutable.ChangeName) {
			Layoutable.ChangeName c = (Layoutable.ChangeName)b;
			checkEligibility(b, c.getCreator());
			checkName(b, c.getCreator(), c.newName);
			c.getCreator().setName(getScratch(), c.newName);
		} else return b;
		return null;
	}
}
