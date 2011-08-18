package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.model.NamespaceManager.NameType;
import dk.itu.big_red.model.assistants.ModelPropertySource;
import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.model.interfaces.IPoint;
import dk.itu.big_red.model.interfaces.internal.ICommentable;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.model.interfaces.internal.INameable;
import dk.itu.big_red.model.interfaces.internal.IOutlineColourable;

/**
 * A Link is the superclass of {@link Edge}s and {@link OuterName}s &mdash;
 * model objects which have multiple {@link LinkConnection}s to {@link Point}s.
 * @author alec
 *
 */
public abstract class Link implements IAdaptable, ILayoutable, INameable, ICommentable, IOutlineColourable, ILink {
	/**
	 * The property name fired when the target edge set changes (that is, an
	 * edge for which this object is the target is added or removed).
	 */
	public static final String PROPERTY_TARGET_EDGE = "LinkTargetEdge";
	
	protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}
	
	/**
	 * The {@link LinkConnection}s that comprise this Link on the bigraph.
	 */
	private ArrayList<LinkConnection> connections =
		new ArrayList<LinkConnection>();

	/**
	 * Adds the given {@link Point} to this Link's set of points, and creates a
	 * new {@link LinkConnection} joining it to this Link's {@link Link}.
	 * @param point a Point
	 */
	public void addPoint(Point point) {
		LinkConnection c = new LinkConnection(this);
		c.setPoint(point);
		
		point.addConnection(c);
		addConnection(c);
	}
	
	/**
	 * Removes the given {@link Point} from this Link's set of points and
	 * destroys its {@link LinkConnection}.
	 * 
	 * @param point a Point
	 */
	public void removePoint(Point point) {
		for (LinkConnection e : connections) {
			if (e.getPoint() == point) {
				point.removeConnection(e);
				removeConnection(e);
				
				break;
			}
		}
	}
	
	public void addConnection(LinkConnection e) {
		connections.add(e);
		listeners.firePropertyChange(Link.PROPERTY_TARGET_EDGE, null, e);
	}

	public void removeConnection(LinkConnection e) {
		connections.remove(e);
		listeners.firePropertyChange(Link.PROPERTY_TARGET_EDGE, e, null);
	}
	
	public List<LinkConnection> getConnections() {
		return connections;
	}
	
	@Override
	public Rectangle getRootLayout() {
		return new Rectangle(getLayout()).translate(getParent().getRootLayout().getTopLeft());
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == IPropertySource.class) {
			return new ModelPropertySource(this);
		} else return null;
	}
	
	/**
	 * Returns an empty list.
	 */
	@Override
	public List<ILayoutable> getChildren() {
		return new ArrayList<ILayoutable>();
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void addChild(ILayoutable c) {
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void removeChild(ILayoutable c) {
	}

	/**
	 * Returns false.
	 */
	@Override
	public boolean hasChild(ILayoutable c) {
		return false;
	}
	
	/**
	 * Returns false.
	 */
	@Override
	public boolean canContain(ILayoutable c) {
		return false;
	}
	
	@Override
	public String getName() {
		return getBigraph().getNamespaceManager().getRequiredName(Link.class, this);
	}
	
	@Override
	public void setName(String name) {
		NamespaceManager nm = getBigraph().getNamespaceManager();
		String oldName = nm.getName(Link.class, this);
		if (name != null) {
			if (nm.setName(Link.class, name, this))
				listeners.firePropertyChange(PROPERTY_NAME, oldName, name);
		} else {
			String newName = nm.newName(Link.class, this, NameType.NAME_ALPHABETIC);
			if (!newName.equals(oldName))
				listeners.firePropertyChange(PROPERTY_NAME, oldName, newName);
		}
	}
	
	private ILayoutable parent = null;
	
	@Override
	public ILayoutable getParent() {
		return this.parent;
	}

	@Override
	public void setParent(ILayoutable p) {
		if (p != null) {
			ILayoutable oldParent = this.parent;
			this.parent = p;
			listeners.firePropertyChange(PROPERTY_PARENT, oldParent, parent);
		}
	}
	
	@Override
	public Bigraph getBigraph() {
		return getParent().getBigraph();
	}
	
	private String comment = null;
	
	@Override
	public String getComment() {
		return this.comment;
	}
	
	@Override
	public void setComment(String comment) {
		String oldComment = getComment();
		this.comment = comment;
		listeners.firePropertyChange(PROPERTY_COMMENT, oldComment, comment);
	}
	
	private Rectangle layout = new Rectangle(0, 0, 10, 10);

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
	
	private RGB outlineColour = new RGB(0, 127, 0);
	
	@Override
	public void setOutlineColour(RGB outlineColour) {
		RGB oldColour = getOutlineColour();
		this.outlineColour = outlineColour;
		listeners.firePropertyChange(PROPERTY_OUTLINE_COLOUR, oldColour, outlineColour);
	}

	@Override
	public RGB getOutlineColour() {
		return outlineColour;
	}
	
	public abstract Link clone();
	
	@Override
	public Iterable<IPoint> getIPoints() {
		ArrayList<IPoint> points = new ArrayList<IPoint>();
		for (LinkConnection c : connections)
			points.add(c.getPoint());
		return points;
	}
}
