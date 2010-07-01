package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertySource;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.itu.big_red.model.interfaces.ICommentable;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.model.interfaces.IPropertyChangeNotifier;
import dk.itu.big_red.model.interfaces.IXMLisable;
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
public class Thing implements IAdaptable, IXMLisable, ILayoutable, IPropertyChangeNotifier, ICommentable {
	protected PropertyChangeSupport listeners =
		new PropertyChangeSupport(this);
	
	/**
	 * The property name fired when a child is added or removed.
	 */
	public static final String PROPERTY_CHILD = "ThingChild";
	/**
	 * The property name fired when the name of a Thing changes.
	 */
	public static final String PROPERTY_RENAME = "ThingRename";
	
	protected Rectangle layout;
	
	protected ArrayList<Thing> children = new ArrayList<Thing>();
	protected ILayoutable parent = null;
	
	private IPropertySource propertySource = null;
	
	public Thing() {
		this.layout = new Rectangle(10, 10, 100, 100);
	}

	@Override
	public Rectangle getLayout() {
		return new Rectangle(this.layout);
	}
	
	@Override
	public void setLayout(Rectangle newLayout) {
		Rectangle oldLayout = this.layout;
		this.layout = new Rectangle(newLayout);
		listeners.firePropertyChange(PROPERTY_LAYOUT, oldLayout, this.layout);
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
	
	public List<Thing> getChildren() {
		return this.children;
	}
	
	@Override
	public ILayoutable getParent() {
		return this.parent;
	}
	
	@Override
	public void setParent(ILayoutable parent) {
		this.parent = parent;
	}
	
	@Override
	public Rectangle getRootLayout() {
		return new Rectangle(getLayout()).translate(getParent().getRootLayout().getTopLeft());
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
				propertySource = new ModelPropertySource(this);
			return propertySource;
		}
		return null;
	}
	
	protected Thing _overwrite(Thing orig) throws CloneNotSupportedException {
		this.setParent(orig.getParent());
		this.setLayout(new Rectangle(
			orig.getLayout().x + 10, orig.getLayout().y + 10,
			orig.getLayout().width, orig.getLayout().height));
		
		Iterator<Thing> it = orig.getChildren().iterator();
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
	
	public Element mintElement(org.w3c.dom.Node d) {
		org.w3c.dom.Element r =
			d.getOwnerDocument().createElement(getClass().getSimpleName().toLowerCase());
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
		for (Thing b : getChildren())
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
				Thing nc = (Thing)ModelFactory.getNewObject(t.getNodeName());
				addChild(nc);
				nc.fromXML(t);
			}
		}
	}
	
	public Signature getSignature() {
		return getBigraph().getSignature();
	}
	
	public void relayout() {
		int padding = 10;
		Rectangle r = new Rectangle(getLayout());
		r.width = r.height = 20;
		for (Thing i : getChildren()) {
			i.relayout();
			r.width += i.getLayout().width + padding;
			r.height += i.getLayout().height;
		}
		setLayout(r);
		
		int currX = 10;
		for (Thing i : getChildren()) {
			Rectangle cr = new Rectangle(i.getLayout());
			cr.x = currX;
			cr.y = (r.height / 2) - (cr.height / 2);
			//cr.y = currY;
			currX += cr.width + padding;
			i.setLayout(cr);
		}
		
		setLayout(r);
	}

	private String comment = "";
	
	@Override
	public String getComment() {
		return this.comment;
	}

	@Override
	public void setComment(String comment) {
		String oldComment = this.comment;
		this.comment = comment;
		listeners.firePropertyChange(PROPERTY_COMMENT, oldComment, comment);
	}
	
	@Override
	public Bigraph getBigraph() {
		return getParent().getBigraph();
	}

	@Override
	public void setBigraph(Bigraph bigraph) {
		// TODO Auto-generated method stub
		
	}
}
