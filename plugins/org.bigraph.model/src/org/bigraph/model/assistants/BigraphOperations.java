package org.bigraph.model.assistants;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.Node;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.Store;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.utilities.FilteringIterable;

import static org.bigraph.model.utilities.CollectionUtilities.collect;

public abstract class BigraphOperations {
	private BigraphOperations() {}
	
	private static IChangeDescriptor simulate(
			IChangeDescriptor cd, PropertyScratchpad context, Resolver r) {
		cd.simulate(context, r);
		return cd;
	}
	
	public static void disconnectPoint(ChangeDescriptorGroup cdg,
			PropertyScratchpad context, Point p) {
		if (cdg == null || context == null || p == null)
			return;
		else if (p.getLink(context) == null)
			return;
		
		cdg.add(simulate(
				new Point.ChangeDisconnectDescriptor(
						p.getIdentifier(context),
						p.getLink(context).getIdentifier(context)),
				context, p.getBigraph(context)));
	}
	
	public static void removeObject(ChangeDescriptorGroup cdg,
			PropertyScratchpad context, Layoutable l) {
		if (cdg == null || context == null || l == null)
			return;
		else if (l.getParent(context) == null)
			return;
		
		Bigraph b = l.getBigraph(context);
		
		if (l instanceof Container) {
			Container c = (Container)l;
			
			if (l instanceof Node) {
				Node n = (Node)l;
				
				for (Port p : n.getPorts())
					disconnectPoint(cdg, context, p);
			}
			
			for (Layoutable ch : collect(c.getChildren(context)))
				removeObject(cdg, context, ch);
		} else if (l instanceof Link) {
			Link li = (Link)l;
			
			for (Point p : collect(li.getPoints(context)))
				disconnectPoint(cdg, context, p);
		} else if (l instanceof Point) {
			Point p = (Point)l;
			
			disconnectPoint(cdg, context, p);
		}
		
		Layoutable.Identifier lid = l.getIdentifier(context);
		Container.Identifier pid = l.getParent(context).getIdentifier(context);
		
		cdg.add(simulate(
				new Store.ToStoreDescriptor(
						lid, Store.getInstance().createID()),
				context, b));
		cdg.add(simulate(
				new Container.ChangeRemoveChildDescriptor(pid, lid),
				context, b));
		
		/* Garbage collection */
		removeNullEdges(cdg, context, b);
	}
	
	public static void reparentObject(ChangeDescriptorGroup cdg,
			PropertyScratchpad context, Layoutable l, Container newParent) {
		if (cdg == null || context == null || l == null || newParent == null)
			return;
		
		Bigraph b = newParent.getBigraph(context);
		Layoutable.Identifier lid = l.getIdentifier(context);
		Container.Identifier
			opid = l.getParent(context).getIdentifier(context),
			npid = newParent.getIdentifier(context);
		
		ChangeDescriptorGroup remove = new ChangeDescriptorGroup();
		removeObject(remove, context, l);
		
		ChangeDescriptorGroup add = remove.inverse();
		add.set(add.indexOf(
						new Container.ChangeAddChildDescriptor(opid, lid)),
				new Container.ChangeAddChildDescriptor(npid, lid));
		simulate(add, context, b);
		
		cdg.addAll(remove);
		cdg.addAll(add);
	}
	
	public static void copyPlace(ChangeDescriptorGroup cdg,
			PropertyScratchpad context, Layoutable l, Container newParent) {
		if (cdg == null || context == null || l == null || newParent == null)
			return;
		
		Bigraph
			b2 = newParent.getBigraph(context);
		
		Layoutable.Identifier newID = l.getIdentifier(context).getRenamed(
				b2.getNamespace(l).getNextName(context));
		
		cdg.add(simulate(
				new Container.ChangeAddChildDescriptor(
						newParent.getIdentifier(context), newID),
				context, b2));
		
		if (l instanceof Container)
			for (Layoutable m : ((Container)l).getChildren(context))
				copyPlace(cdg, context, m,
						((Container.Identifier)newID).lookup(context, b2));
	}
	
	public static void removeNullEdges(ChangeDescriptorGroup cdg,
			PropertyScratchpad context, Bigraph b) {
		if (cdg == null || context == null || b == null)
			return;
		
		for (Edge ch : new FilteringIterable<Edge>(
				Edge.class, collect(b.getChildren(context))))
			if (ch.getPoints(context).size() == 0)
				removeObject(cdg, context, ch);
	}
}
