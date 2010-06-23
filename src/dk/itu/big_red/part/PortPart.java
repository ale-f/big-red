package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.EllipseAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import dk.itu.big_red.editpolicies.EdgeCreationPolicy;
import dk.itu.big_red.figure.PortFigure;
import dk.itu.big_red.figure.adornments.CentreAnchor;
import dk.itu.big_red.model.EdgeConnection;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.interfaces.IConnectable;

public class PortPart extends AbstractGraphicalEditPart implements NodeEditPart, PropertyChangeListener {
	@Override
	public Port getModel() {
		return (Port)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new PortFigure();
	}

	public void activate() {
		super.activate();
		getModel().addPropertyChangeListener(this);
	}

	public void deactivate() {
		getModel().removePropertyChangeListener(this);
		super.deactivate();
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeCreationPolicy());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(IConnectable.PROPERTY_SOURCE_EDGE)) {
			refreshSourceConnections();
	    }
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		Port model = getModel();
		PortFigure figure = (PortFigure)getFigure();
		
		figure.setConstraint(model.getLayout());
		figure.setRootConstraint(model.getRootLayout());
	}
	
	@Override
	protected List<EdgeConnection> getModelSourceConnections() {
        return getModel().getConnections();
    }
	
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new CentreAnchor(getFigure());
    }
    
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new CentreAnchor(getFigure());
    }
	
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new CentreAnchor(getFigure());
    }
    
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new CentreAnchor(getFigure());
    }
}
