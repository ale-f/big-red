package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;

import dk.itu.big_red.editors.bigraph.EdgeCreationPolicy;
import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.editors.bigraph.LayoutableLayoutPolicy;
import dk.itu.big_red.editors.bigraph.figures.AbstractFigure;
import dk.itu.big_red.editors.bigraph.figures.assistants.FixedPointAnchor;
import dk.itu.big_red.editors.bigraph.figures.assistants.FixedPointAnchor.Orientation;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.assistants.LinkConnection;

public abstract class LinkPart extends AbstractPart implements NodeEditPart, PropertyChangeListener {

	public LinkPart() {
		super();
	}

	@Override
	public Link getModel() {
		return (Link)super.getModel();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getPropertyName().equals(Link.PROPERTY_POINT)) {
	    	refreshTargetConnections();
	    }
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		AbstractFigure figure = getFigure();
		Link model = getModel();
		
		setToolTip(model.getName());
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
        ArrayList<LinkConnection> l = new ArrayList<LinkConnection>();
        for (Point p : getModel().getPoints())
        	l.add(getModel().getConnectionFor(p));
        return l;
    }
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutableLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeCreationPolicy());
	}
	
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new FixedPointAnchor(getFigure(), getAnchorOrientation());
    }
    
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new FixedPointAnchor(getFigure(), getAnchorOrientation());
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