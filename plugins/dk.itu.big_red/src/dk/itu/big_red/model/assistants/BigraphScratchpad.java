package dk.itu.big_red.model.assistants;

import java.util.HashMap;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.names.INamespace;

/**
 * The BigraphScratchpad is a wrapper around various kinds of {@link
 * Layoutable} which maps objects to copies of themselves, allowing
 * the copies to be modified without affecting the original.
 * @author alec
 *
 */
public class BigraphScratchpad extends PropertyScratchpad {
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
	
	public void removeChildFor(Container a, Layoutable b) {
		this.<Layoutable>getModifiableList(a, Container.PROPERTY_CHILD).remove(b);
		setProperty(b, Layoutable.PROPERTY_PARENT, null);
		setNameFor(b, null);
	}
	
	public void addChildFor(Container a, Layoutable b, String name) {
		this.<Layoutable>getModifiableList(a, Container.PROPERTY_CHILD).add(b);
		setProperty(b, Layoutable.PROPERTY_PARENT, a);
		setNameFor(b, name);
	}
	
	public void removePointFor(Link a, Point b) {
		this.<Point>getModifiableList(a, Link.PROPERTY_POINT).remove(b);
		setProperty(b, Point.PROPERTY_LINK, null);
	}
	
	public void addPointFor(Link a, Point b) {
		this.<Point>getModifiableList(a, Link.PROPERTY_POINT).add(b);
		setProperty(b, Point.PROPERTY_LINK, a);
	}
	
	public void setNameFor(Layoutable a, String b) {
		String currentName = a.getName(this);
		if (currentName != null)
			getNamespaceFor(a).remove(currentName);
		
		setProperty(a, Layoutable.PROPERTY_NAME, b);
		
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
