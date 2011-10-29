package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;

import dk.itu.big_red.model.Container;

/**
 * The ContainerPart is the base class for edit parts whose model objects are
 * instances of {@link Container}.
 * @author alec
 *
 */
public abstract class ContainerPart extends AbstractPart {
	@Override
	public Container getModel() {
		return (Container)super.getModel();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == getModel()) {
			if (evt.getPropertyName().equals(Container.PROPERTY_CHILD))
				refreshChildren();
		}
	}
	
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		
		setToolTip(getDisplayName());
	}
}
