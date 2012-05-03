package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.editors.bigraph.LayoutableLayoutPolicy;
import dk.itu.big_red.editors.bigraph.figures.NodeFigure;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Layoutable;
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
		String name = evt.getPropertyName();
		super.propertyChange(evt);
		if (ExtendedDataUtilities.FILL.equals(name) ||
	        ExtendedDataUtilities.OUTLINE.equals(name) ||
	        Node.PROPERTY_PARAMETER.equals(name)) {
	    	refreshVisuals();
	    }
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		NodeFigure figure = (NodeFigure)getFigure();
		Node model = getModel();
		Control control = model.getControl();
		
		setResizable(control.isResizable());
		
		figure.setShape(control.getShape());
		String parameter = model.getParameter();
		if (parameter == null) {
			figure.setLabel(control.getLabel());
		} else figure.setLabel(parameter + " : " + control.getLabel());
		figure.setToolTip(getToolTip());
		
		PointList points = model.getFittedPolygon();
		if (points != null)
			figure.setPoints(points);
		
		figure.setBackgroundColor(getFill(ExtendedDataUtilities.getFill(model)));
		figure.setForegroundColor(getOutline(ExtendedDataUtilities.getOutline(model)));
		
		figure.repaint();
	}
	
	@Override
	public List<Layoutable> getModelChildren() {
		ArrayList<Layoutable> children =
				new ArrayList<Layoutable>(getModel().getChildren());
		children.addAll(getModel().getPorts());
		return children;
	}
	
	@Override
	public String getToolTip() {
		return getModel().getControl().getName() + " " + getModel().getName();
	}
}
