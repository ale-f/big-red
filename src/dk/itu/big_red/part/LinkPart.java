package dk.itu.big_red.part;

import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;

import dk.itu.big_red.figure.AbstractFigure;
import dk.itu.big_red.figure.adornments.FixedPointAnchor;
import dk.itu.big_red.figure.adornments.FixedPointAnchor.Orientation;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.LinkConnection;

public abstract class LinkPart extends AbstractPart implements NodeEditPart, PropertyChangeListener {

	public LinkPart() {
		super();
	}

	@Override
	public Link getModel() {
		return (Link)super.getModel();
	}

	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		AbstractFigure figure = (AbstractFigure)getFigure();
		Link model = getModel();
		
		figure.setConstraint(model.getLayout());
		
		String toolTip = model.getName();
		if (model.getComment() != null)
			toolTip += "\n\n" + model.getComment();
		figure.setToolTip(toolTip);
		
		figure.setBackgroundColor(model.getOutlineColour());
	}
	
	/**
	 * Returns a list of all the {@link LinkConnection}s for which the model
	 * object is the <i>target</i>.
	 * 
	 * <p>Note that LinkParts are always targets rather than sources, so
	 * there's no need to override the {@link
	 * AbstractPart#getModelSourceConnections()} implementation.
	 */
	@Override
	protected List<LinkConnection> getModelTargetConnections() {
        return getModel().getConnections();
    }
	
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return null;
    }
    
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return null;
    }
	
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new FixedPointAnchor(getFigure(), getAnchorOrientation());
    }
    
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new FixedPointAnchor(getFigure(), getAnchorOrientation());
    }
	
	/**
	 * Returns the {@link Orientation} which should be given to any of this
	 * PointPart's target {@link FixedPointAnchor}s.
	 * @return an Orientation
	 */
	abstract public Orientation getAnchorOrientation();
}