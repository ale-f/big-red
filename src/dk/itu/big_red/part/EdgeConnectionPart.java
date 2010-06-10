package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;



import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;

import dk.itu.big_red.editpolicies.EdgeEditPolicy;
import dk.itu.big_red.figure.EdgeFigure;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.EdgeConnection;

public class EdgeConnectionPart extends AbstractConnectionEditPart implements PropertyChangeListener {
	@Override
	public EdgeConnection getModel() {
		return (EdgeConnection)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new EdgeFigure();
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
		installEditPolicy(EditPolicy.CONNECTION_ROLE, new EdgeEditPolicy());
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
		EdgeFigure figure = (EdgeFigure)getFigure();
		EdgeConnection model = getModel();
		
		figure.setToolTip(model.getComment());
	}
}
