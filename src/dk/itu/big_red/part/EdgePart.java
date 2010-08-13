package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;

import dk.itu.big_red.editpolicies.EdgeCreationPolicy;
import dk.itu.big_red.editpolicies.ILayoutableDeletePolicy;
import dk.itu.big_red.figure.EdgeFigure;
import dk.itu.big_red.figure.adornments.FixedPointAnchor;
import dk.itu.big_red.model.Edge;
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
public class EdgePart extends AbstractPart implements NodeEditPart, PropertyChangeListener {
	@Override
	public Edge getModel() {
		return (Edge)super.getModel();
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
		refreshVisuals();
		refreshTargetConnections();
	}
	
	public void refreshVisuals() {
		EdgeFigure figure = (EdgeFigure)getFigure();
		Edge model = getModel();
		
		setResizable(false);
		
		figure.setConstraint(model.getLayout());
		
		figure.setToolTip(model.getComment());
		
		figure.setBackgroundColor(model.getOutlineColour());
	}

	/**
	 * Returns a list of all the {@link LinkConnection}s for which the model
	 * object is the <i>target</i>.
	 * 
	 * <p>Note that EdgeParts are always targets rather than sources, so
	 * there's no need to override the {@link
	 * AbstractPart#getModelSourceConnections()} implementation.
	 */
	@Override
	protected List<LinkConnection> getModelTargetConnections() {
        return getModel().getConnections();
    }
	
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new FixedPointAnchor(getFigure());
    }
    
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new FixedPointAnchor(getFigure());
    }
	
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new FixedPointAnchor(getFigure());
    }
    
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new FixedPointAnchor(getFigure());
    }
}
