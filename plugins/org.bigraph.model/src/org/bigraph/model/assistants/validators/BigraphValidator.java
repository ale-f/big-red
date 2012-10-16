package org.bigraph.model.assistants.validators;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.Node;
import org.bigraph.model.Point;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.Port;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;

/**
 * The <strong>BigraphIntegrityValidator</strong> is the basic validator that
 * all changes to {@link Bigraph}s must go through; it checks for both model
 * consistency and visual sensibleness.
 * @author alec
 */
public class BigraphValidator extends ModelObjectValidator {
	private final Bigraph bigraph;
	
	public BigraphValidator(Bigraph bigraph) {
		this.bigraph = bigraph;
	}
	
	private void checkEligibility(
			PropertyScratchpad context, IChange b, Layoutable... l)
			throws ChangeRejectedException {
		for (Layoutable i : l)
			if (i.getBigraph(context) != bigraph)
				throw new ChangeRejectedException(b,
						i + " is not part of this Bigraph");
	}
	
	@Override
	public boolean tryValidateChange(Process process, IChange b)
			throws ChangeRejectedException {
		final PropertyScratchpad context = process.getScratch();
		if (super.tryValidateChange(process, b)) {
			return true;
		} else if (b instanceof Point.ChangeConnect) {
			Point.ChangeConnect c = (Point.ChangeConnect)b;
			checkEligibility(context, b, c.link, c.getCreator());
			if (c.getCreator().getLink(context) != null)
				throw new ChangeRejectedException(b,
						"Connections can only be established to Points that " +
						"aren't already connected");
		} else if (b instanceof Point.ChangeDisconnect) {
			Point.ChangeDisconnect c = (Point.ChangeDisconnect)b;
			checkEligibility(context, b, c.getCreator());
			Link l = c.getCreator().getLink(context);
			if (l == null)
				throw new ChangeRejectedException(b,
						"The Point is already disconnected");
		} else if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			
			if (c.getCreator() instanceof Node &&
				((Node)c.getCreator()).getControl().getKind() == Kind.ATOMIC)
				throw new ChangeRejectedException(b,
						((Node)c.getCreator()).getControl().getName() +
						" is an atomic control");
			
			checkName(context, b, c.child,
					bigraph.getNamespace(c.child), c.name);

			if (c.child instanceof Edge) {
				if (!(c.getCreator() instanceof Bigraph))
					throw new ChangeRejectedException(b,
							"Edges must be children of the top-level Bigraph");
			} else {
				if (c.child instanceof Container)
					if (((Container)c.child).getChildren(context).size() != 0)
						throw new ChangeRejectedException(b,
								c.child + " already has child objects");
				if (!c.getCreator().canContain(c.child))
					throw new ChangeRejectedException(b,
							c.getCreator().getType() + "s can't contain " +
							c.child.getType() + "s");
			}
			
			Container existingParent = c.child.getParent(context);
			if (existingParent != null)
				throw new ChangeRejectedException(b,
						c.child + " already has a parent (" +
						existingParent + ")");
		} else if (b instanceof Layoutable.ChangeRemove) {
			Layoutable.ChangeRemove c = (Layoutable.ChangeRemove)b;
			Layoutable ch = c.getCreator();
			checkEligibility(context, b, ch);
			
			if (ch instanceof InnerName)
				if (((InnerName) ch).getLink(context) != null)
					throw new ChangeRejectedException(b,
							"The point " + ch + " must be disconnected " +
							"before it can be deleted");
			
			if (ch instanceof Container) {
				if (((Container)ch).getChildren(context).size() != 0)
					throw new ChangeRejectedException(b,
							ch + " has child objects which must be " +
							"removed first");
				if (ch instanceof Node) {
					for (Port p : ((Node)ch).getPorts())
						if (p.getLink(context) != null)
							throw new ChangeRejectedException(b,
									"The point " + ch + " must be " +
									"disconnected before it can be deleted");
				}
			}
			Container cp = ch.getParent(context);
			if (cp == null)
				throw new ChangeRejectedException(b, ch + " has no parent");
		} else if (b instanceof Layoutable.ChangeName) {
			Layoutable.ChangeName c = (Layoutable.ChangeName)b;
			checkEligibility(context, b, c.getCreator());
			checkName(context, b, c.getCreator(),
					bigraph.getNamespace(c.getCreator()), c.newName);
		} else return false;
		b.simulate(context);
		return true;
	}
}
