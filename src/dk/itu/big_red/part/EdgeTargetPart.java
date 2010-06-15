package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import dk.itu.big_red.editpolicies.EdgeCreationPolicy;
import dk.itu.big_red.figure.EdgeConnectionFigure;
import dk.itu.big_red.figure.EdgeTargetFigure;
import dk.itu.big_red.model.EdgeConnection;
import dk.itu.big_red.model.EdgeTarget;

public class EdgeTargetPart extends AbstractGraphicalEditPart implements PropertyChangeListener {
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
		figure.setRootConstraint(model.getLayout());
		
		figure.setToolTip(model.getParent().getComment());
	}

	@Override
	protected List<EdgeConnection> getModelTargetConnections() {
        return getModel().getConnections();
    }
}
