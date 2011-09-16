package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.edit_policies.EdgeCreationPolicy;
import dk.itu.big_red.editors.edit_policies.ILayoutableDeletePolicy;
import dk.itu.big_red.editors.edit_policies.ILayoutableLayoutPolicy;
import dk.itu.big_red.figure.OuterNameFigure;
import dk.itu.big_red.figure.assistants.FixedPointAnchor.Orientation;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.interfaces.internal.ICommentable;

public class OuterNamePart extends LinkPart {
	@Override
	protected IFigure createFigure() {
		return new OuterNameFigure();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ILayoutableLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ILayoutableDeletePolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeCreationPolicy());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getPropertyName().equals(InnerName.PROPERTY_NAME) ||
			evt.getPropertyName().equals(ICommentable.PROPERTY_COMMENT)) {
	    	refreshVisuals();
	    } else if (evt.getPropertyName().equals(Link.PROPERTY_TARGET_EDGE)) {
	    	refreshTargetConnections();
	    }
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		((OuterNameFigure)getFigure()).setName(getModel().getName());
	}
	
	@Override
	public Orientation getAnchorOrientation() {
		return Orientation.SOUTH;
	}
}
