package dk.itu.big_red.editors.bigraph.commands;

import java.util.ArrayList;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.assistants.PropertyScratchpad;
import dk.itu.big_red.model.changes.ChangeGroup;

public class ModelDeleteCommand extends ChangeCommand {
	@Override
	public Bigraph getTarget() {
		return (Bigraph)super.getTarget();
	}
	
	private ChangeGroup cg = new ChangeGroup();
	
	public ModelDeleteCommand() {
		setChange(cg);
	}
	
	private ArrayList<Object> objects = new ArrayList<Object>();
	
	public void addObject(Object m) {
		if (m != null && !(m instanceof Bigraph) && !(m instanceof Port)) {
			objects.add(m);
			if (m instanceof Link.Connection) {
				setTarget(((Link.Connection)m).getLink().getBigraph());
			} else if (m instanceof Layoutable) {
				setTarget(((Layoutable)m).getBigraph());
			}
		}
	}
	
	private PropertyScratchpad scratch = null;

	public void setTarget(Bigraph target) {
		super.setTarget(target);
		if (scratch == null)
			scratch = new PropertyScratchpad();
	}
	
	private void removePoint(Link l, Point p) {
		cg.add(p.changeDisconnect());
		l.removePoint(scratch, p);
		if (l.getPoints(scratch).size() == 0 && l instanceof Edge) {
			cg.add(l.changeRemove());
			l.getBigraph().removeChild(scratch, l);
		}
	}
	
	private void remove(Object m) {
		if (m instanceof Link.Connection) {
			Link.Connection l = (Link.Connection)m;
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
			cg.add(n.changeRemove());
			n.getParent().removeChild(scratch, n);
		}
	}
	
	@Override
	public ModelDeleteCommand prepare() {
		cg.clear();
		if (scratch != null)
			scratch.clear();
		for (Object m : objects)
			remove(m);
		return this;
	}
}
