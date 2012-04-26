package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;

import dk.itu.big_red.editors.bigraph.EdgeCreationPolicy;
import dk.itu.big_red.editors.bigraph.figures.PortFigure;
import dk.itu.big_red.editors.bigraph.figures.assistants.FixedPointAnchor.Orientation;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.assistants.Ellipse;
import dk.itu.big_red.model.assistants.Line;

/**
 * PortParts represent {@link Port}s, sites on {@link Node}s which can be
 * connected to {@link Edge}s.
 * @see Port
 * @author alec
 *
 */
public class PortPart extends PointPart {
	@Override
	public void activate() {
		super.activate();
		getModel().getParent().addPropertyChangeListener(this);
	}

	@Override
	public void deactivate() {
		getModel().getParent().removePropertyChangeListener(this);
		super.deactivate();
	}
	
	@Override
	protected IFigure createFigure() {
		return new PortFigure();
	}
	
	@Override
	public Port getModel() {
		return (Port)super.getModel();
	}
	
	@Override
	public void installEditPolicy(Object key, EditPolicy editPolicy) {
		if (key != EditPolicy.PRIMARY_DRAG_ROLE)
			super.installEditPolicy(key, editPolicy);
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeCreationPolicy());
		super.installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new NonResizableEditPolicy() {{
			setDragAllowed(false);
		}});
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		String prop = evt.getPropertyName();
		Object source = evt.getSource();
		if (source == getModel().getParent()) {
			if (prop.equals(Layoutable.PROPERTY_LAYOUT))
				refreshVisuals();
		}
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		setResizable(false);
		
		Rectangle r = new Rectangle(0, 0, 10, 10);
		PointList polypt = ((NodePart)getParent()).getFittedPolygon();
		double distance = getModel().getSpec().getDistance();
		if (polypt != null) {
			int segment = getModel().getSpec().getSegment();
			org.eclipse.draw2d.geometry.Point p1 = polypt.getPoint(segment),
			      p2 = polypt.getPoint((segment + 1) % polypt.size());
			r.setLocation(new Line(p1, p2).
					getPointFromOffset(distance).translate(-5, -5));
		} else {
			r.setLocation(
				new Ellipse(
						getModel().getParent().getLayout().getCopy().
						setLocation(0, 0)).
						getPointFromOffset(distance).translate(-5, -5));
		}
		getFigure().setConstraint(r);
	}
	
	@Override
	public Orientation getAnchorOrientation() {
		return Orientation.CENTER;
	}
	
	@Override
	public String getTypeName() {
		return "Port";
	}
}
