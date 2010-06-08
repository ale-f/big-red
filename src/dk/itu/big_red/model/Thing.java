package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertySource;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.itu.big_red.model.factories.ThingFactory;
import dk.itu.big_red.propertysources.ThingPropertySource;
import dk.itu.big_red.util.DOM;

/**
 * The <code>Thing</code> provides the basic functionality shared by most
 * elements of a bigraph. <code>Thing</code>s can be moved around, copied, and
 * deleted by the user, so {@link Root}s, {@link Node}s, and {@link Site}s are
 * all <code>Thing</code>s.
 * 
 * <p><code>Thing</code>s are only useful if they've been added to a {@link
 * Bigraph} - make sure you do that before you manipulate them.
 * @author alec
 *
 */
public class Thing implements IAdaptable, IXMLisable, IPropertyChangeNotifier {
	protected PropertyChangeSupport listeners =
		new PropertyChangeSupport(this);
	
	public static final String PROPERTY_LAYOUT = "ThingLayout";
	/**
	 * The property name fired when a child is added or removed.
	 */
	public static final String PROPERTY_CHILD = "ThingChild";
	/**
	 * The property name fired when the name of a Thing changes.
	 */
	public static final String PROPERTY_RENAME = "ThingRename";
	/**
	 * The property name fired when the source edge set changes (that is, a
	 * source edge is added or removed).
	 */
	public static final String PROPERTY_SOURCE_EDGE = "ThingSourceEdge";
	/**
	 * The property name fired when the target edge set changes (that is, a
	 * target edge is added or removed).
	 */
	public static final String PROPERTY_TARGET_EDGE = "ThingTargetEdge";
	
	protected Rectangle layout;
	
	protected ArrayList<Thing> children = new ArrayList<Thing>();
	protected Thing parent = null;
	
	protected ArrayList<Edge> sourceEdges = new ArrayList<Edge>();
	protected ArrayList<Edge> targetEdges = new ArrayList<Edge>();
	
	private IPropertySource propertySource = null;
	
	public Thing() {
		this.layout = new Rectangle(10, 10, 100, 100);
	}
	
	public void setLayout(Rectangle newLayout) {
		Rectangle oldLayout = this.layout;
		this.layout = newLayout;
		listeners.firePropertyChange(PROPERTY_LAYOUT, oldLayout, newLayout);
	}
	
	public Rectangle getLayout() {
		return this.layout;
	}
	
	public boolean canContain(Thing child) {
		return false;
	}
	
	public boolean addChild(Thing child) {
		boolean added = this.children.add(child);
		if (added) {
			child.setParent(this);
			listeners.firePropertyChange(PROPERTY_CHILD, null, child);
		}
		return added;
	}
	
	public boolean removeChild(Thing child) {
		boolean removed = this.children.remove(child);
		if (removed)
			listeners.firePropertyChange(PROPERTY_CHILD, child, null);
		return removed;
	}
	
	public List<Thing> getChildrenArray() {
		return this.children;
	}
	
	private void setParent(Thing parent) {
		this.parent = parent;
	}
	
