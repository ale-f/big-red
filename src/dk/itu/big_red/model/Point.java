package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.model.assistants.ModelPropertySource;
import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.model.interfaces.IPoint;
import dk.itu.big_red.model.interfaces.internal.ICommentable;
import dk.itu.big_red.model.interfaces.internal.IConnectable;
import dk.itu.big_red.model.interfaces.internal.IFillColourable;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.model.interfaces.internal.INameable;

/**
 * Points are objects which can be connected to <em>at most one</em> {@link
 * Link} - {@link Port}s and {@link InnerName}s.
 * @author alec
 *
 */
public abstract class Point implements IConnectable, IAdaptable, ICommentable, IFillColourable, IPoint {
	@Override
	public ILink getILink() {
		return (getConnections().size() > 0 ?
				getConnections().get(0).getTarget() : null);
	}

	private String comment = null;
	
	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public void setComment(String comment) {
		String oldComment = this.comment;
		this.comment = comment;
		listeners.firePropertyChange(PROPERTY_COMMENT, oldComment, comment);
	}

	protected Rectangle layout = new Rectangle(5, 5, 10, 10);
	
	@Override
	public Rectangle getLayout() {
		return new Rectangle(layout);
	}

	@Override
	public void setLayout(Rectangle layout) {
		if (layout != null) {
			Rectangle oldLayout = new Rectangle(this.layout);
			this.layout.setBounds(layout);
			listeners.firePropertyChange(ILayoutable.PROPERTY_LAYOUT, oldLayout, layout);
		}
	}

	protected LinkConnection connection = null;
	
	@Override
	public void addConnection(LinkConnection e) {
		if (connection != null)
			connection.getTarget().removePoint(this);
		LinkConnection oldConnection = connection;
		connection = e;
		listeners.firePropertyChange(IConnectable.PROPERTY_SOURCE_EDGE, oldConnection, e);
	}

	@Override
	public void removeConnection(LinkConnection e) {
		if (connection == e) {
			connection = null;
			listeners.firePropertyChange(IConnectable.PROPERTY_SOURCE_EDGE, e, null);
		}
	}
	
	@Override
	public List<LinkConnection> getConnections() {
		ArrayList<LinkConnection> e = new ArrayList<LinkConnection>();
		if (connection != null)
			e.add(connection);
		return e;
	}

	protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}

	protected String name = "?";
	
	/**
	 * Gets the name of this Point.
	 * @return the current name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of this Point.
	 * @param name the new name
	 */
	public void setName(String name) {
		if (name != null) {
			String oldName = this.name;
			this.name = name;
			listeners.firePropertyChange(INameable.PROPERTY_NAME, oldName, name);
		}
	}

	@Override
	public Rectangle getRootLayout() {
		return new Rectangle(getLayout()).translate(getParent().getRootLayout().getTopLeft());
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
	public Point clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return new ModelPropertySource(this);
		} else return null;
	}
	
	private RGB fillColour = new RGB(255, 0, 0);
	
	@Override
	public void setFillColour(RGB fillColour) {
		RGB oldColour = getFillColour();
		this.fillColour = fillColour;
		listeners.firePropertyChange(PROPERTY_FILL_COLOUR, oldColour, fillColour);
	}

	@Override
	public RGB getFillColour() {
		return fillColour;
	}
}
