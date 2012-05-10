package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.services.IDisposable;

import dk.itu.big_red.model.assistants.IPropertyProviderProxy;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;

/**
 * This is the superclass of everything in Big Red's version of the bigraphical
 * model. It allows {@link PropertyChangeListener}s to register for, and
 * unregister from, change notifications, and has a {@link String} comment
 * which can be set and retrieved.
 * 
 * <p>Objects which can appear on a bigraph are instances of the subclass
 * {@link Layoutable}.
 * @author alec
 * @see Layoutable
 *
 */
public abstract class ModelObject implements IDisposable {
	public abstract class ModelObjectChange extends Change {
		/**
		 * Gets the {@link ModelObject} which created this {@link ModelObjectChange}.
		 * @return
		 */
		public ModelObject getCreator() {
			return ModelObject.this;
		}
	}
	
	public static interface ExtendedDataValidator {
		String validate(ChangeExtendedData c, IPropertyProviderProxy context);
	}
	
	public class ChangeExtendedData extends ModelObjectChange {
		public String key;
		public Object newValue;
		public ExtendedDataValidator validator;
		
		protected ChangeExtendedData(
				String key, Object newValue, ExtendedDataValidator validator) {
			this.key = key;
			this.newValue = newValue;
			this.validator = validator;
		}
		
		private Object oldValue;
		
		@Override
		public void beforeApply() {
			oldValue = getCreator().getExtendedData(key);
		}
		
		@Override
		public Change inverse() {
			return new ChangeExtendedData(key, oldValue, validator);
		}
	}
	
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	/**
	 * Registers a {@link PropertyChangeListener} to receive property change
	 * notifications from this object.
	 * @param listener the PropertyChangeListener
	 */
	public final void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	/**
	 * Unregisters a {@link PropertyChangeListener} from receiving property
	 * change notifications from this object.
	 * @param listener the PropertyChangeListener
	 */
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
	
	/**
	 * Returns a new instance of this {@link ModelObject}'s class,
	 * created as though by <code>this.getClass().newInstance()</code>.
	 * @return a new instance of this ModelObject's class, or
	 * <code>null</code>
	 */
	public ModelObject newInstance() {
		try {
			return getClass().newInstance();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Creates and returns a new copy of this {@link ModelObject}.
	 * <p>(Although the returned copy is a {@link ModelObject}, it's
	 * really an instance of whatever subclass this object is.)
	 * @param m a {@link CloneMap} to be notified of the new copy, or
	 * <code>null</code>
	 * @return a new copy of this {@link ModelObject}
	 */
	public ModelObject clone(Map<ModelObject, ModelObject> m) {
		ModelObject i = newInstance();
		if (m != null)
			m.put(this, i);
		return i;
	}
	
	@Override
	public ModelObject clone() {
		return clone(null);
	}
	
	protected Object getProperty(String name) {
		return null;
	}
	
	protected Object getProperty(IPropertyProviderProxy context, String name) {
		if (context == null || !context.hasProperty(this, name)) {
			return getProperty(name);
		} else return context.getProperty(this, name);
	}
	
	@Override
	public String toString() {
		return "<" + getType() + "@" + System.identityHashCode(this) + ">";
	}
	
	/**
	 * Returns the name of this object's type.
	 * @return the name, as a {@link String}
	 */
	public String getType() {
		return getClass().getSimpleName();
	}
	
	public Change changeExtendedData(String key, Object newValue) {
		return changeExtendedData(key, newValue, null);
	}
	
	public Change changeExtendedData(
			String key, Object newValue, ExtendedDataValidator validator) {
		return new ChangeExtendedData(key, newValue, validator);
	}
	
	@Override
	public void dispose() {
		PropertyChangeListener[] pls =
			listeners.getPropertyChangeListeners().clone();
		for (PropertyChangeListener i : pls)
			listeners.removePropertyChangeListener(i);
		listeners = null;
		
		if (extendedData != null) {
			extendedData.clear();
			extendedData = null;
		}
	}
	
	private Map<String, Object> extendedData;
	
	/**
	 * Retrieves a piece of extended data from this object.
	 * @param key a key
	 * @return an {@link Object}, or <code>null</code> if the key has no
	 * associated data
	 */
	public Object getExtendedData(String key) {
		return (extendedData != null ? extendedData.get(key) : null);
	}
	
	/**
	 * Adds a piece of extended data to this object.
	 * @param key a key
	 * @param value an {@link Object} to associate with the key, or
	 * <code>null</code> to remove an existing association
	 */
	public void setExtendedData(String key, Object value) {
		if (key == null)
			return;
		Object oldValue;
		if (value == null) {
			if (extendedData == null)
				return;
			if ((oldValue = extendedData.remove(key)) != null) {
				if (extendedData.isEmpty())
					extendedData = null;
			}
		} else {
			if (extendedData == null)
				extendedData = new HashMap<String, Object>();
			oldValue = extendedData.put(key, value);
		}
		firePropertyChange(key, oldValue, value);
	}
	
	/**
	 * Overwrites this object's extended data with the data from another
	 * object.
	 * @param m a {@link ModelObject} (can be <code>null</code>)
	 */
	protected void setExtendedDataFrom(ModelObject m) {
		if (m != null && m.extendedData != null) {
			extendedData = new HashMap<String, Object>(m.extendedData);
		} else extendedData = null;
	}
	
	protected void doChange(Change c_) {
		c_.beforeApply();
		if (c_ instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)c_)
				doChange(c);
		} else if (c_ instanceof ChangeExtendedData) {
			ChangeExtendedData c = (ChangeExtendedData)c_;
			c.getCreator().setExtendedData(c.key, c.newValue);
		}
	}
}
