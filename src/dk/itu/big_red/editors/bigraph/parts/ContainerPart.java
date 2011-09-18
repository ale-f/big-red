package dk.itu.big_red.editors.bigraph.parts;

import dk.itu.big_red.editors.bigraph.figures.AbstractFigure;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;

/**
 * The ContainerPart is the base class for edit parts whose model objects are
 * instances of {@link Container}, the ridiculously-named model superclass which
 * provides a useful default implementation of {@link ILayoutable}.
 * @author alec
 *
 */
public abstract class ContainerPart extends AbstractPart {
	@Override
	public Container getModel() {
		return (Container)super.getModel();
	}
	
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		
		AbstractFigure figure = (AbstractFigure)getFigure();
		Container model = getModel();
		
		String toolTip = model.getClass().getSimpleName();
		if (model.getComment() != null)
			toolTip += "\n\n" + model.getComment();
		figure.setToolTip(toolTip);
		
		figure.setConstraint(model.getLayout());
	}
}
