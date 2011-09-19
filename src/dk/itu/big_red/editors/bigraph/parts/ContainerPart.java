package dk.itu.big_red.editors.bigraph.parts;

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
		
		setToolTip(getModel().getClass().getSimpleName());
	}
}
