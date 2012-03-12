package dk.itu.big_red.model.assistants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.assistants.IPropertyProviders.IPropertyProviderProxy;
import dk.itu.big_red.model.namespaces.INamespace;

/**
 * The BigraphScratchpad is a wrapper around various kinds of {@link
 * Layoutable} which maps objects to copies of themselves, allowing
 * the copies to be modified without affecting the original.
 * @author alec
 *
 */
public class BigraphScratchpad {
	private BigraphScratchpad2 scratch = new BigraphScratchpad2();
	
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
	public BigraphScratchpad clear() {
		scratch.clear();
		namespaces.clear();
		return this;
	}
	
	public Bigraph getBigraph() {
		return bigraph;
	}
	
	public void setLayoutFor(Layoutable a, Rectangle b) {
		scratch.setValue(a, Layoutable.PROPERTY_LAYOUT, b);
	}
	
	public IPropertyProviderProxy getProxy() {
		return scratch;
	}
	
	private List<Layoutable> getModifiableChildren(Container a) {
		List<Layoutable> c;
		if (scratch.hasValue(a, Container.PROPERTY_CHILD)) {
			c = scratch.new ContainerProxy(a).getChildren();
		} else {
			scratch.setValue(a, Container.PROPERTY_CHILD,
					c = new ArrayList<Layoutable>(a.getChildren()));
		}
		return c;
	}
	
	public void removeChildFor(Container a, Layoutable b) {
		getModifiableChildren(a).remove(b);
		scratch.setValue(b, Layoutable.PROPERTY_PARENT, null);
		setNameFor(b, null);
	}
	
	public void addChildFor(Container a, Layoutable b, String name) {
		getModifiableChildren(a).add(b);
		scratch.setValue(b, Layoutable.PROPERTY_PARENT, a);
		setNameFor(b, name);
	}
	
	private List<Point> getModifiablePoints(Link a) {
		List<Point> p;
		if (scratch.hasValue(a, Link.PROPERTY_POINT)) {
			p = scratch.new LinkProxy(a).getPoints();
		} else {
			scratch.setValue(a, Link.PROPERTY_POINT,
					p = new ArrayList<Point>(a.getPoints()));
		}
		return p;
	}
	
	public void removePointFor(Link a, Point b) {
		getModifiablePoints(a).remove(b);
		scratch.setValue(b, Point.PROPERTY_LINK, null);
	}
	
	public void addPointFor(Link a, Point b) {
		getModifiablePoints(a).add(b);
		scratch.setValue(b, Point.PROPERTY_LINK, a);
	}
	
	public void setNameFor(Layoutable a, String b) {
		String currentName = a.getName(getProxy());
		if (currentName != null)
			getNamespaceFor(a).remove(currentName);
		
		scratch.setValue(a, Layoutable.PROPERTY_NAME, b);
		
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
