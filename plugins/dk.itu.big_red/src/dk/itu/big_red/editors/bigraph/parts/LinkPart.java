package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.bigraph.EdgeCreationPolicy;
import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.editors.bigraph.LayoutableLayoutPolicy;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;

public abstract class LinkPart extends ConnectablePart {
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
				getOutline(getModel().getOutlineColour()));
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
	protected List<Link.Connection> getModelTargetConnections() {
        ArrayList<Link.Connection> l = new ArrayList<Link.Connection>();
        for (Point p : getModel().getPoints())
        	l.add(getModel().getConnectionFor(p));
        return l;
    }
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutableLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeCreationPolicy());
	}
}