package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;

/**
 * Objects implementing IPropertyChangeNotifier are model objects which are
 * happy to communicate changes in their state to
 * {@link PropertyChangeListener}s.
 * @author alec
 *
 */
public interface IPropertyChangeNotifier {
	/**
	 * Registers a {@link PropertyChangeListener} to receive property change
	 * notifications from this object.
	 * @param listener the PropertyChangeListener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener);
	
	/**
	 * Unregisters a {@link PropertyChangeListener} from receiving property
	 * change notifications from this object.
	 * @param listener the PropertyChangeListener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener);
}
