package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import dk.itu.big_red.editors.bigraph.figures.NameFigure;
import dk.itu.big_red.editors.bigraph.figures.assistants.FixedPointAnchor.Orientation;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.ModelObject;

public class OuterNamePart extends LinkPart {
	@Override
	protected IFigure createFigure() {
		return new NameFigure();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getPropertyName().equals(InnerName.PROPERTY_NAME) ||
			evt.getPropertyName().equals(ModelObject.PROPERTY_COMMENT)) {
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
	public String getDisplayName() {
		return "Outer name " + getModel().getName();
	}
}
