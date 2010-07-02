package dk.itu.big_red.part;

import java.util.ArrayList;
import java.util.List;


import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editpolicies.ILayoutableLayoutPolicy;
import dk.itu.big_red.model.*;
import dk.itu.big_red.model.interfaces.ILayoutable;

public class BigraphPart extends ThingPart {
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		Figure f = new Figure();
		f.setLayoutManager(new XYLayout());
		return f;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ILayoutableLayoutPolicy());
	}
	
	public List<ILayoutable> getModelChildren() {
		ArrayList<ILayoutable> children = new ArrayList<ILayoutable>(getModel().getChildren());
		children.addAll(getModel().getNHTLOs());
		return children;
	}
	
	/**
	 * Does nothing.
	 */
	@Override
	protected void refreshVisuals() {
	}
}
