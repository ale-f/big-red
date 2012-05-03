package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.editors.bigraph.LayoutableLayoutPolicy;
import dk.itu.big_red.editors.bigraph.figures.NodeFigure;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Control.Shape;

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
		if (Layoutable.PROPERTY_LAYOUT.equals(name))
			fittedPolygon = null;
		super.propertyChange(evt);
		if (Colourable.PROPERTY_FILL.equals(name) ||
	        Colourable.PROPERTY_OUTLINE.equals(name) ||
	        Node.PROPERTY_PARAMETER.equals(name)) {
	    	refreshVisuals();
	    }
	}
	
	public static PointList fitPolygon(Node n) {
		Control c = n.getControl();
		Rectangle rectangle = n.getLayout();
		PointList points = c.getPoints();
		if (points == null)
			return null;
		PointList fittedPolygon = points.getCopy();

		/*
		 * Move the polygon so that its top-left corner is at (0,0).
		 */
		fittedPolygon.translate(
				points.getBounds().getTopLeft().getNegated());
		
		/*
		 * Work out the scaling factors that'll make the polygon fit inside
		 * the layout.
		 * 
		 * (Note that adjustedBounds.width and adjustedBounds.height are
		 * both off-by-one - getBounds() prefers < to <=, it seems.)
		 */
		Rectangle adjustedBounds = new Rectangle(fittedPolygon.getBounds());
		double xScale = rectangle.width() - 2,
		       yScale = rectangle.height() - 2;
		xScale /= adjustedBounds.width() - 1;
		yScale /= adjustedBounds.height() - 1;
		
		/*
		 * Scale all of the points.
		 */
		Point tmp = Point.SINGLETON;
		for (int i = 0; i < fittedPolygon.size(); i++) {
			fittedPolygon.getPoint(tmp, i).
				scale(xScale, yScale).translate(1, 1);
			fittedPolygon.setPoint(tmp, i);
		}
		
		return fittedPolygon;
	}
	
	private PointList fittedPolygon = null;
	
	/**
	 * Lazily creates and returns the <i>fitted polygon</i> for this Node (a
	 * copy of its {@link Control}'s polygon, scaled to fit inside this Node's
	 * layout).
	 * 
	 * <p>A call to {@link #setControl} or {@link #setLayout} will invalidate
	 * the fitted polygon.
	 * @return the fitted polygon
	 */
	protected PointList getFittedPolygon() {
		if (fittedPolygon == null) {
			if (getModel().getControl().getShape() == Shape.POLYGON)
				fittedPolygon = fitPolygon(getModel());
		}
		return fittedPolygon;
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
		
		PointList points = getFittedPolygon();
		if (points != null)
			figure.setPoints(points);
		
		figure.setBackgroundColor(getFill(model.getFillColour()));
		figure.setForegroundColor(getOutline(model.getOutlineColour()));
		
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
