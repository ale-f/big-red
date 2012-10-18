package org.bigraph.model.assistants;

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
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepValidator;

class BigraphValidator implements IStepValidator {
	@Override
	public boolean tryValidateChange(Process process, IChange b)
			throws ChangeRejectedException {
		final PropertyScratchpad context = process.getScratch();
		if (b instanceof Point.ChangeConnect) {
			Point.ChangeConnect c = (Point.ChangeConnect)b;
			if (c.getCreator().getLink(context) != null)
				throw new ChangeRejectedException(b,
						"Connections can only be established to Points that " +
						"aren't already connected");
		} else if (b instanceof Point.ChangeDisconnect) {
			Point.ChangeDisconnect c = (Point.ChangeDisconnect)b;
			Link l = c.getCreator().getLink(context);
			if (l == null)
				throw new ChangeRejectedException(b,
						"The Point is already disconnected");
		} else if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			Bigraph bigraph = c.getCreator().getBigraph(context);
			
			if (c.getCreator() instanceof Node &&
				((Node)c.getCreator()).getControl().getKind() == Kind.ATOMIC)
				throw new ChangeRejectedException(b,
						((Node)c.getCreator()).getControl().getName() +
						" is an atomic control");
			
			ModelObjectValidator.checkName(context, b, c.child,
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
			Bigraph bigraph = c.getCreator().getBigraph(context);
			ModelObjectValidator.checkName(context, b, c.getCreator(),
					bigraph.getNamespace(c.getCreator()), c.newName);
		} else return false;
		return true;
	}
}
