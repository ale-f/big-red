package dk.itu.big_red.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;


import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.itu.big_red.commands.EdgeCreateCommand;
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
		
		Document doc = impl.createDocument(null, "brs", null);
		Node node = doc.getDocumentElement();
		
		node.appendChild(doc.createComment(
			"This is a Big Red XML bigraph definition. " +
			"DO NOT EDIT IT UNLESS YOU KNOW WHAT YOU'RE DOING - " +
			"whitespace is significant and no attributes are optional!"));
		
		Node signatureE = doc.createElement("signature");
		for (Control k : getSignature().getControls())
			signatureE.appendChild(k.toXML(signatureE));
		node.appendChild(signatureE);
		
		Node bigraphE = doc.createElement("bigraph");
		for (Thing b : getChildrenArray())
			bigraphE.appendChild(b.toXML(bigraphE));
		node.appendChild(bigraphE);
		return doc;
	}
	
	public static Bigraph fromXML(org.w3c.dom.Document doc) {
		Bigraph r = new Bigraph();
		
		ArrayList<Node> mcs =
			DOM.getNamedChildNodes(doc.getElementsByTagName("signature").item(0), "control");
		for (Node t : mcs) {
			String name = DOM.getAttribute(t, "name");
			Control.Shape shape =
				Control.Shape.valueOf(DOM.getAttribute(t, "shape"));
			String label = DOM.getAttribute(t, "label");
			
			Point defaultSize = new Point(
				DOM.getIntAttribute(t, "width"),
				DOM.getIntAttribute(t, "height"));
			
			boolean resizable =
				DOM.getAttribute(t, "resizable").equals("true");
			
			Control mc =
				r.getSignature().
				addControl(name, label, shape, defaultSize, resizable);
			
			ArrayList<Node> ports = DOM.getNamedChildNodes(t, "port");
			for (Node u : ports) {
				String port = DOM.getAttribute(u, "key");
				int offset = DOM.getIntAttribute(u, "offset");
					
				mc.addPort(port, offset);
			}
		}
		
		NodeList l = doc.getElementsByTagName("bigraph").item(0).getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			Node t = l.item(i);
			if (t.getAttributes() != null) {
				Thing nc = (Thing)ModelFactory.getNewObject(t.getNodeName());
				r.addChild(nc);
				nc.fromXML(t);
			}
		}
		
		/* EDGE XML */
		return r;
	}
	
	public static Bigraph fromXML(String filename) {
		Document doc = DOM.parse(filename);
		return (doc != null ? Bigraph.fromXML(doc) : null);
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
