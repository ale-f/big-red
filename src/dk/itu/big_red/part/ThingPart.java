package dk.itu.big_red.part;

import dk.itu.big_red.figure.AbstractFigure;
import dk.itu.big_red.model.Thing;

/**
 * The ThingPart is the base class for edit parts whose model objects are
 * instances of {@link Thing}, the ridiculously-named model superclass which
 * provides a useful default implementation of {@link ILayoutable}.
 * @author alec
 *
 */
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
