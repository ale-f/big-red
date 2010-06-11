package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.EllipseAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;

import dk.itu.big_red.editpolicies.ThingDeletePolicy;
import dk.itu.big_red.editpolicies.EdgeCreationPolicy;
import dk.itu.big_red.editpolicies.ThingLayoutPolicy;
import dk.itu.big_red.figure.NameFigure;
import dk.itu.big_red.model.Name;
import dk.itu.big_red.model.Name.NameType;
import dk.itu.big_red.model.interfaces.IConnectable;

public class NamePart extends AbstractPart implements NodeEditPart {
	@Override
	public Name getModel() {
		return (Name)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new NameFigure();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ThingLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ThingDeletePolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeCreationPolicy());
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getPropertyName().equals(Name.PROPERTY_NAME) ||
			evt.getPropertyName().equals(Name.PROPERTY_TYPE)) {
	    	refreshVisuals();
	    } else if (evt.getPropertyName().equals(IConnectable.PROPERTY_SOURCE_EDGE)) {
	    	refreshSourceConnections();
	    }
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		NameFigure figure = (NameFigure)getFigure();
		Name model = getModel();
		
		figure.setName(model.getName());
		figure.setLayout(model.getLayout());
		figure.setToolTip(model.getType() == NameType.NAME_INNER ?
			"Inner name" : "Outer name");
		
		figure.setBackgroundColor(model.getType() == NameType.NAME_INNER ?
			ColorConstants.blue : ColorConstants.red);
	}
	
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new EllipseAnchor(getFigure());
    }
    
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new EllipseAnchor(getFigure());
    }
	
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new EllipseAnchor(getFigure());
    }
    
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new EllipseAnchor(getFigure());
    }
}
