package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.model.NamespaceManager.NameType;
import dk.itu.big_red.model.assistants.ModelPropertySource;
import dk.itu.big_red.model.interfaces.ICommentable;
import dk.itu.big_red.model.interfaces.IConnectable;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.model.interfaces.INameable;

public abstract class Link implements IAdaptable, ILayoutable, INameable, IConnectable, ICommentable {
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
	 * The {@link EdgeConnection}s that comprise this Edge on the bigraph.
	 */
	private ArrayList<EdgeConnection> connections =
		new ArrayList<EdgeConnection>();

	/**
	 * Adds the given {@link IConnectable} to this Edge's set of points, and
	 * creates a new {@link EdgeConnection} joining it to this Edge's {@link
	 * Edge}.
	 * @param point an IConnectable
	 */
	public void addPoint(IConnectable point) {
		EdgeConnection c = new EdgeConnection(this);
		c.setSource(point);
		
		if (!getBigraph().hasChild(this))
			getBigraph().addChild(this);
		
		point.addConnection(c);
		addConnection(c);
	}
	
	/**
	 * Removes the given {@link IConnectable} from this Edge's set of points
	 * and destroys its {@link EdgeConnection}.
	 * 
	 * <p>If this Edge has no points left after this operation, then it'll be
	 * removed from the Bigraph.
	 * @param point an IConnectable
	 */
	public void removePoint(IConnectable point) {
		for (EdgeConnection e : connections) {
			if (e.getSource() == point) {
				point.removeConnection(e);
				removeConnection(e);
				
				break;
			}
		}
		
		if (connections.size() == 0)
			getBigraph().removeChild(this);
	}
	
	@Override
	public void addConnection(EdgeConnection e) {
		connections.add(e);
		listeners.firePropertyChange(IConnectable.PROPERTY_TARGET_EDGE, null, e);
	}

	@Override
	public void removeConnection(EdgeConnection e) {
		connections.remove(e);
		listeners.firePropertyChange(IConnectable.PROPERTY_TARGET_EDGE, e, null);
	}
	
	@Override
	public List<EdgeConnection> getConnections() {
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
		return NamespaceManager.sensibleGetNameImplementation(this, getBigraph().getNamespaceManager());
	}
	
	@Override
	public void setName(String name) {
		NamespaceManager nm = getBigraph().getNamespaceManager();
		String oldName = nm.getName(getClass(), this);
		if (name != null) {
			if (nm.setName(getClass(), name, this))
				listeners.firePropertyChange(PROPERTY_NAME, oldName, name);
		} else {
			String newName = nm.newName(getClass(), this, NameType.NAME_ALPHABETIC);
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
	
	public abstract Link clone();
}
