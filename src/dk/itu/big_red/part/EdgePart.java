package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import dk.itu.big_red.editpolicies.EdgeCreationPolicy;
import dk.itu.big_red.editpolicies.ILayoutableDeletePolicy;
import dk.itu.big_red.figure.EdgeFigure;
import dk.itu.big_red.figure.assistants.FixedPointAnchor.Orientation;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.LinkConnection;

/**
 * EdgeParts represent {@link Edge}s, the container for - and target point of -
 * {@link LinkConnection}s.
 * @see Edge
 * @see LinkConnection
 * @see LinkConnectionPart
 * @author alec
 *
 */
public class EdgePart extends LinkPart {
	@Override
	public Link getModel() {
		return super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new EdgeFigure();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ILayoutableDeletePolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeCreationPolicy());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		refreshVisuals();
		refreshTargetConnections();
	}
	
	@Override
	public void refreshVisuals() {
		super.refreshVisuals();
		setResizable(false);
	}
	
	@Override
	public Orientation getAnchorOrientation() {
		return Orientation.CENTER;
	}
}