	public Thing getParent() {
		return this.parent;
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}
	
	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}

	public boolean contains(Thing child) {
		return children.contains(child);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			if (propertySource == null)
				propertySource = new ThingPropertySource(this);
			return propertySource;
		}
		return null;
	}
	
	protected Thing _overwrite(Thing orig) throws CloneNotSupportedException {
		this.setParent(orig.getParent());
		this.setLayout(new Rectangle(
			orig.getLayout().x + 10, orig.getLayout().y + 10,
			orig.getLayout().width, orig.getLayout().height));
		
		Iterator<Thing> it = orig.getChildrenArray().iterator();
		while (it.hasNext()) {
			Thing child = it.next();
			Thing childClone = (Thing)(child.clone());
			this.addChild(childClone);
			childClone.setLayout(child.getLayout());
		}
		
		return this;
	}
	
	public Thing clone() throws CloneNotSupportedException {
		return new Thing()._overwrite(this);
	}
	
	public void addEdge(Edge c) {
		if (c == null || c.getSource() == c.getTarget())
			throw new IllegalArgumentException();
		if (c.getSource() == this) {
			sourceEdges.add(c);
			listeners.firePropertyChange(PROPERTY_SOURCE_EDGE, null, c);
		} else if (c.getTarget() == this){
			targetEdges.add(c);
			listeners.firePropertyChange(PROPERTY_TARGET_EDGE, null, c);
		}
	}
	
	public void removeEdge(Edge c) {
		if (c == null)
			throw new IllegalArgumentException();
		if (c.getSource() == this) {
			sourceEdges.remove(c);
			listeners.firePropertyChange(PROPERTY_SOURCE_EDGE, c, null);
		} else if (c.getTarget() == this) {
			targetEdges.remove(c);
			listeners.firePropertyChange(PROPERTY_TARGET_EDGE, c, null);
		}
	}
	
	public List<Edge> getSourceEdges() {
		return new ArrayList<Edge>(sourceEdges);
	}
	
	public List<Edge> getTargetEdges() {
		return new ArrayList<Edge>(targetEdges);
	}
	
	public boolean edgeIncident(Edge c) {
		return (sourceEdges.contains(c) || targetEdges.contains(c));
	}
	
	public Element mintElement(org.w3c.dom.Node d) {
		org.w3c.dom.Element r =
			d.getOwnerDocument().createElement(getClass().getSimpleName().toLowerCase());
		r.setAttribute("id", Integer.toString(hashCode()));
		return r;
	}
	
	@Override
	public org.w3c.dom.Node toXML(org.w3c.dom.Node d) {
		/*
		 * Override in subclasses!
		 */
		org.w3c.dom.Element r = mintElement(d);
		r.setAttribute("x", Integer.toString(getLayout().x));
		r.setAttribute("y", Integer.toString(getLayout().y));
		r.setAttribute("width", Integer.toString(getLayout().width));
		r.setAttribute("height", Integer.toString(getLayout().height));
		for (Thing b : getChildrenArray())
			r.appendChild(b.toXML(r));
		return r;
	}

	@Override
	public void fromXML(Node d) {
		Rectangle layout = new Rectangle();
		getBigraph().idRegistry.put(DOM.getAttribute(d, "id"), this);
		layout.x = DOM.getIntAttribute(d, "x");
		layout.y = DOM.getIntAttribute(d, "y");
		layout.width = DOM.getIntAttribute(d, "width");
		layout.height = DOM.getIntAttribute(d, "height");
		setLayout(layout);
		
		NodeList l = d.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			Node t = l.item(i);
			if (t.getAttributes() != null) {
				Thing nc = ThingFactory.getNewObject(t.getNodeName());
				addChild(nc);
				nc.fromXML(t);
			}
		}
	}
	
	public ArrayList<Thing> findAllChildren(Class<? extends Thing> c) {
		ArrayList<Thing> r = new ArrayList<Thing>();
		for (Thing x : this.getChildrenArray()) {
			if (x.getClass() == c)
				r.add(x);
			r.addAll(x.findAllChildren(c));
		}
		return r;
	}
	
	public void growUpRecursively(Rectangle removed, Rectangle added) {
		Rectangle newLayout = new Rectangle(getLayout());
		newLayout.width = this.layout.width - removed.width + added.width;
		newLayout.height = this.layout.height - removed.width + added.height;
		if (getParent() != null)
			getParent().growUpRecursively(removed, added);
	}
	
	public Bigraph getBigraph() {
		Thing i = this;
		for (; i != null && !(i instanceof Bigraph); i = i.getParent());
		return (Bigraph)i;
	}
	
	public Signature getSignature() {
		return getBigraph().getSignature();
	}
	
	public void relayout() {
		int padding = 10;
		Rectangle r = new Rectangle(getLayout());
		r.width = r.height = 20;
		for (Thing i : getChildrenArray()) {
			i.relayout();
			r.width += i.getLayout().width + padding;
			r.height += i.getLayout().height;
		}
		setLayout(r);
		
		int currX = 10;
		for (Thing i : getChildrenArray()) {
			Rectangle cr = new Rectangle(i.getLayout());
			cr.x = currX;
			cr.y = (r.height / 2) - (cr.height / 2);
			//cr.y = currY;
			currX += cr.width + padding;
			i.setLayout(cr);
		}
		
		setLayout(r);
	}
}
