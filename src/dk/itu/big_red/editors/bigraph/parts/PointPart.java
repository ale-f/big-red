package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import dk.itu.big_red.editors.bigraph.figures.AbstractFigure;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Point;

public abstract class PointPart extends ConnectablePart {
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
		if (getModel().getLink() != null)
			getModel().getLink().addPropertyChangeListener(this);
	}

	@Override
	public void deactivate() {
		if (getModel().getLink() != null)
			getModel().getLink().removePropertyChangeListener(this);
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
		
		String toolTip = getDisplayName();
		Link l = model.getLink();
		if (l != null)
			toolTip += "\n(connected to " + l.getName() + ")";
		setToolTip(toolTip);
		
		figure.setBackgroundColor(l != null ?
				l.getOutlineColour().getSWTColor() : Point.DEFAULT_COLOUR.getSWTColor());
	}
	
	@Override
	protected List<Link.Connection> getModelSourceConnections() {
		ArrayList<Link.Connection> l = new ArrayList<Link.Connection>();
		Link link = getModel().getLink();
		if (link != null)
			l.add(link.getConnectionFor(getModel()));
        return l;
    }
}