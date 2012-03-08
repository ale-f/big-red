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
import dk.itu.big_red.model.assistants.BigraphScratchpad2.ContainerProxy;
import dk.itu.big_red.model.assistants.BigraphScratchpad2.LayoutableProxy;
import dk.itu.big_red.model.assistants.BigraphScratchpad2.LinkProxy;
import dk.itu.big_red.model.assistants.BigraphScratchpad2.MaybeNull;
import dk.itu.big_red.model.assistants.BigraphScratchpad2.PointProxy;
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
		((LayoutableProxy)scratch.requireProvider(a)).layout =
				new MaybeNull<Rectangle>(b);
	}
	
	public IPropertyProviderProxy getProxy() {
		return scratch;
	}
	
	protected void setParentFor(Layoutable a, Container b) {
		((LayoutableProxy)scratch.requireProvider(a)).parent =
				new MaybeNull<Container>(b);
	}
	
	private List<Layoutable> getModifiableChildren(Container a) {
		ContainerProxy cp = (ContainerProxy)scratch.requireProvider(a);
		if (cp.children == null)
			cp.children = new MaybeNull<List<Layoutable>>(
					new ArrayList<Layoutable>(a.getChildren()));
		return cp.children.get();
	}
	
	public void removeChildFor(Container a, Layoutable b) {
		getModifiableChildren(a).remove(b);
		setParentFor(b, null);
		setNameFor(b, null);
	}
	
	public void addChildFor(Container a, Layoutable b, String name) {
		getModifiableChildren(a).add(b);
		setParentFor(b, a);
		setNameFor(b, name);
	}
	
	protected void setLinkFor(Point a, Link b) {
		((PointProxy)scratch.requireProvider(a)).link = new MaybeNull<Link>(b);
	}
	
	private List<Point> getModifiablePoints(Link a) {
		LinkProxy cp = (LinkProxy)scratch.requireProvider(a);
		if (cp.points == null)
			cp.points = new MaybeNull<List<Point>>(
					new ArrayList<Point>(a.getPoints()));
		return cp.points.get();
	}
	
	public void removePointFor(Link a, Point b) {
		getModifiablePoints(a).remove(b);
		setLinkFor(b, null);
	}
	
	public void addPointFor(Link a, Point b) {
		getModifiablePoints(a).add(b);
		setLinkFor(b, a);
	}
	
	public void setNameFor(Layoutable a, String b) {
		String currentName = a.getName(getProxy());
		if (currentName != null)
			getNamespaceFor(a).remove(currentName);
		
		((LayoutableProxy)scratch.requireProvider(a)).name =
				new MaybeNull<String>(b);
		
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
