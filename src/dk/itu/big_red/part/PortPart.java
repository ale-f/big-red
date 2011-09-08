package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import dk.itu.big_red.editpolicies.EdgeCreationPolicy;
import dk.itu.big_red.figure.PortFigure;
import dk.itu.big_red.figure.adornments.FixedPointAnchor.Orientation;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;

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
			if (prop.equals(ILayoutable.PROPERTY_LAYOUT))
				refreshVisuals();
		}
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		setResizable(false);
	}
	
	@Override
	public Orientation getAnchorOrientation() {
		return Orientation.CENTER;
	}
}
