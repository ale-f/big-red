package org.bigraph.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import org.bigraph.model.assistants.IObjectIdentifier;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

/**
 * All model objects inherit from <strong>ModelObject</strong>. It provides,
 * amongst other things:&mdash;
 * <ul>
 * <li>property change notifications to interested parties;
 * <li>an extensibility mechanism called <i>extended data</i>; and
 * <li>several useful base classes, like {@link ModelObjectChange} and {@link
 * Identifier}.
 * </ul>
 * @author alec
 * @see ModelObjectChange
 * @see Identifier
 * @see #setExtendedData(String, Object)
 */
public abstract class ModelObject {
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	/**
	 * Registers a {@link PropertyChangeListener} to receive property change
	 * notifications from this object.
	 * @param listener the PropertyChangeListener
	 */
	public final void addPropertyChangeListener(
			PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	/**
	 * Unregisters a {@link PropertyChangeListener} from receiving property
	 * change notifications from this object.
	 * @param listener the PropertyChangeListener
	 */
	public final void removePropertyChangeListener(
			PropertyChangeListener listener) {
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
	
	@Override
	protected ModelObject clone() {
		ModelObject i = newInstance();
		i.setExtendedDataFrom(this);
		return i;
	}
	
	protected Object getProperty(String name) {
		if (PROPERTY_EXTENDED_DATA_MAP.equals(name)) {
			return getExtendedDataMap();
		} else return null;
	}
	
	protected <T> T getProperty(
			PropertyScratchpad context, String name, Class<T> klass) {
		if (context != null && context.hasProperty(this, name)) {
			Object o = context.getProperty(this, name);
			if (o == null || klass.isInstance(o)) {
				return klass.cast(o);
			} else throw new RuntimeException(
					"BUG: " + context + "'s entry for " + name + "(" + o +
					") isn't an instance of " + klass.getCanonicalName());
		}
		return klass.cast(getProperty(name));
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
	
	public void dispose() {
		if (listeners != null) {
			for (PropertyChangeListener i :
					listeners.getPropertyChangeListeners().clone())
				listeners.removePropertyChangeListener(i);
			listeners = null;
		}
		
		if (extendedData != null) {
			extendedData.clear();
			extendedData = null;
		}
	}
	
	private static final String PROPERTY_EXTENDED_DATA_MAP =
			"org.bigraph.model.ModelObject:ExtendedDataMap";
	
	Map<String, Object> getExtendedDataMap() {
		return extendedData;
	}
	
	@SuppressWarnings("unchecked")
	Map<String, Object> getExtendedDataMap(
			PropertyScratchpad context) {
		return getProperty(context, PROPERTY_EXTENDED_DATA_MAP, Map.class);
	}
	
	Map<String, Object> getModifiableExtendedDataMap(
			PropertyScratchpad context) {
		if (context != null) {
			return context.getModifiableMap(
					this, PROPERTY_EXTENDED_DATA_MAP, getExtendedDataMap());
		} else return getExtendedDataMap();
	}
	
	private Map<String, Object> extendedData = new HashMap<String, Object>();
	
	/**
	 * Retrieves one of this object's extended data properties.
	 * @param key the property name
	 * @return an {@link Object}, or <code>null</code> if the named property
	 * has no content
	 */
	public Object getExtendedData(String key) {
		return getExtendedData(null, key);
	}
	
	public Object getExtendedData(PropertyScratchpad context, String key) {
		return getExtendedDataMap(context).get(key);
	}
	
	/**
	 * Sets one of this object's extended data properties, broadcasting a
	 * property change event in the process.
	 * @param key the property name
	 * @param value an {@link Object} to assign to the named property, or
	 * <code>null</code> to remove an existing assignment
	 */
	public void setExtendedData(String key, Object value) {
		setExtendedData(null, key, value);
	}
	
	public void setExtendedData(
			PropertyScratchpad context, String key, Object value) {
		if (key == null)
			return;
		Map<String, Object> map = getModifiableExtendedDataMap(context);
		Object oldValue = (value != null ?
				map.put(key, value) : map.remove(key));
		if (context == null)
			firePropertyChange(key, oldValue, value);
	}
	
	/**
	 * Overwrites this object's extended data with the data from another
	 * object.
	 * @param m a {@link ModelObject} (can be <code>null</code>)
	 */
	protected void setExtendedDataFrom(ModelObject m) {
		extendedData.clear();
		if (m != null)
			extendedData.putAll(m.extendedData);
	}
	
	/**
	 * Classes implementing <strong>Identifier</strong> are <i>abstract object
	 * identifiers</i> &mdash; that is, they refer to {@link ModelObject}s in
	 * a less specific way than a Java object reference does.
	 * @author alec
	 */
	public interface Identifier extends IObjectIdentifier {
		@Override
		ModelObject lookup(PropertyScratchpad context, Resolver r);
	}
	
	public static abstract class ModelObjectChangeDescriptor
			implements IChangeDescriptor {
	}
	
	public Identifier getIdentifier() {
		throw new RuntimeException(
				"BUG: can't get an identifier for class " +
				getClass().getCanonicalName());
	}
	
	public Identifier getIdentifier(PropertyScratchpad context) {
		throw new RuntimeException(
				"BUG: can't get an in-context identifier for class " +
				getClass().getCanonicalName());
	}
	
	public static <T> T require(Object o, Class<? extends T> klass) {
		return (klass.isInstance(o) ? klass.cast(o) : null);
	}
	
	public static boolean safeClassCmp(Object o1, Object o2) {
		return safeEquals(
				o1 != null ? o1.getClass() : null,
				o2 != null ? o2.getClass() : null);
	}
	
	public static boolean safeEquals(Object o1, Object o2) {
		return (o1 != null ? o1.equals(o2) : o2 == null);
	}
	
	public static int compositeHashCode(Object... objs) {
		if (objs != null && objs.length > 0) {
			int total = 123;
			for (Object i : objs)
				total += (i != null ? i.hashCode() : 0);
			return total;
		} else return 0;
	}
}
