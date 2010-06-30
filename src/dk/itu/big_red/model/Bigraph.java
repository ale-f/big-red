package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.itu.big_red.model.interfaces.IHierarchical;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.part.BigraphPart;
import dk.itu.big_red.util.DOM;

public class Bigraph extends Thing {
	protected Signature signature = new Signature();
	protected HashMap<String, Thing> idRegistry = new HashMap<String, Thing>();
	protected ArrayList<Edge> edges = new ArrayList<Edge>();
	
	public Thing clone() throws CloneNotSupportedException {
		return new Bigraph()._overwrite(this);
	}
	public boolean canContain(Thing child) {
		Class<? extends Thing> c = child.getClass();
		return (c == Root.class || c == Name.class);
	}
	
	public Node toXML() {
		DOMImplementation impl = DOM.getImplementation();
		
		Document doc = impl.createDocument(
				"http://pls.itu.dk/bigraphs/2010/bigraph", "bigraph", null);
		Element node = doc.getDocumentElement();
		DOM.applyAttributesToElement(node,
			"signature", "signatures/test.bigraph-signature", /* placeholder */
			"xmlns:big-red", "http://pls.itu.dk/bigraphs/2010/big-red");
		
		for (Thing b : getChildrenArray())
			node.appendChild(b.toXML(node));
		return doc;
	}
	
	public static Bigraph fromXML(org.w3c.dom.Document doc) {
		Bigraph r = new Bigraph();
		
		NodeList l = doc.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			Node t = l.item(i);
			if (t.getAttributes() != null) {
				Thing nc = (Thing)ModelFactory.getNewObject(t.getNodeName());
				r.addChild(nc);
				nc.fromXML(t);
			}
		}
		
		return r;
	}
	
	public Bigraph getBigraph() {
		return this;
	}
	
	public Signature getSignature() {
		return signature;
	}
	
	public void setParent(IHierarchical parent) {
		/* do nothing */
	}
	
	public IHierarchical getParent() {
		return null;
	}
	
	@Override
	public Rectangle getRootLayout() {
		return new Rectangle();
	}
	
	private ArrayList<ILayoutable> nhtlo = new ArrayList<ILayoutable>();
	
	/**
	 * Adds a <i>non-hierarchical top-level object</i> to this Bigraph.
	 * @param o an {@link ILayoutable}
	 * @see Bigraph#getNHTLOs()
	 */
	public void addNHTLO(ILayoutable o) {
		if (!nhtlo.contains(o)) {
			nhtlo.add(o);
			listeners.firePropertyChange(PROPERTY_CHILD, null, o);
		}
	}
	
	/**
	 * Removes a <i>non-hierarchical top-level object</i> from this Bigraph.
	 * @param o an {@link ILayoutable}
	 * @see Bigraph#getNHTLOs()
	 */
	public void removeNHTLO(ILayoutable o) {
		if (nhtlo.contains(o)) {
			nhtlo.remove(o);
			listeners.firePropertyChange(PROPERTY_CHILD, o, null);
		}
	}
	
	/**
	 * Returns the array of <i>non-hierarchical top-level objects</i> for this
	 * Bigraph.
	 * 
	 * <p>A <i>non-hierarchical top-level object</i> is an object whose {@link
	 * EditPart} must always appear as a top-level child of {@link
	 * BigraphPart}; they include {@link Port}s and {@link EdgeTarget}s. An
	 * object is a good candidate for being a NHTLO if it doesn't really make
	 * sense to think of it as being a child of a particular {@link Node}, or
	 * if it needs to be able to escape the bounding box of its parent.
	 * @return an array of {@link ILayoutable} objects
	 */
	public ArrayList<ILayoutable> getNHTLOs() {
		return nhtlo;
	}
}
