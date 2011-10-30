package dk.itu.big_red.editors.bigraph.commands;

import java.util.ArrayList;
import java.util.List;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.assistants.BigraphScratchpad;
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
	
	private ArrayList<ModelObject> objects = new ArrayList<ModelObject>();
	
	public static final String GROUP_MAP_ID =
		"dk.itu.big_red.editors.bigraph.commands.ModelDeleteCommand";
	
	public void addObject(ModelObject m) {
		if (m != null && !(m instanceof Bigraph))
			objects.add(m);
	}
	
	private BigraphScratchpad scratch = null;

	public void setTarget(Bigraph target) {
		super.setTarget(target);
		if (scratch == null)
			scratch = new BigraphScratchpad(target);
	}
	
	private void removePoint(Link l, Point p) {
		cg.add(p.changeDisconnect(l));
		scratch.removePointFor(l, p);
		if (scratch.getPointsFor(l).size() == 0 && l instanceof Edge) {
			cg.add(l.changeName(null),
					l.getBigraph().changeRemoveChild(l));
			scratch.removeChildFor(l.getBigraph(), l);
		}
	}
	
	private void remove(ModelObject m) {
		if (m instanceof Link.Connection) {
			Link.Connection l = (Link.Connection)m;
			Link link = l.getLink(); Point point = l.getPoint();
			setTarget(link.getBigraph());
			removePoint(link, point);
		} else if (m instanceof Layoutable) {
			Layoutable n = (Layoutable)m;
			setTarget(n.getBigraph());
			if (n instanceof Container) {
				Container c = (Container)n;
				recursivelyRemoveConnections(c);
			} else if (n instanceof Link) {
				Link l = (Link)n;
				List<Point> points =
					new ArrayList<Point>(scratch.getPointsFor(l));
				for (Point p : points)
					removePoint(l, p);
				if (l instanceof Edge)
					return;
			} else if (n instanceof Point) {
				Point p = (Point)n;
				if (scratch.getLinkFor(p) != null)
					removePoint(p.getLink(), p);
			}
			cg.add(n.changeName(null),
					n.getParent().changeRemoveChild(n));
			scratch.removeChildFor(n.getParent(), n);
		}
	}
	
	private void recursivelyRemoveConnections(Container c) {
		if (c instanceof Node) {
			Node j = (Node)c;
			for (Point p : j.getPorts()) {
				Link l = scratch.getLinkFor(p);
				if (l != null)
					removePoint(l, p);
			}
		}
		for (Layoutable i : scratch.getChildrenFor(c)) {
			if (i instanceof Container)
				recursivelyRemoveConnections((Container)i);
		}
	}
	
	private boolean parentScheduledForDeletion(Layoutable i) {
		Container parent = i.getParent();
		while (parent != null) {
			if (objects.contains(parent))
				return true;
			parent = parent.getParent();
		}
		return false;
	}
	
	private boolean linkOrPointScheduledForDeletion(Link.Connection l) {
		if (objects.contains(l.getLink()) ||
			objects.contains(l.getPoint())) {
			return true;
		} else if (l.getPoint() instanceof Port) {
			return parentScheduledForDeletion(l.getPoint());
		}
		return false;
	}

	private boolean allIsWell(Layoutable m) {
		return m.getBigraph() != null;
	}
	
	@Override
	public void prepare() {
		cg.clear();
		if (scratch != null)
			scratch.clear();
		for (ModelObject m : objects) {
			if (!(m instanceof Port) &&
				(m instanceof Layoutable &&
					!parentScheduledForDeletion((Layoutable)m) &&
					allIsWell((Layoutable)m)) ||
				(m instanceof Link.Connection &&
					!linkOrPointScheduledForDeletion((Link.Connection)m)))
				remove(m);
		}
	}
}
