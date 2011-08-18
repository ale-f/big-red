package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;

import dk.itu.big_red.editpolicies.EdgeCreationPolicy;
import dk.itu.big_red.editpolicies.ILayoutableDeletePolicy;
import dk.itu.big_red.editpolicies.ILayoutableLayoutPolicy;
import dk.itu.big_red.figure.OuterNameFigure;
import dk.itu.big_red.figure.adornments.FixedPointAnchor;
import dk.itu.big_red.figure.adornments.FixedPointAnchor.Orientation;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.LinkConnection;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.interfaces.internal.ICommentable;

public class OuterNamePart extends AbstractPart implements NodeEditPart {
	@Override
	public OuterName getModel() {
		return (OuterName)super.getModel();
	}
	
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
		
		OuterNameFigure figure = (OuterNameFigure)getFigure();
		OuterName model = getModel();
		
		figure.setName(model.getName());
		figure.setConstraint(model.getLayout());
		
		String toolTip = "Outer name " + model.getName();
		if (model.getComment() != null)
			toolTip += "\n\n" + model.getComment();
		figure.setToolTip(toolTip);
		
		figure.setBackgroundColor(model.getOutlineColour());
	}
	
	@Override
	protected List<LinkConnection> getModelTargetConnections() {
        return getModel().getConnections();
    }
	
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new FixedPointAnchor(getFigure(), Orientation.SOUTH);
    }
    
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new FixedPointAnchor(getFigure(), Orientation.SOUTH);
    }
	
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new FixedPointAnchor(getFigure(), Orientation.SOUTH);
    }
    
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new FixedPointAnchor(getFigure(), Orientation.SOUTH);
    }
}
