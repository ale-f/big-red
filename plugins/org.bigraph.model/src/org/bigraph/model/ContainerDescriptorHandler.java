package org.bigraph.model;

import org.bigraph.model.Container.ChangeAddChildDescriptor;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.experimental.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.experimental.IDescriptorStepValidator;
import org.bigraph.model.names.Namespace;

final class ContainerDescriptorHandler
		implements IDescriptorStepExecutor, IDescriptorStepValidator {
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final PropertyScratchpad scratch = context.getScratch();
		final Resolver resolver = context.getResolver();
		if (change instanceof ChangeAddChildDescriptor) {
			ChangeAddChildDescriptor cd = (ChangeAddChildDescriptor)change;
			Container.Identifier parentI = cd.getParent();
			Layoutable.Identifier childI = cd.getChild();
			
			Container parent = parentI.lookup(scratch, resolver);
			if (parent == null)
				throw new ChangeCreationException(cd,
						"lookup failed: " + parentI);
			
			if (parent instanceof Node) {
				Control c = ((Node)parent).getControl();
				if (c.getKind(scratch) == Kind.ATOMIC)
					throw new ChangeCreationException(cd,
							"" + c.toString(scratch) +
							" is an atomic control");
			}
			
			if (childI instanceof Node.Identifier) {
				Node.Identifier nodeI = (Node.Identifier)childI;
				Control.Identifier controlI = nodeI.getControl();
				if (controlI.lookup(scratch, resolver) == null)
					throw new ChangeCreationException(cd,
							"lookup failed: " + controlI);
			}
			
			if (!canContain(parentI, childI))
				throw new ChangeCreationException(cd,
						"" + parentI + " can't contain " + childI);
		} else return false;
		return true;
	}

	private static boolean canContain(
			Container.Identifier c, Layoutable.Identifier l) {
		return
			(c instanceof Bigraph.Identifier &&
				(l instanceof Edge.Identifier ||
				 l instanceof OuterName.Identifier ||
				 l instanceof InnerName.Identifier ||
				 l instanceof Root.Identifier)) ||
			((c instanceof Node.Identifier || c instanceof Root.Identifier) &&
				(l instanceof Node.Identifier ||
				 l instanceof Site.Identifier));
	}
	
	@Override
	public boolean executeChange(Resolver resolver, IChangeDescriptor change) {
		if (change instanceof ChangeAddChildDescriptor) {
			ChangeAddChildDescriptor cd = (ChangeAddChildDescriptor)change;
			Container.Identifier parentI = cd.getParent();
			Layoutable.Identifier childI = cd.getChild();
			
			Container parent = parentI.lookup(null, resolver);
			
			Layoutable l;
			if (childI instanceof Root.Identifier) {
				l = new Root();
			} else if (childI instanceof Site.Identifier) {
				l = new Site();
			} else if (childI instanceof InnerName.Identifier) {
				l = new InnerName();
			} else if (childI instanceof OuterName.Identifier) {
				l = new OuterName();
			} else if (childI instanceof Edge.Identifier) {
				l = new Edge();
			} else if (childI instanceof Node.Identifier) {
				l = new Node(((Node.Identifier)childI).getControl().
						lookup(null, parent.getBigraph()));
			} else l = null;
			
			Namespace<Layoutable> ns = parent.getBigraph().getNamespace(l);
			l.setName(ns.put(childI.getName(), l));
			parent.addChild(-1, l);
		} else return false;
		return true;
	}
}
