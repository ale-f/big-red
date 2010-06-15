package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.interfaces.IConnectable;
import dk.itu.big_red.model.interfaces.IHierarchical;
import dk.itu.big_red.model.interfaces.IPropertyChangeNotifier;

/**
 * An EdgeTarget is a small object used to keep {@link Edge}s as close to the
 * formal bigraphical model as possible. GEF/GMF requires that every connection
 * joins <i>two</i> objects; the EdgeTarget provides a target for multiple
 * connections, so multi-point bigraphical edges can be constructed quite
 * happily. 
 * @author alec
 *
 */
public class EdgeTarget implements IConnectable, IPropertyChangeNotifier {
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	private Edge parent;
	private Rectangle layout = new Rectangle(0, 0, 10, 10);
	
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
		listeners.firePropertyChange(IConnectable.PROPERTY_TARGET_EDGE, null, e);
		int tx = 0, ty = 0;
		for (EdgeConnection f : connections) {
			tx += ((IHierarchical)f.getSource()).getRootLayout().x;
			ty += ((IHierarchical)f.getSource()).getRootLayout().y;
		}
		setLayout(new Rectangle(tx / connections.size(), ty / connections.size(), getLayout().width, getLayout().height));
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
		return getConnections().get(0).getSource().getBigraph();
	}

	@Override
	public void setBigraph(Bigraph bigraph) {
		getConnections().get(0).getSource().setBigraph(bigraph);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}
}
