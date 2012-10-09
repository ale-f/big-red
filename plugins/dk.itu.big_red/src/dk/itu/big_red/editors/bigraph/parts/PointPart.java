package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.Point;

import dk.itu.big_red.model.Colour;
import dk.itu.big_red.model.ColourUtilities;
import dk.itu.big_red.model.ExtendedDataUtilities;

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
			if (Layoutable.PROPERTY_NAME.equals(prop)) {
				refreshVisuals();
			} else if (Point.PROPERTY_LINK.equals(prop)) {
				Link oldLink = (Link)evt.getOldValue(),
						newLink = (Link)evt.getNewValue();
				if (oldLink != null)
					oldLink.removePropertyChangeListener(this);
				if (newLink != null)
					newLink.addPropertyChangeListener(this);
				refreshSourceConnections();
				refreshVisuals();
		    } else if (ExtendedDataUtilities.COMMENT.equals(prop)) {
		    	refreshVisuals();
		    }
		} else if (source == getModel().getLink()) {
			if (Link.PROPERTY_NAME.equals(prop) ||
				ColourUtilities.OUTLINE.equals(prop)) {
				refreshVisuals();
			}
		}
	}
	
	abstract String getTypeName();
	
	@Override
	public String getToolTip() {
		Link l = getModel().getLink();
		return getTypeName() + " " + getModel().getName() +
			(l != null ? "\n(connected to link " + l.getName() + ")" : "");
	}
	
	/**
	 * The colour to be given to Points not connected to a {@link Link}.
	 */
	public static final Colour DEFAULT_COLOUR = new Colour("red");
	
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		
		Link l = getModel().getLink();
		getFigure().setBackgroundColor(getOutline(
				l != null ? ColourUtilities.getOutline(l) : DEFAULT_COLOUR));
	}
	
	@Override
	protected List<LinkPart.Connection> getModelSourceConnections() {
		ArrayList<LinkPart.Connection> l = new ArrayList<LinkPart.Connection>();
		Link link = getModel().getLink();
		if (link != null)
			l.add(new LinkPart.Connection(link, getModel()));
        return l;
    }
	
	@Override
	void layoutChange(int generations) {
		EdgePart ep = getPartFor(getModel().getLink(), EdgePart.class);
		if (ep != null)
			ep.refreshLocation();
	}
}