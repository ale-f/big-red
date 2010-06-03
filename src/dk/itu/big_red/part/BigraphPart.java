package dk.itu.big_red.part;

import java.util.List;


import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editpolicies.ThingLayoutPolicy;
import dk.itu.big_red.figure.BigraphFigure;
import dk.itu.big_red.model.*;

public class BigraphPart extends AbstractPart {
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new BigraphFigure();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ThingLayoutPolicy());
	}

	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		BigraphFigure figure = (BigraphFigure)getFigure();
		Bigraph model = getModel();

		figure.setLayout(model.getLayout());
	}
	
	public List<Thing> getModelChildren() {
		return ((Bigraph)getModel()).getChildrenArray();
	}
}
