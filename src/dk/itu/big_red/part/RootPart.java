package dk.itu.big_red.part;

import java.util.ArrayList;
import java.util.List;



import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editpolicies.ThingDeletePolicy;
import dk.itu.big_red.editpolicies.ThingLayoutPolicy;
import dk.itu.big_red.figure.RootFigure;
import dk.itu.big_red.model.*;
import dk.itu.big_red.model.interfaces.ILayoutable;



public class RootPart extends ThingPart {
	@Override
	public Root getModel() {
		return (Root)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new RootFigure();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ThingLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ThingDeletePolicy());
	}

	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		RootFigure figure = (RootFigure)getFigure();
		Root model = getModel();

		figure.setNumber(model.getNumber());
	}
	
	public List<ILayoutable> getModelChildren() {
		ArrayList<ILayoutable> children = new ArrayList<ILayoutable>();
		for (Thing t : getModel().getChildrenArray())
			children.add(t);
		return children;
	}
}
