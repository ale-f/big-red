package dk.itu.big_red.editors.assistants;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.bigraph.model.ModelObject;
import org.eclipse.ui.services.IDisposable;

public class ModelListenerProxy
		implements PropertyChangeListener, IDisposable {
	private final ModelObject modelObject;
	private final PropertyChangeListener target;
	
	public ModelListenerProxy(
			ModelObject modelObject, PropertyChangeListener target) {
		this.target = target;
		this.modelObject = modelObject;
		
		modelObject.addPropertyChangeListener(this);
	}
	
	public ModelObject getModel() {
		return modelObject;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		target.propertyChange(evt);
	}

	@Override
	public void dispose() {
		modelObject.removePropertyChangeListener(this);
	}
}
