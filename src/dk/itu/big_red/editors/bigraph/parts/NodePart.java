package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.editors.bigraph.LayoutableLayoutPolicy;
import dk.itu.big_red.editors.bigraph.figures.NodeFigure;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Node;

/**
 * NodeParts represent {@link Node}s, the basic building block of bigraphs.
 * @see Node
 * @author alec
 *
 */
public class NodePart extends ContainerPart {
	@Override
	public Node getModel() {
		return (Node)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new NodeFigure();
	}
	
	@Override
	public void installEditPolicy(Object key, EditPolicy editPolicy) {
		super.installEditPolicy(key, editPolicy);
		/*
		 * Trap attempts to install a PRIMARY_DRAG_ROLE EditPolicy so that they
		 * can be tweaked to better fit the model.
		 */
		if (key == EditPolicy.PRIMARY_DRAG_ROLE)
			setResizable(getModel().getControl().isResizable());
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutableLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getPropertyName().equals(Colourable.PROPERTY_FILL_COLOUR) ||
	        evt.getPropertyName().equals(Colourable.PROPERTY_OUTLINE_COLOUR) ||
	        evt.getPropertyName().equals(ModelObject.PROPERTY_COMMENT)) {
	    	refreshVisuals();
	    	refreshChildren();
	    }
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		NodeFigure figure = (NodeFigure)getFigure();
		Node model = getModel();
		Control control = model.getControl();
		
		String toolTip;
		if (control != null) {
			setResizable(getModel().getControl().isResizable());
			
			figure.setShape(model.getControl().getShape());
			figure.setLabel(model.getControl().getLabel());
			
			toolTip = model.getControl().getLongName();
		} else {
			setResizable(true);
			
			figure.setShape(Control.Shape.SHAPE_POLYGON);
			figure.setLabel("?");
			
			toolTip = "Node with no control";
		}
		setToolTip(toolTip);
		
		PointList points = model.getFittedPolygon();
		if (points != null)
			figure.setPoints(points);
		
		figure.setBackgroundColor(model.getFillColour().getSWTColor());
		figure.setForegroundColor(model.getOutlineColour().getSWTColor());
		
		/*
		 * Any changes to the Metaclass will almost certainly change the
		 * figure's shape, so repaint it in case that's happened.
		 */
		figure.repaint();
	}
	
	@Override
	public List<Layoutable> getModelChildren() {
		ArrayList<Layoutable> children = new ArrayList<Layoutable>(getModel().getChildren());
		children.addAll(getModel().getPorts());
		return children;
	}
}
