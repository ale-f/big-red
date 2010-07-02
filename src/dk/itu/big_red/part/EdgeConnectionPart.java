package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;

import dk.itu.big_red.editpolicies.EdgeConnectionDeletePolicy;
import dk.itu.big_red.editpolicies.EdgeCreationPolicy;
import dk.itu.big_red.figure.EdgeConnectionFigure;
import dk.itu.big_red.model.EdgeConnection;

public class EdgeConnectionPart extends AbstractConnectionEditPart implements NodeEditPart, PropertyChangeListener {
	/**
	 * Returns the {@link EdgePart} corresponding to this connection's
	 * {@link Edge}.
	 * @return an EdgeTargetPart
	 */
	public EdgePart getEdgePart() {
		return ((EdgePart)this.getViewer().getEditPartRegistry().get(getModel().getParent()));
	}
	
	@Override
	public EdgeConnection getModel() {
		return (EdgeConnection)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new EdgeConnectionFigure();
	}

	public void activate() {
		super.activate();
		getModel().addPropertyChangeListener(this);
		refreshVisuals();
	}

	public void deactivate() {
		getModel().removePropertyChangeListener(this);
		super.deactivate();
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeCreationPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new EdgeConnectionDeletePolicy());
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE,
                          new ConnectionEndpointEditPolicy());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		/*
		 * When in doubt, refresh visuals!
		 */
		refreshVisuals();
	}

	public void refreshVisuals() {
		EdgeConnectionFigure figure = (EdgeConnectionFigure)getFigure();
		EdgeConnection model = getModel();
		
		figure.setToolTip(model.getComment());
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connection) {
		return getEdgePart().getSourceConnectionAnchor(connection);
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connection) {
		return getEdgePart().getTargetConnectionAnchor(connection);
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return getEdgePart().getSourceConnectionAnchor(request);
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return getEdgePart().getTargetConnectionAnchor(request);
	}
}
