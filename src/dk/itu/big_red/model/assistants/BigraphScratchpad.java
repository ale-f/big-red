package dk.itu.big_red.model.assistants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.namespaces.INamespace;

/**
 * The BigraphScratchpad is a wrapper around various kinds of {@link
 * Layoutable} which maps objects to copies of themselves, allowing
 * the copies to be modified without affecting the original.
 * @author alec
 *
 */
public class BigraphScratchpad extends BigraphScratchpad2 {
	private Bigraph bigraph;
	public BigraphScratchpad(Bigraph bigraph) {
		this.bigraph = bigraph;
	}
	
	private HashMap<Object, INamespace<Layoutable>> namespaces =
			new HashMap<Object, INamespace<Layoutable>>();
	
	/**
	 * Clears everything in this {@link BigraphScratchpad}.
	 * @return <code>this</code>, for convenience
	 */
	@Override
	public BigraphScratchpad clear() {
		super.clear();
		namespaces.clear();
		return this;
	}
	
	public Bigraph getBigraph() {
		return bigraph;
	}
	
	private List<Layoutable> getModifiableChildren(Container a) {
		List<Layoutable> c;
		if (hasValue(a, Container.PROPERTY_CHILD)) {
			c = new ContainerProxy(a).getChildren();
		} else {
			setValue(a, Container.PROPERTY_CHILD,
					c = new ArrayList<Layoutable>(a.getChildren()));
		}
		return c;
	}
	
	public void removeChildFor(Container a, Layoutable b) {
		getModifiableChildren(a).remove(b);
		setValue(b, Layoutable.PROPERTY_PARENT, null);
		setNameFor(b, null);
	}
	
	public void addChildFor(Container a, Layoutable b, String name) {
		getModifiableChildren(a).add(b);
		setValue(b, Layoutable.PROPERTY_PARENT, a);
		setNameFor(b, name);
	}
	
	private List<Point> getModifiablePoints(Link a) {
		List<Point> p;
		if (hasValue(a, Link.PROPERTY_POINT)) {
			p = new LinkProxy(a).getPoints();
		} else {
			setValue(a, Link.PROPERTY_POINT,
					p = new ArrayList<Point>(a.getPoints()));
		}
		return p;
	}
	
	public void removePointFor(Link a, Point b) {
		getModifiablePoints(a).remove(b);
		setValue(b, Point.PROPERTY_LINK, null);
	}
	
	public void addPointFor(Link a, Point b) {
		getModifiablePoints(a).add(b);
		setValue(b, Point.PROPERTY_LINK, a);
	}
	
	public void setNameFor(Layoutable a, String b) {
		String currentName = a.getName(this);
		if (currentName != null)
			getNamespaceFor(a).remove(currentName);
		
		setValue(a, Layoutable.PROPERTY_NAME, b);
		
		getNamespaceFor(a).put(b, a);
	}
	
	public INamespace<Layoutable> getNamespaceFor(Layoutable a) {
		Object nsi = Bigraph.getNSI(a);
		INamespace<Layoutable> b;
		if (!namespaces.containsKey(nsi)) {
			b = bigraph.getNamespace(nsi).clone();
			namespaces.put(nsi, b);
		} else b = namespaces.get(nsi);
		return b;
	}
}
