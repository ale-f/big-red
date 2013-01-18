package org.bigraph.model;

import org.bigraph.model.Control.Kind;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.names.Namespace;

final class ContainerHandler extends DescriptorHandlerUtilities.HandlerImpl {
	@Override
	public boolean executeChange(IChange b) {
		if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			Namespace<Layoutable> ns =
					c.getCreator().getBigraph().getNamespace(c.child);
			c.child.setName(ns.put(c.name, c.child));
			c.getCreator().addChild(c.child);
		} else return false;
		return true;
	}
	
	private static boolean canContain(Container c, Layoutable l) {
		return
			(c instanceof Bigraph &&
				(l instanceof Edge ||
				 l instanceof OuterName ||
				 l instanceof InnerName ||
				 l instanceof Root)) ||
			((c instanceof Node || c instanceof Root) &&
				(l instanceof Node ||
				 l instanceof Site));
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
			
			NamedModelObjectHandler.checkName(context, b, c.child,
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
				if (!canContain(container, c.child))
					throw new ChangeRejectedException(b,
							container.getType() + "s can't contain " +
							c.child.getType() + "s");
			}
			
			Container existingParent = c.child.getParent(context);
			if (existingParent != null)
				throw new ChangeRejectedException(b,
						c.child + " already has a parent (" +
						existingParent + ")");
		} else return false;
		return true;
	}
}
