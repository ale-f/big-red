package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dk.itu.big_red.model.interfaces.IConnectable;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.model.interfaces.IPropertyChangeNotifier;
import dk.itu.big_red.model.interfaces.IXMLisable;
import dk.itu.big_red.util.DOM;

/**
 * Points are objects which can be connected to <em>at most one</em> {@link
 * Link} - {@link Port}s and {@link InnerName}s.
 * @author alec
 *
 */
public class Point implements IConnectable, IPropertyChangeNotifier, IXMLisable {
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

	@Override
	public Bigraph getBigraph() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBigraph(Bigraph bigraph) {
		// TODO Auto-generated method stub
		
	}

	protected EdgeConnection connection = null;
	
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

	protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}

	@Override
	public Node toXML(Node d) {
		if (connection != null) {
			Document doc = d.getOwnerDocument();
			Element r = doc.createElement(getClass().getSimpleName().toLowerCase());
			DOM.applyAttributesToElement(r,
					"name", Integer.toString(hashCode(), 16),
					"link", Integer.toString(connection.getParent().hashCode(), 16));
			return r;
		} else return null;
	}

	@Override
	public void fromXML(Node d) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Rectangle getRootLayout() {
		// TODO Auto-generated method stub
		return null;
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
}
