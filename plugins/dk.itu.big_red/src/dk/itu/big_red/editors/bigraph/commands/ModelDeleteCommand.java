package dk.itu.big_red.editors.bigraph.commands;

import java.util.ArrayList;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.Node;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;

import dk.itu.big_red.editors.bigraph.parts.LinkPart;

public class ModelDeleteCommand extends ChangeCommand {
	private ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
	
	public ModelDeleteCommand() {
		setChange(cg);
	}
	
	private ArrayList<Object> objects = new ArrayList<Object>();
	
	public void addObject(Object m) {
		if (m != null && !(m instanceof Bigraph) && !(m instanceof Port)) {
			objects.add(m);
			if (m instanceof LinkPart.Connection) {
				setTarget(((LinkPart.Connection)m).getLink().getBigraph());
			} else if (m instanceof Layoutable) {
				setTarget(((Layoutable)m).getBigraph());
			}
		}
	}
	
	private PropertyScratchpad scratch = null;

	public void setTarget(Bigraph target) {
		super.setContext(target);
		if (scratch == null)
			scratch = new PropertyScratchpad();
	}
	
	private void removePoint(Link l, Point p) {
		cg.add(scratch.executeChange(new BoundDescriptor(p.getBigraph(scratch),
				new Point.ChangeDisconnectDescriptor(
						p.getIdentifier(scratch), l.getIdentifier(scratch)))));
		if (l.getPoints(scratch).size() == 0 && l instanceof Edge) {
			cg.add(scratch.executeChange(l.changeRemove()));
		}
	}
	
	private void remove(Object m) {
		if (m instanceof LinkPart.Connection) {
			LinkPart.Connection l = (LinkPart.Connection)m;
			Link link = l.getLink(); Point point = l.getPoint();
			if (point.getLink(scratch) != link)
				return; /* connection already destroyed */
			setTarget(link.getBigraph());
			removePoint(link, point);
		} else if (m instanceof Layoutable) {
			Layoutable n = (Layoutable)m;
			if (n.getParent(scratch) == null)
				return;
			
			if (n instanceof Container) {
				Container c = (Container)n;
				
				if (n instanceof Node) {
					Node j = (Node)n;
					for (Point p : j.getPorts()) {
						Link l = p.getLink(scratch);
						if (l != null)
							removePoint(l, p);
					}
				}
				
				for (Layoutable i :
					new ArrayList<Layoutable>(c.getChildren(scratch)))
					remove(i);
			} else if (n instanceof Link) {
				Link l = (Link)n;
				for (Point p : new ArrayList<Point>(l.getPoints(scratch)))
					removePoint(l, p);
				if (l instanceof Edge)
					return;
			} else if (n instanceof Point) {
				Point p = (Point)n;
				if (p.getLink(scratch) != null)
					removePoint(p.getLink(scratch), p);
			}
			cg.add(scratch.executeChange(n.changeRemove()));
		}
	}
	
	@Override
	public void prepare() {
		cg.clear();
		if (scratch != null)
			scratch.clear();
		for (Object m : objects)
			remove(m);
	}
}
