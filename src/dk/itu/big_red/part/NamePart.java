package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editpolicies.ThingDeletePolicy;
import dk.itu.big_red.editpolicies.ThingEdgePolicy;
import dk.itu.big_red.editpolicies.ThingLayoutPolicy;
import dk.itu.big_red.figure.NameFigure;
import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.Name;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Name.NameType;

public class NamePart extends AbstractPart {
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
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ThingEdgePolicy());
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getPropertyName().equals(Name.PROPERTY_NAME) ||
			evt.getPropertyName().equals(Name.PROPERTY_TYPE)) {
	    	refreshVisuals();
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
}
