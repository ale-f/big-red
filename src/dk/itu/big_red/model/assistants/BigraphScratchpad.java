package dk.itu.big_red.model.assistants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.util.geometry.Rectangle;

/**
 * The BigraphScratchpad is a wrapper around various kinds of {@link
 * Layoutable} which maps objects to copies of themselves, allowing
 * the copies to be modified without affecting the original.
 * @author alec
 *
 */
public class BigraphScratchpad {
	private HashMap<Layoutable, Rectangle> layouts =
			new HashMap<Layoutable, Rectangle>();
	
	private HashMap<Layoutable, Container> parents =
			new HashMap<Layoutable, Container>();
	
	private HashMap<Container, List<Layoutable>> children =
			new HashMap<Container, List<Layoutable>>();
	
	private HashMap<Link, List<Point>> points =
			new HashMap<Link, List<Point>>();
	
	private HashMap<Point, Link> links = new HashMap<Point, Link>();
	
	public void clear() {
		links.clear();
		points.clear();
		layouts.clear();
		parents.clear();
	}
	
	public Rectangle getLayoutFor(Layoutable a) {
		Rectangle b;
		if (!layouts.containsKey(a)) {
			b = a.getLayout();
			layouts.put(a, b);
		} else b = layouts.get(a);
		return b;
	}
	
	public void setLayoutFor(Layoutable a, Rectangle b) {
		layouts.put(a, b);
	}
	
	public Container getParentFor(Layoutable a) {
		Container b;
		if (!parents.containsKey(a)) {
			b = a.getParent();
			parents.put(a, b);
		} else b = parents.get(a);
		return b;
	}
	
	public void setParentFor(Layoutable a, Container b) {
		parents.put(a, b);
	}
	
	public List<Layoutable> getChildrenFor(Container a) {
		List<Layoutable> b;
		if (!children.containsKey(a)) {
			b = new ArrayList<Layoutable>(a.getChildren());
			children.put(a, b);
		} else b = children.get(a);
		return b;
	}
	
	public void removeChildFor(Container a, Layoutable b) {
		getChildrenFor(a).remove(b);
		setParentFor(b, null);
	}
	
	public void addChildFor(Container a, Layoutable b) {
		getChildrenFor(a).add(b);
		setParentFor(b, a);
	}
	
	public Link getLinkFor(Point a) {
		Link b;
		if (!links.containsKey(a)) {
			b = a.getLink();
			links.put(a, b);
		} else b = links.get(a);
		return b;
	}
	
	public void setLinkFor(Point a, Link b) {
		links.put(a, b);
	}
	
	public List<Point> getPointsFor(Link a) {
		List<Point> b;
		if (!points.containsKey(a)) {
			b = new ArrayList<Point>(a.getPoints());
			points.put(a, b);
		} else b = points.get(a);
		return b;
	}
	
	public void removePointFor(Link a, Point b) {
		getPointsFor(a).remove(b);
		setLinkFor(b, null);
	}
	
	public void addPointFor(Link a, Point b) {
		getPointsFor(a).add(b);
		setLinkFor(b, a);
	}

}
