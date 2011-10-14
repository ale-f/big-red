package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;

import dk.itu.big_red.editors.bigraph.figures.AbstractFigure;
import dk.itu.big_red.editors.bigraph.figures.assistants.FixedPointAnchor;
import dk.itu.big_red.editors.bigraph.figures.assistants.FixedPointAnchor.Orientation;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.assistants.LinkConnection;

public abstract class PointPart extends AbstractPart implements NodeEditPart, PropertyChangeListener {

	public PointPart() {
		super();
	}

	@Override
	public Point getModel() {
		return (Point)super.getModel();
	}

	@Override
	public void activate() {
		super.activate();
		getModel().addPropertyChangeListener(this);
		if (getModel().getLink() != null)
			getModel().getLink().addPropertyChangeListener(this);
	}

	@Override
	public void deactivate() {
		if (getModel().getLink() != null)
			getModel().getLink().removePropertyChangeListener(this);
		getModel().removePropertyChangeListener(this);
		super.deactivate();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		String prop = evt.getPropertyName();
		Object source = evt.getSource();
		if (source == getModel()) {
			if (prop.equals(Layoutable.PROPERTY_NAME)) {
				refreshVisuals();
			} else if (prop.equals(Point.PROPERTY_LINK)) {
				Link oldLink = (Link)evt.getOldValue(),
						newLink = (Link)evt.getNewValue();
				if (oldLink != null)
					oldLink.removePropertyChangeListener(this);
				if (newLink != null)
					newLink.addPropertyChangeListener(this);
				refreshSourceConnections();
				refreshVisuals();
		    } else if (prop.equals(ModelObject.PROPERTY_COMMENT)) {
		    	refreshVisuals();
		    }
		} else if (source == getModel().getLink()) {
			if (prop.equals(Link.PROPERTY_NAME) ||
				prop.equals(Colourable.PROPERTY_OUTLINE)) {
				refreshVisuals();
			}
		}
	}
	
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		
		Point model = getModel();
		AbstractFigure figure = getFigure();
		
		String toolTip = model.getName();
		Link l = model.getLink();
		if (l != null)
			toolTip += "\n(connected to " + l.getName() + ")";
		setToolTip(toolTip);
		
		figure.setBackgroundColor(l != null ?
				l.getOutlineColour().getSWTColor() : Point.DEFAULT_COLOUR.getSWTColor());
	}
	
	@Override
	protected List<LinkConnection> getModelSourceConnections() {
		ArrayList<LinkConnection> l = new ArrayList<LinkConnection>();
		Link link = getModel().getLink();
		if (link != null)
			l.add(link.getConnectionFor(getModel()));
        return l;
    }

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new FixedPointAnchor(getFigure(), getAnchorOrientation());
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		 return new FixedPointAnchor(getFigure(), getAnchorOrientation());
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new FixedPointAnchor(getFigure(), getAnchorOrientation());
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new FixedPointAnchor(getFigure(), getAnchorOrientation());
	}
	
	/**
	 * Returns the {@link Orientation} which should be given to any of this
	 * PointPart's source {@link FixedPointAnchor}s.
	 * @return an Orientation
	 */
	abstract public Orientation getAnchorOrientation();
}