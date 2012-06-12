package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Link;
import org.bigraph.model.Point;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.bigraph.EdgeCreationPolicy;
import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.editors.bigraph.LayoutableLayoutPolicy;

public abstract class LinkPart extends ConnectablePart {
	private static final boolean cmp(Object o1, Object o2) {
		return (o1 != null ? o1.equals(o2) : o1 == o2);
	}
	
	public static final class Connection {
		private Link link;
		private Point point;
		
		Connection(Link link, Point point) {
			this.link = link;
			this.point = point;
		}
		
		public Point getPoint() {
			return point;
		}
		
		public Link getLink() {
			return link;
		}
		
		@Override
		public boolean equals(Object obj_) {
			if (obj_ instanceof Connection) {
				Connection obj = (Connection)obj_;
				return (cmp(obj.link, link) && cmp(obj.point, point));
			} else return false;
		}
		
		@Override
		public int hashCode() {
			return link.hashCode() ^ point.hashCode();
		}
	}
	
	@Override
	public Link getModel() {
		return (Link)super.getModel();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == getModel()) {
			String property = evt.getPropertyName();
			if (property.equals(Link.PROPERTY_POINT)) {
		    	refreshTargetConnections();
		    	refreshVisuals();
		    } else if (property.equals(Link.PROPERTY_NAME) ||
		    		property.equals(ExtendedDataUtilities.OUTLINE)) {
		    	refreshVisuals();
		    }
		}
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		getFigure().setBackgroundColor(
				getOutline(ExtendedDataUtilities.getOutline(getModel())));
	}
	
	/**
	 * Returns a list of all the {@link Link.Connection}s for which the model
	 * object is the <i>target</i>.
	 * 
	 * <p>Note that LinkParts are always targets rather than sources, so
	 * there's no need to override the {@link
	 * AbstractPart#getModelSourceConnections()} implementation.
	 */
	@Override
	protected List<Connection> getModelTargetConnections() {
        ArrayList<Connection> l = new ArrayList<Connection>();
        for (Point p : getModel().getPoints())
        	l.add(new Connection(getModel(), p));
        return l;
    }
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutableLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeCreationPolicy());
	}
}