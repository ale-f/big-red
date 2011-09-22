package dk.itu.big_red.model.assistants;

import java.util.HashMap;
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
	
	private HashMap<Point, Link> links = new HashMap<Point, Link>();
	
	public void clear() {
		layouts.clear();
		parents.clear();
		links.clear();
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
}
