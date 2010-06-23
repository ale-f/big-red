package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.Connection;
import org.eclipse.ui.views.properties.IPropertySource;
import org.w3c.dom.Node;

import dk.itu.big_red.model.interfaces.ICommentable;
import dk.itu.big_red.model.interfaces.IConnectable;
import dk.itu.big_red.model.interfaces.IPropertyChangeNotifier;
import dk.itu.big_red.model.interfaces.IXMLisable;

/**
 * An Edge is a connection which connects any number of {@link Port}s and
 * {@link Name}s. (An Edge which "connects" only one point is perfectly
 * legitimate.)
 * 
 * <p>Note that Edges represent the <i>bigraphical</i> concept of an edge
 * rather than a GEF/GMF {@link Connection}, and so they lack any concept of a
 * "source" or "target"; Ports and Names are always source nodes as far as the
 * underlying framework is concerned, and the target is always an {@link
 * EdgeTarget}.
 * @author alec
 *
 */
public class Edge implements IAdaptable, IPropertyChangeNotifier, IXMLisable, ICommentable {
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	public static final String PROPERTY_COMMENT = "EdgeComment";
	
	/**
	 * The points on the bigraph connected by this Edge. (This should generally
	 * contain at least one entry.)
	 */
	private ArrayList<IConnectable> points = new ArrayList<IConnectable>();
	
	/**
	 * The {@link EdgeConnection}s that comprise this Edge on the bigraph.
	 */
	private ArrayList<EdgeConnection> connections = new ArrayList<EdgeConnection>();
	
	/**
	 * The {@link EdgeTarget} which all the {@link EdgeConnection}s use as
	 * their target.
	 */
	private EdgeTarget target = new EdgeTarget(this);
	
	private IPropertySource propertySource = null;

	private String comment = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			if (propertySource == null) {
				propertySource = new ModelPropertySource(this);
			}
			return propertySource;
		}
		return null;
	}

	/**
	 * Adds the given {@link IConnectable} to this Edge's set of points, and
	 * creates a new {@link EdgeConnection} joining it to this Edge's {@link
	 * EdgeTarget}.
	 * @param point an IConnectable
	 */
	public void addPoint(IConnectable point) {
		EdgeConnection c = new EdgeConnection(this);
		c.setSource(point);
		points.add(point);
		connections.add(c);
		point.addConnection(c);
		
		getEdgeTarget().addConnection(c);
		point.getBigraph().addNHTLO(getEdgeTarget());
	}
	
	/**
	 * Removes the given {@link IConnectable} from this Edge's set of points
	 * and destroys its {@link EdgeConnection}.
	 * @param point an IConnectable
	 */
	public void removePoint(IConnectable point) {
		if (points.contains(point)) {
			for (EdgeConnection e : point.getConnections()) {
				int index = connections.indexOf(e);
				if (index != -1) {
					points.remove(point);
					connections.remove(index);
					getEdgeTarget().removeConnection(e);
					point.removeConnection(e);
					break;
				}
			}
		}
	}
	
	public EdgeTarget getEdgeTarget() {
		return target;
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}
	
	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
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
	public void fromXML(Node d) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Node toXML(Node d) {
		// TODO Auto-generated method stub
		return null;
	}
}
