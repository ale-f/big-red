package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import dk.itu.big_red.model.interfaces.internal.IPropertyChangeNotifier;

/**
 * This is the superclass of everything in Big Red's version of the bigraphical
 * model. It provides an implementation of {@link IPropertyChangeNotifier}.
 * 
 * <p>Objects which can appear on a bigraph are instances of the subclass
 * {@link LayoutableModelObject}.
 * @author alec
 * @see LayoutableModelObject
 *
 */
public class ModelObject implements IPropertyChangeNotifier {
	private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	@Override
	public final void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	@Override
	public final void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}

	/**
	 * Notifies all associated {@link PropertyChangeListener}s of a property
	 * change.
	 * @param propertyName the ID of the changed property
	 * @param oldValue its old value
	 * @param newValue its new value
	 */
	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		listeners.firePropertyChange(propertyName, oldValue, newValue);
	}
}
