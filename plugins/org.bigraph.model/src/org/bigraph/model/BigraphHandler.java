package org.bigraph.model;

import java.util.List;

import org.bigraph.model.Control.Kind;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;
import org.bigraph.model.names.Namespace;

final class BigraphHandler implements IStepExecutor, IStepValidator {
	@Override
	public boolean executeChange(IChange b) {
		if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			Namespace<Layoutable> ns =
					c.getCreator().getBigraph().getNamespace(c.child);
			c.child.setName(ns.put(c.name, c.child));
			c.getCreator().addChild(c.position, c.child);
		} else if (b instanceof Layoutable.ChangeRemove) {
			Layoutable.ChangeRemove c = (Layoutable.ChangeRemove)b;
			Layoutable ch = c.getCreator();
			Namespace<Layoutable> ns = ch.getBigraph().getNamespace(ch);
			ch.getParent().removeChild(ch);
			ns.remove(ch.getName());
		} else return false;
		return true;
	}
	
	@Override
	public boolean tryValidateChange(Process process, IChange b)
			throws ChangeRejectedException {
		final PropertyScratchpad context = process.getScratch();
		if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			Container container = c.getCreator();
			Bigraph bigraph = container.getBigraph(context);
			
			if (container instanceof Node &&
				((Node)container).getControl().getKind() == Kind.ATOMIC)
				throw new ChangeRejectedException(b,
						((Node)container).getControl().getName() +
						" is an atomic control");
			
			ModelObjectHandler.checkName(context, b, c.child,
					bigraph.getNamespace(c.child), c.name);

			if (c.child instanceof Edge) {
				if (!(container instanceof Bigraph))
					throw new ChangeRejectedException(b,
							"Edges must be children of the top-level Bigraph");
			} else {
				if (c.child instanceof Container)
					if (((Container)c.child).getChildren(context).size() != 0)
						throw new ChangeRejectedException(b,
								c.child + " already has child objects");
				if (!container.canContain(c.child))
					throw new ChangeRejectedException(b,
							container.getType() + "s can't contain " +
							c.child.getType() + "s");
			}
			
			Container existingParent = c.child.getParent(context);
			if (existingParent != null)
				throw new ChangeRejectedException(b,
						c.child + " already has a parent (" +
						existingParent + ")");
			
			List<? extends Layoutable> siblings =
					container.getChildren(context);
			if (c.position < -1 || c.position > siblings.size())
				throw new ChangeRejectedException(b,
						"" + c.position + " is not a valid position for " +
						c.child);
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
		} else return false;
		return true;
	}
}
