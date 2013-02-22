package org.bigraph.model;

import org.bigraph.model.Container.ChangeAddChildDescriptor;
import org.bigraph.model.Container.ChangeRemoveChildDescriptor;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.Layoutable.Identifier;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.names.Namespace;

final class ContainerDescriptorHandler
		extends HandlerUtilities.DescriptorHandlerImpl {
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final PropertyScratchpad scratch = context.getScratch();
		final Resolver resolver = context.getResolver();
		if (change instanceof ChangeAddChildDescriptor) {
			ChangeAddChildDescriptor cd = (ChangeAddChildDescriptor)change;
			Container.Identifier parentI = cd.getParent();
			Layoutable.Identifier childI = cd.getChild();
			
			Container parent =
					tryLookup(cd, parentI, scratch, resolver, Container.class);
			
			if (parent instanceof Node) {
				Control c = ((Node)parent).getControl();
				if (c.getKind(scratch) == Kind.ATOMIC)
					throw new ChangeCreationException(cd,
							"" + c.toString(scratch) +
							" is an atomic control");
			}
			
			Layoutable tmp = instantiate(childI, scratch, resolver);
			Namespace<Layoutable> ns =
					parent.getBigraph(scratch).getNamespace(tmp);
			HandlerUtilities.checkName(scratch, cd, tmp, ns, childI.getName());
			
			if (childI instanceof Node.Identifier)
				tryLookup(cd, ((Node.Identifier)childI).getControl(),
						scratch, resolver, Control.class);
			
			if (!canContain(parentI, childI))
				throw new ChangeCreationException(cd,
						"" + parentI + " can't contain " + childI);
		} else if (change instanceof ChangeRemoveChildDescriptor) {
			ChangeRemoveChildDescriptor co =
					(ChangeRemoveChildDescriptor)change;
			Layoutable ch = tryLookup(co,
					co.getChild(), scratch, resolver, Layoutable.class);
			Container parent = tryLookup(co,
					co.getParent(), scratch, resolver, Container.class);
			
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
						"" + co.getChild() + " isn't connected to " +
						co.getParent());
			
			if (ch.getExtendedDataMap(scratch).size() != 0)
				throw new ChangeCreationException(co,
						"" + co.getChild() + " still has extended data " +
						"that must be removed first");
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
			parent.addChild(l);
		} else if (change instanceof ChangeRemoveChildDescriptor) {
			ChangeRemoveChildDescriptor co =
					(ChangeRemoveChildDescriptor)change;
			Container parent = co.getParent().lookup(null, resolver);
			Layoutable ch = co.getChild().lookup(null, resolver);
			Namespace<Layoutable> ns = ch.getBigraph().getNamespace(ch);
			parent.removeChild(ch);
			ns.remove(ch.getName());
		} else return false;
		return true;
	}
}
