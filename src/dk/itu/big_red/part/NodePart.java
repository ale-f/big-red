package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;


import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;

import dk.itu.big_red.editpolicies.ThingDeletePolicy;
import dk.itu.big_red.editpolicies.ThingLayoutPolicy;
import dk.itu.big_red.figure.NodeFigure;
import dk.itu.big_red.model.*;
import dk.itu.big_red.model.interfaces.ILayoutable;

public class NodePart extends ThingPart {
	@Override
	public Node getModel() {
		return (Node)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new NodeFigure();
	}
	
	/**
	 * Modifies this object's {@link EditPolicy#PRIMARY_DRAG_ROLE} edit policy
	 * to enforce the resizability constraint from the model. 
	 */
	protected void setResizability() {
		EditPolicy pol = getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
		if (pol != null && pol instanceof ResizableEditPolicy) {
			((ResizableEditPolicy)pol).setResizeDirections(
				(getModel().getControl().isResizable() ? PositionConstants.NSEW : 0));
		}
	}
	
	@Override
	public void installEditPolicy(Object key, EditPolicy editPolicy) {
		super.installEditPolicy(key, editPolicy);
		/*
		 * Trap attempts to install a PRIMARY_DRAG_ROLE EditPolicy so that they
		 * can be tweaked to better fit the model.
		 */
		if (key == EditPolicy.PRIMARY_DRAG_ROLE)
			setResizability();
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ThingLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ThingDeletePolicy());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getPropertyName().equals(Node.PROPERTY_CONTROL) ||
			evt.getPropertyName().equals(Node.PROPERTY_FILL_COLOUR) ||
	        evt.getPropertyName().equals(Node.PROPERTY_OUTLINE_COLOUR) ||
	        evt.getPropertyName().equals(Node.PROPERTY_COMMENT)) {
	    	refreshVisuals();
	    	refreshChildren();
	    }
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		NodeFigure figure = (NodeFigure)getFigure();
		Node model = getModel();
		
		setResizability();
		
		Rectangle layout = model.getLayout();
		
		String portDescription = null;
		if (model.getControl().getPortNames().size() != 0)
			portDescription = model.getControl().getPortNames().toString();
		
		figure.setShape(model.getControl().getShape());
		figure.setLabel(model.getControl().getLabel());
		figure.setToolTip(model.getControl().getLongName(), portDescription, model.getComment());
		
		figure.setFillColour(model.getFillColour());
		figure.setOutlineColour(model.getOutlineColour());
		
		/*
		 * Any changes to the Metaclass will almost certainly change the
		 * figure's shape, so repaint it in case that's happened.
		 */
		figure.repaint();
	}
	
	@Override
	public List<ILayoutable> getModelChildren() {
		ArrayList<ILayoutable> children = new ArrayList<ILayoutable>();
		for (Thing t : getModel().getChildrenArray())
			children.add(t);
		for (Port p : getModel().getPorts())
			children.add(p);
		return children;
	}
}
