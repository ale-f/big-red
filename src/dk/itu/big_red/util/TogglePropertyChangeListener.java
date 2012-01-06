package dk.itu.big_red.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public abstract class TogglePropertyChangeListener implements PropertyChangeListener {
	private boolean enabled = true, running = false;
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!enabled || running)
			return;
		try {
			running = true;
			propertyChange2(evt);
		} finally {
			running = false;
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean getEnabled() {
		return enabled;
	}
	
	/**
	 * Called when a bound property has changed.
	 * <p>All other property change notifications will be discarded until this
	 * method returns.
	 * @param evt a {@link PropertyChangeEvent} containing details of the
	 * property change
	 */
	protected abstract void propertyChange2(PropertyChangeEvent evt);
}
