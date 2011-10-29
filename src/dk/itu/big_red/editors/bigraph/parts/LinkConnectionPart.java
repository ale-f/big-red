package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PartInitException;

import dk.itu.big_red.editors.bigraph.EdgeCreationPolicy;
import dk.itu.big_red.editors.bigraph.LinkConnectionDeletePolicy;
import dk.itu.big_red.editors.bigraph.figures.LinkConnectionFigure;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.util.UI;

/**
 * LinkConnectionParts represent {@link Link.Connection}s, the individual
 * connections that together comprise an {@link Edge}.
 * @see Edge
 * @see Link.Connection
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
		return ((NodeEditPart)getViewer().getEditPartRegistry().get(getModel().getLink()));
	}
	
	@Override
	public Link.Connection getModel() {
		return (Link.Connection)super.getModel();
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
	@Override
	public void activate() {
		super.activate();
		getModel().getLink().addPropertyChangeListener(this);
		refreshVisuals();
	}
	
	/**
	 * Extends {@link AbstractGraphicalEditPart#activate()} to also unregister
	 * from the property change notifications of the model object and its
	 * parent {@link Edge}.
	 */
	@Override
	public void deactivate() {
		getModel().getLink().removePropertyChangeListener(this);
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
		String prop = evt.getPropertyName();
		if (evt.getSource() == getModel().getLink()) {
			if (prop.equals(Colourable.PROPERTY_OUTLINE)) {
				refreshVisuals();
			}
		}
	}

	@Override
	public void refreshVisuals() {
		LinkConnectionFigure figure = (LinkConnectionFigure)getFigure();
		Link.Connection model = getModel();
		
		String s = getDisplayName();
		if (model.getComment() != null)
			s += "\n\n" + model.getComment();
		figure.setToolTip(s);
		
		figure.setForegroundColor(model.getLink().getOutlineColour().getSWTColor());
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
	
	/**
	 * Handles {@link RequestConstants#REQ_OPEN} requests by opening the
	 * property sheet.
	 */
	@Override
	public void performRequest(Request req) {
		if (req.getType().equals(RequestConstants.REQ_OPEN)) {
			try {
				UI.getWorkbenchPage().showView(IPageLayout.ID_PROP_SHEET);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getDisplayName() {
		Link l = getModel().getLink();
		return "Connection to " +
				(l instanceof OuterName ? "outer name" :
					l instanceof Edge ? "edge" : "link") + " " + l.getName();
	}
}
