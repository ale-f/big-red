package org.bigraph.model;

import org.bigraph.model.Container.ChangeAddChildDescriptor;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.Layoutable.Identifier;
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
	
	static Layoutable instantiate(
			Identifier id, PropertyScratchpad context, Resolver r) {
		if (id instanceof Root.Identifier) {
			return new Root();
		} else if (id instanceof Site.Identifier) {
			return new Site();
		} else if (id instanceof InnerName.Identifier) {
			return new InnerName();
		} else if (id instanceof OuterName.Identifier) {
			return new OuterName();
		} else if (id instanceof Edge.Identifier) {
			return new Edge();
		} else if (id instanceof Node.Identifier) {
			return new Node(((Node.Identifier)id).getControl().
					lookup(context, r));
		} else return null;
	}
	
	@Override
	public boolean executeChange(Resolver resolver, IChangeDescriptor change) {
		if (change instanceof ChangeAddChildDescriptor) {
			ChangeAddChildDescriptor cd = (ChangeAddChildDescriptor)change;
			Container.Identifier parentI = cd.getParent();
			Layoutable.Identifier childI = cd.getChild();
			
			Container parent = parentI.lookup(null, resolver);
			Layoutable l = instantiate(childI, null, parent.getBigraph());
			
			Namespace<Layoutable> ns = parent.getBigraph().getNamespace(l);
			l.setName(ns.put(childI.getName(), l));
			parent.addChild(-1, l);
		} else return false;
		return true;
	}
}
