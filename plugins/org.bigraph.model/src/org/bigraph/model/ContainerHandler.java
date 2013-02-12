package org.bigraph.model;

import org.bigraph.model.Control.Kind;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.names.Namespace;

final class ContainerHandler extends HandlerUtilities.HandlerImpl {
	@Override
	public boolean executeChange(Resolver resolver, IChangeDescriptor b) {
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
	public boolean tryValidateChange(Process process, IChangeDescriptor b)
			throws ChangeCreationException {
		final PropertyScratchpad context = process.getScratch();
		if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			
			if (c.child == null || c.name == null)
				throw new ChangeCreationException(b,
						"" + b + " is not ready");
			
			Container container = c.getCreator();
			Bigraph bigraph = container.getBigraph(context);
			
			if (container instanceof Node &&
				((Node)container).getControl().getKind() == Kind.ATOMIC)
				throw new ChangeCreationException(b,
						((Node)container).getControl().getName() +
						" is an atomic control");
			
			HandlerUtilities.checkName(context, c, c.child,
					bigraph.getNamespace(c.child), c.name);

			if (c.child instanceof Edge) {
				if (!(container instanceof Bigraph))
					throw new ChangeCreationException(b,
							"Edges must be children of the top-level Bigraph");
			} else {
				if (c.child instanceof Container)
					if (((Container)c.child).getChildren(context).size() != 0)
						throw new ChangeCreationException(b,
								c.child + " already has child objects");
				if (!canContain(container, c.child))
					throw new ChangeCreationException(b,
							container.getType() + "s can't contain " +
							c.child.getType() + "s");
			}
			
			Container existingParent = c.child.getParent(context);
			if (existingParent != null)
				throw new ChangeCreationException(b,
						c.child + " already has a parent (" +
						existingParent + ")");
		} else return false;
		return true;
	}
}
