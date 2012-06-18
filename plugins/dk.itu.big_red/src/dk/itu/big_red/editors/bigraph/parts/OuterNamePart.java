package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;

import org.bigraph.model.InnerName;
import org.eclipse.draw2d.IFigure;
import dk.itu.big_red.editors.bigraph.figures.NameFigure;
import dk.itu.big_red.editors.bigraph.figures.assistants.FixedPointAnchor.Orientation;

public class OuterNamePart extends LinkPart {
	@Override
	protected IFigure createFigure() {
		return new NameFigure();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getPropertyName().equals(InnerName.PROPERTY_NAME)) {
	    	refreshVisuals();
	    }
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		((NameFigure)getFigure()).setName(getModel().getName());
	}
	
	@Override
	public Orientation getAnchorOrientation() {
		return Orientation.SOUTH;
	}
	
	@Override
	public String getToolTip() {
		return "Outer name " + getModel().getName();
	}
}
