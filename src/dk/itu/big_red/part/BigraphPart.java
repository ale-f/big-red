package dk.itu.big_red.part;

import java.util.ArrayList;
import java.util.List;


import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editpolicies.ThingLayoutPolicy;
import dk.itu.big_red.figure.BigraphFigure;
import dk.itu.big_red.model.*;
import dk.itu.big_red.model.interfaces.ILayoutable;

public class BigraphPart extends ThingPart {
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
	
	public List<ILayoutable> getModelChildren() {
		ArrayList<ILayoutable> children = new ArrayList<ILayoutable>();
		for (Thing t : getModel().getChildrenArray())
			children.add(t);
		for (ILayoutable t : getModel().getNHTLOs())
			children.add(t);
		return children;
	}
}
