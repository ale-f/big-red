package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;

import dk.itu.big_red.editpolicies.EdgeConnectionDeletePolicy;
import dk.itu.big_red.figure.EdgeConnectionFigure;
import dk.itu.big_red.model.EdgeConnection;

public class EdgeConnectionPart extends AbstractConnectionEditPart implements PropertyChangeListener {
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
}
