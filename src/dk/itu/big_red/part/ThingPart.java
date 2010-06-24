package dk.itu.big_red.part;

import org.eclipse.gef.EditPart;

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
		
		figure.setToolTip(model.getClass().getSimpleName());
		
		figure.setConstraint(model.getLayout());
		figure.setRootConstraint(model.getRootLayout());
		
		for (Object i : getChildren())
			((EditPart)i).refresh();
	}
}
