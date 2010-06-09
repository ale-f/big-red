package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;


import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editpolicies.ThingDeletePolicy;
import dk.itu.big_red.editpolicies.ThingEdgePolicy;
import dk.itu.big_red.editpolicies.ThingLayoutPolicy;
import dk.itu.big_red.figure.NodeFigure;
import dk.itu.big_red.model.*;
import dk.itu.big_red.model.interfaces.ILayoutable;

public class NodePart extends AbstractPart {
	@Override
	public Node getModel() {
		return (Node)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new NodeFigure();
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
		if (evt.getPropertyName().equals(Node.PROPERTY_CONTROL) ||
			evt.getPropertyName().equals(Node.PROPERTY_FILL_COLOUR) ||
	        evt.getPropertyName().equals(Node.PROPERTY_OUTLINE_COLOUR) ||
	        evt.getPropertyName().equals(Node.PROPERTY_COMMENT)) {
	    	refreshVisuals();
	    }
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		NodeFigure figure = (NodeFigure)getFigure();
		Node model = getModel();

		Rectangle layout = model.getLayout();
		
		String portDescription = null;
		if (model.getControl().getPortNames().size() != 0)
			portDescription = model.getControl().getPortNames().toString();
		
		figure.clearPortAnchors();
		for (String i : model.getControl().getPortNames())
			figure.addPortAnchor(model.getPortAnchorPosition(i));
		
		figure.setShape(model.getControl().getShape());
		figure.setLayout(layout);
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
	
	public List<ILayoutable> getModelChildren() {
		ArrayList<ILayoutable> children = new ArrayList<ILayoutable>();
		for (Thing t : getModel().getChildrenArray())
			children.add(t);
		for (Port p : getModel().getPorts())
			children.add(p);
		return children;
	}
}
