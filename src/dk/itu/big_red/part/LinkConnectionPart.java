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
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;

import dk.itu.big_red.editpolicies.LinkConnectionDeletePolicy;
import dk.itu.big_red.editpolicies.EdgeCreationPolicy;
import dk.itu.big_red.figure.LinkConnectionFigure;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.LinkConnection;

/**
 * EdgeConnectionParts represent {@link LinkConnection}s, the individual
 * connections that together comprise an {@link Edge}.
 * @see Edge
 * @see LinkConnection
 * @see EdgePart
 * @author alec
 *
 */
public class LinkConnectionPart extends AbstractConnectionEditPart implements NodeEditPart, PropertyChangeListener {
	/**
	 * Returns the {@link NodeEditPart} corresponding to this connection's
	 * {@link Link}.
	 * @return a NodeEditPart
	 */
	public NodeEditPart getLinkPart() {
		return ((NodeEditPart)this.getViewer().getEditPartRegistry().get(getModel().getParent()));
	}
	
	@Override
	public LinkConnection getModel() {
		return (LinkConnection)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new LinkConnectionFigure();
	}

	/**
	 * Extends {@link AbstractGraphicalEditPart#activate()} to also register to
	 * receive property change notifications from both the model object <i>and</i>
	 * its parent {@link Edge}.
	 */
	public void activate() {
		super.activate();
		getModel().getParent().addPropertyChangeListener(this);
		refreshVisuals();
	}
	
	/**
	 * Extends {@link AbstractGraphicalEditPart#activate()} to also unregister
	 * from the property change notifications of the model object and its
	 * parent {@link Edge}.
	 */
	public void deactivate() {
		getModel().getParent().removePropertyChangeListener(this);
		super.deactivate();
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeCreationPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LinkConnectionDeletePolicy());
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
		LinkConnectionFigure figure = (LinkConnectionFigure)getFigure();
		LinkConnection model = getModel();
		
		figure.setToolTip(model.getParent().getComment());
		
		figure.setOutlineColour(model.getParent().getOutlineColour());
	}

	/**
	 * Proxies requests for a source connection anchor to the parent {@link
	 * Edge}'s {@link EdgePart}.
	 */
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connection) {
		return getLinkPart().getSourceConnectionAnchor(connection);
	}

	/**
	 * Proxies requests for a target connection anchor to the parent {@link
	 * Edge}'s {@link EdgePart}.
	 */
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connection) {
		return getLinkPart().getTargetConnectionAnchor(connection);
	}

	/**
	 * Proxies requests for a source connection anchor to the parent {@link
	 * Edge}'s {@link EdgePart}.
	 */
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return getLinkPart().getSourceConnectionAnchor(request);
	}

	/**
	 * Proxies requests for a target connection anchor to the parent {@link
	 * Edge}'s {@link EdgePart}.
	 */
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return getLinkPart().getTargetConnectionAnchor(request);
	}
}
