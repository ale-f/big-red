package dk.itu.big_red.editors.simulation_spec;

import java.beans.PropertyChangeEvent;
import org.eclipse.jface.viewers.AbstractListViewer;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.utilities.ui.jface.ModelObjectContentProvider;

class SimulationSpecRRContentProvider extends ModelObjectContentProvider {
	public SimulationSpecRRContentProvider(AbstractListViewer alv) {
		super(alv);
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		return ((SimulationSpec)inputElement).getRules().toArray();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!evt.getSource().equals(getInput()))
			return;
		String propertyName = evt.getPropertyName();
		Object oldValue = evt.getOldValue(), newValue = evt.getNewValue();
		if (propertyName.equals(SimulationSpec.PROPERTY_RULE)) {
			if (oldValue == null && newValue != null) { /* added */
				getViewer().add(newValue);
			} else if (oldValue != null && newValue == null) { /* removed */
				getViewer().remove(oldValue);
			}
		}
	}
}
