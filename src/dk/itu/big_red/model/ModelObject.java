package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import dk.itu.big_red.model.interfaces.internal.IPropertyChangeNotifier;

public class ModelObject implements IPropertyChangeNotifier {
	protected final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	@Override
	public final void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	@Override
	public final void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}

}
