package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
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
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.interfaces.internal.ICommentable;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.model.interfaces.internal.INameable;

public abstract class PointPart extends AbstractPart implements NodeEditPart {

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
		String prop = evt.getPropertyName();
		Object source = evt.getSource();
		if (source == getModel()) {
			if (prop.equals(ILayoutable.PROPERTY_LAYOUT) ||
				prop.equals(INameable.PROPERTY_NAME)) {
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
		    } else if (prop.equals(ICommentable.PROPERTY_COMMENT)) {
		    	refreshVisuals();
		    }
		} else if (source == getModel().getLink()) {
			if (prop.equals(Link.PROPERTY_NAME) ||
				prop.equals(Link.PROPERTY_OUTLINE_COLOUR)) {
				refreshVisuals();
			}
		}
	}
	
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		
		Point model = getModel();
		AbstractFigure figure = (AbstractFigure)getFigure();
		
		figure.setConstraint(model.getLayout());
		
		String toolTip = model.getName();
		Link l = model.getLink();
		if (l != null)
			toolTip += "\n(connected to " + l.getName() + ")";
		if (model.getComment() != null)
			toolTip += "\n\n" + model.getComment();
		figure.setToolTip(toolTip);
		
		figure.setBackgroundColor(l != null ?
				l.getOutlineColour() : Point.DEFAULT_COLOUR);
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
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new FixedPointAnchor(getFigure(), Orientation.CALCULATE);
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new FixedPointAnchor(getFigure(), Orientation.CALCULATE);
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return null;
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return null;
	}

}