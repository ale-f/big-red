package dk.itu.big_red.model.assistants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;

/**
 * The BigraphScratchpad is a wrapper around various kinds of {@link
 * LayoutableModelObject} which maps objects to copies of themselves, allowing
 * the copies to be modified without affecting the original.
 * @author alec
 *
 */
public class BigraphScratchpad {
	private HashMap<LayoutableModelObject, Rectangle> layouts =
			new HashMap<LayoutableModelObject, Rectangle>();
	
	private HashMap<LayoutableModelObject, Container> parents =
			new HashMap<LayoutableModelObject, Container>();
	
	private HashMap<Container, List<LayoutableModelObject>> children =
			new HashMap<Container, List<LayoutableModelObject>>();
	
	private HashMap<Link, List<Point>> points =
			new HashMap<Link, List<Point>>();
	
	private HashMap<Point, Link> links = new HashMap<Point, Link>();
	
	public void clear() {
		links.clear();
		points.clear();
		layouts.clear();
		parents.clear();
	}
	
	public Rectangle getLayoutFor(LayoutableModelObject a) {
		Rectangle b;
		if (!layouts.containsKey(a)) {
			b = a.getLayout();
			layouts.put(a, b);
		} else b = layouts.get(a);
		return b;
	}
	
	public void setLayoutFor(LayoutableModelObject a, Rectangle b) {
		layouts.put(a, b);
	}
	
	public Container getParentFor(LayoutableModelObject a) {
		Container b;
		if (!parents.containsKey(a)) {
			b = a.getParent();
			parents.put(a, b);
		} else b = parents.get(a);
		return b;
	}
	
	public void setParentFor(LayoutableModelObject a, Container b) {
		parents.put(a, b);
	}
	
	public List<LayoutableModelObject> getChildrenFor(Container a) {
		List<LayoutableModelObject> b;
		if (!children.containsKey(a)) {
			b = new ArrayList<LayoutableModelObject>(a.getChildren());
			children.put(a, b);
		} else b = children.get(a);
		return b;
	}
	
	public void removeChildFor(Container a, LayoutableModelObject b) {
		getChildrenFor(a).remove(b);
		setParentFor(b, null);
	}
	
	public void addChildFor(Container a, LayoutableModelObject b) {
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
