package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;

import dk.itu.big_red.editpolicies.EdgeCreationPolicy;
import dk.itu.big_red.figure.PortFigure;
import dk.itu.big_red.figure.adornments.CentreAnchor;
import dk.itu.big_red.model.EdgeConnection;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.interfaces.IConnectable;
import dk.itu.big_red.util.Geometry;

public class PortPart extends AbstractPart implements NodeEditPart, PropertyChangeListener {
	@Override
	public Port getModel() {
		return (Port)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new PortFigure();
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
		
		setResizable(false);
		
		Port model = getModel();
		PortFigure figure = (PortFigure)getFigure();
		
		Rectangle r = model.getLayout();
		PointList polypt = model.getParent().getFittedPolygon();
		if (polypt != null) {
			int segment = model.getSegment();
			Point p1 = polypt.getPoint(segment),
			      p2 = polypt.getPoint((segment + 1) % polypt.size());
			r.setLocation(Geometry.getPointOnSegment(p1, p2, model.getDistance()).translate(-5, -5));
		}
		figure.setConstraint(r);
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
