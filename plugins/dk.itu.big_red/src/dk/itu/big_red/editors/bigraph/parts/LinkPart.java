package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Link;
import org.bigraph.model.Point;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.bigraph.EdgeCreationPolicy;
import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.editors.bigraph.LayoutableLayoutPolicy;
import dk.itu.big_red.model.ColourUtilities;

import static org.bigraph.model.ModelObject.safeEquals;

public abstract class LinkPart extends ConnectablePart {
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
				return (safeEquals(obj.link, link) &&
						safeEquals(obj.point, point));
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
			if (Link.PROPERTY_POINT.equals(property)) {
		    	refreshTargetConnections();
		    	refreshVisuals();
		    } else if (Link.PROPERTY_NAME.equals(property) ||
		    		ColourUtilities.OUTLINE.equals(property)) {
		    	refreshVisuals();
		    }
		}
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		getFigure().setBackgroundColor(
				getOutline(ColourUtilities.getOutline(getModel())));
	}
	
	/**
	 * Returns a list of all the {@link Link.Connection}s for which the model
	 * object is the <i>target</i>.
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
		super.createEditPolicies();
		
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutableLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeCreationPolicy());
	}
}