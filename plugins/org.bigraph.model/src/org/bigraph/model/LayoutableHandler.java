package org.bigraph.model;

import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.names.Namespace;

final class LayoutableHandler extends HandlerUtilities.HandlerImpl {
	@Override
	public boolean executeChange(Resolver resolver, IChangeDescriptor b) {
		if (b instanceof Layoutable.ChangeRemove) {
			Layoutable.ChangeRemove c = (Layoutable.ChangeRemove)b;
			Layoutable ch = c.getCreator();
			
			/* beforeApply() implementation follows */
			c.oldName = ch.getName();
			c.oldParent = ch.getParent();
			
			Namespace<Layoutable> ns = ch.getBigraph().getNamespace(ch);
			ch.getParent().removeChild(ch);
			ns.remove(ch.getName());
		} else return false;
		return true;
	}
	
	@Override
	public boolean tryValidateChange(Process process, IChangeDescriptor b)
			throws ChangeCreationException {
		final PropertyScratchpad context = process.getScratch();
		if (b instanceof Layoutable.ChangeRemove) {
			Layoutable.ChangeRemove c = (Layoutable.ChangeRemove)b;
			Layoutable ch = c.getCreator();
			
			if (ch instanceof InnerName)
				if (((InnerName) ch).getLink(context) != null)
					throw new ChangeCreationException(b,
							"The point " + ch + " must be disconnected " +
							"before it can be deleted");
			
			if (ch instanceof Container) {
				if (((Container)ch).getChildren(context).size() != 0)
					throw new ChangeCreationException(b,
							ch + " has child objects which must be " +
							"removed first");
				if (ch instanceof Node) {
					for (Port p : ((Node)ch).getPorts())
						if (p.getLink(context) != null)
							throw new ChangeCreationException(b,
									"The point " + ch + " must be " +
									"disconnected before it can be deleted");
				}
			}
			Container cp = ch.getParent(context);
			if (cp == null)
				throw new ChangeCreationException(b, ch + " has no parent");
		} else return false;
		return true;
	}
}
