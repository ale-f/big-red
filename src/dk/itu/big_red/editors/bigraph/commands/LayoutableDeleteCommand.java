package dk.itu.big_red.editors.bigraph.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;

public class LayoutableDeleteCommand extends Command {
	private LayoutableModelObject object = null;
	private Container parent = null;
	
	public void setObject(Object model) {
		if (model instanceof LayoutableModelObject) {
			this.object = (LayoutableModelObject)model;
			this.parent = this.object.getParent();
			if (this.parent == null)
				this.object = null;
		}
	}
	
	@Override
	public boolean canExecute() {
		return (object != null && parent != null);
	}
	
	private Link link = null;
	private ArrayList<Point> points = null;
	
	private HashMap<Point, Link> connections = null;
	
	private void iterativelyRemoveConnections(Container c) {
		if (connections == null)
			connections = new HashMap<Point, Link>();
		if (c instanceof Node) {
			Node j = (Node)c;
			for (Point p : j.getPorts()) {
				Link l = p.getLink();
				if (l != null) {
					connections.put(p, l);
					l.removePoint(p);
				}
			}
		}
		for (LayoutableModelObject i : c.getChildren()) {
			if (i instanceof Container)
				iterativelyRemoveConnections((Container)i);
		}
	}
	
	@Override
	public void execute() {
		if (object instanceof Link) {
			Link object = (Link)this.object;
			if (points == null)
				points = new ArrayList<Point>();
			points.addAll(object.getPoints());
			for (Point i : points)
				object.removePoint(i);
		} else if (object instanceof Point) {
			Point object = (Point)this.object;
			link = object.getLink();
			if (link != null)
				link.removePoint(object);
		} else if (object instanceof Container) {
			iterativelyRemoveConnections((Container)object);
		}
		parent.removeChild(object);
	}
	
	@Override
	public void undo() {
		parent.addChild(object);
		if (object instanceof Link && points != null) {
			Link object = (Link)this.object;
			for (Point i : points)
				object.addPoint(i);
			points.clear();
		} else if (object instanceof Point && link != null) {
			link.addPoint((Point)this.object);
		} else if (object instanceof Container && connections != null) {
			for (Entry<Point, Link> i : connections.entrySet())
				i.getValue().addPoint(i.getKey());
			connections.clear();
		}
	}
	
	@Override
	public void redo() {
		execute();
	}
}
