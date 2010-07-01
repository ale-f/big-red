package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import dk.itu.big_red.editpolicies.EdgeCreationPolicy;
import dk.itu.big_red.figure.EdgeTargetFigure;
import dk.itu.big_red.figure.adornments.CentreAnchor;
import dk.itu.big_red.model.EdgeConnection;
import dk.itu.big_red.model.EdgeTarget;

public class EdgeTargetPart extends AbstractGraphicalEditPart implements NodeEditPart, PropertyChangeListener {
	@Override
	public EdgeTarget getModel() {
		return (EdgeTarget)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		// TODO Auto-generated method stub
		return new EdgeTargetFigure();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeCreationPolicy());
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
	public void propertyChange(PropertyChangeEvent evt) {
		refreshVisuals();
		refreshTargetConnections();
	}
	
	public void refreshVisuals() {
		EdgeTargetFigure figure = (EdgeTargetFigure)getFigure();
		EdgeTarget model = getModel();
		
		figure.setConstraint(model.getLayout());
		
		figure.setToolTip(model.getEdge().getComment());
	}

	@Override
	protected List<EdgeConnection> getModelTargetConnections() {
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
