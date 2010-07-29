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

/**
  * An Edge is a connection which connects any number of {@link Port}s and
  * {@link InnerName}s. (An Edge which "connects" only one point is perfectly
  * legitimate.)
  * 
  * <p>Note that Edges represent the <i>bigraphical</i> concept of an edge
  * rather than a GEF/GMF {@link Connection}, and so they lack any concept of a
  * "source" or "target"; the Edge is always the target for a connection, and
  * {@link Point}s are always sources.
  * @author alec
  *
  */
public class Edge implements IAdaptable, IConnectable, ICommentable, INameable {
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	/**
	 * The {@link EdgeConnection}s that comprise this Edge on the bigraph.
	 */
	private ArrayList<EdgeConnection> connections =
		new ArrayList<EdgeConnection>();
	
	private Rectangle layout = new Rectangle(0, 0, 10, 10);
	
	protected String comment = null;
	
	public Edge() {
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
	
	/**
	 * Moves this EdgeTarget to the average position of all the
	 * {@link IConnectable}s connected to it.
	 */
	public void averagePosition() {
		int tx = 0, ty = 0;
		for (EdgeConnection f : connections) {
			tx += f.getSource().getRootLayout().x;
			ty += f.getSource().getRootLayout().y;
		}
		setLayout(new Rectangle(tx / connections.size(), ty / connections.size(), getLayout().width, getLayout().height));
	}
	
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
	public Bigraph getBigraph() {
		return getParent().getBigraph();
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
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
	public Edge clone() {
		return new Edge();
	}
	
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
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == IPropertySource.class) {
			return new ModelPropertySource(this);
		} else return null;
	}
	
	public String getName() {
		return getBigraph().getNamespaceManager().getName(getClass(), this);
	}
	
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
}
