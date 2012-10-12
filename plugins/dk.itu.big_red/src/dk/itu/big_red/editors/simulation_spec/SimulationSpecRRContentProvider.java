package dk.itu.big_red.editors.simulation_spec;

import java.beans.PropertyChangeEvent;

import org.bigraph.model.ReactionRule;
import org.bigraph.model.SimulationSpec;
import org.eclipse.jface.viewers.AbstractListViewer;
import dk.itu.big_red.utilities.ui.jface.ModelObjectListContentProvider;

class SimulationSpecRRContentProvider extends ModelObjectListContentProvider {
	public SimulationSpecRRContentProvider(AbstractListViewer alv) {
		super(alv);
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		return ((SimulationSpec)inputElement).getRules().toArray();
	}
	
	@Override
	protected SimulationSpec getInput() {
		Object o = super.getInput();
		return (o instanceof SimulationSpec ? (SimulationSpec)o : null);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!evt.getSource().equals(getInput()))
			return;
		String propertyName = evt.getPropertyName();
		if (SimulationSpec.PROPERTY_RULE.equals(propertyName)) {
			ReactionRule
				oldValue = (ReactionRule)evt.getOldValue(),
				newValue = (ReactionRule)evt.getNewValue();
			if (oldValue == null && newValue != null) { /* added */
				getViewer().insert(newValue,
						getInput().getRules().indexOf(newValue));
			} else if (oldValue != null && newValue == null) { /* removed */
				getViewer().remove(oldValue);
			}
		}
	}
}
