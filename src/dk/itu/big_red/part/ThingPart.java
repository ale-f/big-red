package dk.itu.big_red.part;

import dk.itu.big_red.figure.AbstractFigure;
import dk.itu.big_red.model.Thing;

public abstract class ThingPart extends AbstractPart {
	@Override
	public Thing getModel() {
		return (Thing)super.getModel();
	}
	
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		
		AbstractFigure figure = (AbstractFigure)getFigure();
		Thing model = getModel();
		
		String toolTip = model.getClass().getSimpleName();
		if (model.getComment() != null)
			toolTip += "\n\n" + model.getComment();
		figure.setToolTip(toolTip);
		
		figure.setConstraint(model.getLayout());
	}
}
