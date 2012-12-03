package org.bigraph.model;

import org.bigraph.model.Layoutable.ChangeRemoveDescriptor;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.experimental.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.experimental.IDescriptorStepValidator;
import org.bigraph.model.names.Namespace;

final class LayoutableDescriptorHandler
		implements IDescriptorStepExecutor, IDescriptorStepValidator {
	@Override
	public boolean executeChange(Resolver resolver, IChangeDescriptor change) {
		if (change instanceof ChangeRemoveDescriptor) {
			ChangeRemoveDescriptor co = (ChangeRemoveDescriptor)change;
			Container parent = co.getParent().lookup(null, resolver);
			Layoutable ch = co.getTarget().lookup(null, resolver);
			Namespace<Layoutable> ns = ch.getBigraph().getNamespace(ch);
			parent.removeChild(ch);
			ns.remove(ch.getName());
		} else return false;
		return true;
	}
	
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final Resolver resolver = context.getResolver();
		final PropertyScratchpad scratch = context.getScratch();
		if (change instanceof ChangeRemoveDescriptor) {
			ChangeRemoveDescriptor co = (ChangeRemoveDescriptor)change;
			Layoutable ch = co.getTarget().lookup(scratch, resolver);
			Container parent = co.getParent().lookup(scratch, resolver);
			
			if (ch == null)
				throw new ChangeCreationException(co,
						"" + co.getTarget() + ": lookup failed");
			if (parent == null)
				throw new ChangeCreationException(co,
						"" + co.getParent() + ": lookup failed");
			
			if (ch instanceof InnerName)
				if (((InnerName)ch).getLink(scratch) != null)
					throw new ChangeCreationException(co,
							"The point " + ch.toString(scratch) +
							" must be disconnected before it can be deleted");
			
			if (ch instanceof Container) {
				Container container = (Container)ch;
				if (container.getChildren(scratch).size() > 0)
					throw new ChangeCreationException(co,
							"" + ch.toString(scratch) + " has child objects " +
							"which must be deleted first");
				if (container instanceof Node) {
					for (Port p : ((Node)container).getPorts())
						if (p.getLink(scratch) != null)
							throw new ChangeCreationException(co,
									"" + ch.toString(scratch) + " must be " +
									"disconnected before it can be deleted");
				}
			}
			
			Container currentParent = ch.getParent(scratch);
			if (currentParent == null ||
					!parent.getIdentifier(scratch).equals(co.getParent()))
				throw new ChangeCreationException(co,
						"" + co.getTarget() + " isn't connected to " +
						co.getParent());
		} else return false;
		return true;
	}
}
