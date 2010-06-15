package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.interfaces.IConnectable;
import dk.itu.big_red.model.interfaces.IHierarchical;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.model.interfaces.IPropertyChangeNotifier;

/**
 * Ports are one of the two kinds of object that can be connected by an
 * {@link Edge} (the other being the {@link Name}). Ports are only ever found
 * on a {@link Node}, and inherit their name from a {@link Control}.
 * @author alec
 *
 */
public class Port implements IAdaptable, IConnectable, ILayoutable, IPropertyChangeNotifier, IHierarchical {
	private IHierarchical parent = null;
	
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	/**
	 * The position of a Port on its parent {@link Node} is governed by its
	 * <code>distance</code>, a value in the range [0,1) that specifies a
	 * fractional clockwise offset on the Node's outline. Here's a terrible
	 * attempt at a diagram:
	 * 
	 * <pre>
	 *             0
	 *         +-------+
	 *         |       |
	 *    0.75 |       | 0.25
	 *         |       |
	 *         +-------+
	 *            0.5</pre>
	 */
	private double distance = 0.0;
	
	/**
	 * The <code>name</code> of a Port identifies it, and is inherited from
	 * the parent {@link Node}'s {@link Control}.
	 */
	private String name = "0";
	
	public Port(String name, double distance) {
		setName(name);
		setDistance(distance);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public IHierarchical getParent() {
		return this.parent;
	}
	
	@Override
	public void setParent(IHierarchical parent) {
		this.parent = parent;
	}
	
	@Override
	public Rectangle getRootLayout() {
		return new Rectangle(getLayout()).translate(getParent().getRootLayout().getTopLeft());
	}
	
	/**
	 * Gets this Port's {@link Port#name name}.
	 * @see Port#name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets this Port's {@link Port#name name}.
	 * @param name the new name for this Port
	 * @see Port#name
	 */
	public void setName(String name) {
		if (name != null)
			this.name = name;
	}
	
	/**
	 * Gets this Port's {@link Port#distance distance}.
	 * @see Port#distance
	 */
	public double getDistance() {
		return distance;
	}
	
	/**
	 * Sets this Port's {@link Port#distance distance}.
	 * @param distance the new distance value, which must be in the range [0,1)
	 * @see Port#distance
	 */
	public void setDistance(double distance) {
		if (distance >= 0 && distance < 1)
			this.distance = distance;
	}

	private Rectangle layout = new Rectangle(5, 5, 10, 10);
	
	@Override
	public Rectangle getLayout() {
		return new Rectangle(layout);
	}

	@Override
	public void setLayout(Rectangle layout) {
		if (layout != null)
			this.layout.setBounds(layout);
	}

	private EdgeConnection connection = null;
	
	@Override
	public void addConnection(EdgeConnection e) {
		if (connection != null)
			connection.getParent().removePoint(this);
		connection = e;
		listeners.firePropertyChange(IConnectable.PROPERTY_SOURCE_EDGE, null, e);
	}

	@Override
	public void removeConnection(EdgeConnection e) {
		if (connection == e) {
			connection = null;
			listeners.firePropertyChange(IConnectable.PROPERTY_SOURCE_EDGE, e, null);
		}
	}
	
	@Override
	public List<EdgeConnection> getConnections() {
		ArrayList<EdgeConnection> e = new ArrayList<EdgeConnection>();
		if (connection != null)
			e.add(connection);
		return e;
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
	public Bigraph getBigraph() {
		return getParent().getBigraph();
	}

	@Override
	public void setBigraph(Bigraph bigraph) {
		getParent().setBigraph(bigraph);
	}
}
