package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Control;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Node;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.assistants.ColourUtilities;
import dk.itu.big_red.editors.assistants.Ellipse;
import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.assistants.LayoutUtilities;
import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.editors.bigraph.LayoutableLayoutPolicy;
import dk.itu.big_red.editors.bigraph.figures.NodeFigure;

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
	protected void createEditPolicies() {
		super.createEditPolicies();
		
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutableLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();
		if (LayoutUtilities.LAYOUT.equals(name))
			fittedPolygon = null;
		super.propertyChange(evt);
		if (ColourUtilities.FILL.equals(name) ||
	        ColourUtilities.OUTLINE.equals(name) ||
	        ExtendedDataUtilities.PARAMETER.equals(name)) {
	    	refreshVisuals();
	    }
	}
	
	public static final PointList fitPolygon(PointList p, Rectangle l) {
		p = p.getCopy();
		p.translate(p.getBounds().getTopLeft().getNegated());
		
		Rectangle adjustedBounds = new Rectangle(p.getBounds());
		double xScale = l.width() - 2,
		       yScale = l.height() - 2;
		xScale /= adjustedBounds.width() - 1;
		yScale /= adjustedBounds.height() - 1;
		
		Point tmp = Point.SINGLETON;
		for (int i = 0; i < p.size(); i++) {
			p.getPoint(tmp, i).scale(xScale, yScale).translate(1, 1);
			p.setPoint(tmp, i);
		}
		
		return p;
	}
	
	private PointList fittedPolygon;
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		NodeFigure figure = (NodeFigure)getFigure();
		Node model = getModel();
		Control control = model.getControl();
		
		Object shape = ExtendedDataUtilities.getShape(control);
		if (shape instanceof PointList && fittedPolygon == null)
			fittedPolygon = fitPolygon((PointList)shape,
					LayoutUtilities.getLayout(model));
		figure.setShape(
			shape instanceof PointList ? fittedPolygon : Ellipse.SINGLETON);
		
		String
			label = ExtendedDataUtilities.getLabel(control),
			parameter = ExtendedDataUtilities.getParameter(model);
		if (parameter != null)
			label = parameter + " : " + label;
		figure.setLabel(label);
		figure.setToolTip(getToolTip());
		
		figure.setBackgroundColor(getFill(ColourUtilities.getFill(model)));
		figure.setForegroundColor(getOutline(ColourUtilities.getOutline(model)));
		
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
