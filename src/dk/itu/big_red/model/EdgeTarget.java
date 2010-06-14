package dk.itu.big_red.model;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.interfaces.IConnectable;

/**
 * An EdgeTarget is a small object used to keep {@link Edge}s as close to the
 * formal bigraphical model as possible. GEF/GMF requires that every connection
 * joins <i>two</i> objects; the EdgeTarget provides a target for multiple
 * connections, so multi-point bigraphical edges can be constructed quite
 * happily. 
 * @author alec
 *
 */
public class EdgeTarget implements IConnectable {
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	private Edge parent;
	private Rectangle layout = new Rectangle();
	
	public EdgeTarget(Edge parent) {
		this.parent = parent;
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

	public Edge getParent() {
		return parent;
	}
	
	private ArrayList<EdgeConnection> connections =
		new ArrayList<EdgeConnection>();
	
	@Override
	public void addConnection(EdgeConnection e) {
		connections.add(e);
	}

	@Override
	public void removeConnection(EdgeConnection e) {
		connections.remove(e);
	}
	
	@Override
	public List<EdgeConnection> getConnections() {
		return connections;
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
}
