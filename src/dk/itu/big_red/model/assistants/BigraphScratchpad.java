package dk.itu.big_red.model.assistants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.utilities.Lists;
import dk.itu.big_red.utilities.geometry.ReadonlyRectangle;
import dk.itu.big_red.utilities.geometry.Rectangle;

/**
 * The BigraphScratchpad is a wrapper around various kinds of {@link
 * Layoutable} which maps objects to copies of themselves, allowing
 * the copies to be modified without affecting the original.
 * @author alec
 *
 */
public class BigraphScratchpad {
	private Bigraph bigraph = null;
	
	public BigraphScratchpad(Bigraph bigraph) {
		this.bigraph = bigraph;
	}
	
	private HashMap<Layoutable, ReadonlyRectangle> layouts =
			new HashMap<Layoutable, ReadonlyRectangle>();
	
	private HashMap<Layoutable, Container> parents =
			new HashMap<Layoutable, Container>();
	
	private HashMap<Container, List<Layoutable>> children =
			new HashMap<Container, List<Layoutable>>();
	
	private HashMap<Link, List<Point>> points =
			new HashMap<Link, List<Point>>();
	
	private HashMap<Point, Link> links = new HashMap<Point, Link>();
	
	private HashMap<Object, Map<String, Layoutable>> namespaces =
			new HashMap<Object, Map<String, Layoutable>>();

	private HashMap<Layoutable, String> names =
			new HashMap<Layoutable, String>();
	
	/**
	 * Clears everything in this {@link BigraphScratchpad}.
	 * @return <code>this</code>, for convenience
	 */
	public BigraphScratchpad clear() {
		links.clear();
		points.clear();
		children.clear();
		layouts.clear();
		parents.clear();
		namespaces.clear();
		names.clear();
		
		return this;
	}
	
	public ReadonlyRectangle getLayoutFor(Layoutable a) {
		if (!layouts.containsKey(a)) {
			return a.getLayout();
		} else return layouts.get(a);
	}
	
	public void setLayoutFor(Layoutable a, Rectangle b) {
		layouts.put(a, b);
	}
	
	public Container getParentFor(Layoutable a) {
		if (!parents.containsKey(a)) {
			return a.getParent();
		} else return parents.get(a);
	}
	
	public Bigraph getBigraphFor(Layoutable l) {
		if (l instanceof Bigraph)
			return (Bigraph)l;
		Container c = getParentFor(l);
		while (c != null && !(c instanceof Bigraph))
			c = getParentFor(c);
		return (Bigraph)c;
	}
	
	protected void setParentFor(Layoutable a, Container b) {
		parents.put(a, b);
	}
	
	public List<Layoutable> getChildrenFor(Container a) {
		List<Layoutable> b;
		if (!children.containsKey(a)) {
			b = Lists.copy(a.getChildren());
			children.put(a, b);
		} else b = children.get(a);
		return b;
	}
	
	public void removeChildFor(Container a, Layoutable b) {
		getChildrenFor(a).remove(b);
		setParentFor(b, null);
		getNamespaceFor(b).remove(b.getName());
	}
	
	public void addChildFor(Container a, Layoutable b, String name) {
		getChildrenFor(a).add(b);
		setParentFor(b, a);
		setNameFor(b, name);
	}
	
	public Link getLinkFor(Point a) {
		if (!links.containsKey(a)) {
			return a.getLink();
		} else return links.get(a);
	}
	
	protected void setLinkFor(Point a, Link b) {
		links.put(a, b);
	}
	
	public List<Point> getPointsFor(Link a) {
		List<Point> b;
		if (!points.containsKey(a)) {
			b = Lists.copy(a.getPoints());
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
	
	public Map<String, Layoutable> getNamespaceFor(Layoutable a) {
		Object nsi = Bigraph.getNSI(a);
		Map<String, Layoutable> b;
		if (!namespaces.containsKey(nsi)) {
			b = Bigraph.newNamespace(bigraph.getNamespace(nsi));
			namespaces.put(nsi, b);
		} else b = namespaces.get(nsi);
		return b;
	}
	
	public String getNameFor(Layoutable a) {
		if (!names.containsKey(a)) {
			return a.getName();
		} else return names.get(a);
	}
	
	public void setNameFor(Layoutable a, String b) {
		String oldName = getNameFor(a);
		
		names.remove(a);
		names.put(a, b);
		
		getNamespaceFor(a).remove(oldName);
		getNamespaceFor(a).put(b, a);
	}
}
