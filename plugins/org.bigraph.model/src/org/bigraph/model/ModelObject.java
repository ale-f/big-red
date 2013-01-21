package org.bigraph.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
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
	/**
	 * All {@link Change}s which operate on {@link ModelObject}s inherit from
	 * <strong>ModelObjectChange</strong>.
	 * @author alec
	 * @see #getCreator()
	 */
	public abstract class ModelObjectChange extends Change {
		/**
		 * Gets the {@link ModelObject} which created this {@link
		 * ModelObjectChange}.
		 * @return
		 */
		public ModelObject getCreator() {
			return ModelObject.this;
		}
	}
	
	public interface ExtendedDataValidator {
		void validate(ChangeExtendedData c, PropertyScratchpad context)
				throws ChangeRejectedException;
	}
	
	public interface FinalExtendedDataValidator extends ExtendedDataValidator {
		void finalValidate(ChangeExtendedData c, PropertyScratchpad context)
				throws ChangeRejectedException;
	}
	
	public interface ExtendedDataNormaliser {
		Object normalise(ChangeExtendedData c, Object rawValue);
	}
	
	/**
	 * The <strong>ChangeExtendedData</strong> class represents a change to one
	 * of a {@link ModelObject}'s extended data properties.
	 * @author alec
	 * @see ModelObject#setExtendedData(String, Object)
	 */
	public final class ChangeExtendedData extends ModelObjectChange {
		public final String key;
		public final Object newValue;
		public final ExtendedDataValidator validator;
		public final ExtendedDataNormaliser normaliser;
		
		protected ChangeExtendedData(String key, Object newValue,
				ExtendedDataValidator validator,
				ExtendedDataNormaliser normaliser) {
			this.key = key;
			this.newValue = newValue;
			this.validator = validator;
			this.normaliser = normaliser;
		}
		
		private Object oldValue;
		
		@Override
		public void beforeApply() {
			oldValue = getCreator().getExtendedData(key);
		}
		
		@Override
		public Change inverse() {
			return new ChangeExtendedData(
					key, oldValue, validator, normaliser);
		}
		
		@Override
		public String toString() {
			return "Change(set extended data field " + key + " of " +
					getCreator() + " to " + newValue + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			getCreator().setExtendedData(context, key, (normaliser == null ?
					newValue : normaliser.normalise(this, newValue)));
		}
	}
	
	static {
		ExecutorManager.getInstance().addParticipant(new ModelObjectHandler());
	}
	
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
	
	public IChange changeExtendedData(String key, Object newValue) {
		return changeExtendedData(key, newValue, null);
	}
	
	public IChange changeExtendedData(
			String key, Object newValue, ExtendedDataValidator validator) {
		return changeExtendedData(key, newValue, validator, null);
	}
	
	public IChange changeExtendedData(String key, Object newValue,
			ExtendedDataValidator validator,
			ExtendedDataNormaliser normaliser) {
		return new ChangeExtendedData(
				key, newValue, validator, normaliser);
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
	public interface Identifier {
		/**
		 * Classes implementing <strong>Resolver</strong> can resolve {@link
		 * Identifier}s into {@link Object}s.
		 * @author alec
		 */
		interface Resolver {
			Object lookup(PropertyScratchpad context, Identifier identifier);
		}
		
		/**
		 * Retrieves the {@link ModelObject} corresponding to this {@link
		 * Identifier} from the given {@link Resolver}.
		 * @param context a {@link PropertyScratchpad} containing changes to
		 * the {@link Resolver}'s state; can be <code>null</code>
		 * @param r a {@link Resolver}
		 * @return a {@link ModelObject}, or <code>null</code> if the lookup
		 * failed
		 */
		ModelObject lookup(PropertyScratchpad context, Resolver r);
	}
	
	public static abstract class ModelObjectChangeDescriptor
			implements IChangeDescriptor {
		@Override
		public void simulate(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			context.executeChange(createChange(context, r));
		}
	}
	
	public static final class ChangeExtendedDataDescriptor
			extends ModelObjectChangeDescriptor {
		private final Identifier target;

		private final String key;
		private final Object oldValue, newValue;
		private final ExtendedDataValidator validator;
		private final ExtendedDataNormaliser normaliser;

		public ChangeExtendedDataDescriptor(Identifier target, String key,
				Object oldValue, Object newValue,
				ExtendedDataValidator validator,
				ExtendedDataNormaliser normaliser) {
			this.target = target;
			this.key = key;
			this.newValue = newValue;
			this.oldValue = oldValue;
			this.validator = validator;
			this.normaliser = normaliser;
		}
		
		public Identifier getTarget() {
			return target;
		}

		public String getKey() {
			return key;
		}

		public Object getNewValue() {
			return newValue;
		}

		public Object getOldValue() {
			return oldValue;
		}
		
		public ExtendedDataValidator getValidator() {
			return validator;
		}
		
		public ExtendedDataNormaliser getNormaliser() {
			return normaliser;
		}
		
		@Override
		public boolean equals(Object obj_) {
			if (safeClassCmp(this, obj_)) {
				ChangeExtendedDataDescriptor obj =
						(ChangeExtendedDataDescriptor)obj_;
				return
						safeEquals(getTarget(), obj.getTarget()) &&
						safeEquals(getKey(), obj.getKey()) &&
						safeEquals(getOldValue(), obj.getOldValue()) &&
						safeEquals(getNewValue(), obj.getNewValue());
			} else return false;
		}

		@Override
		public int hashCode() {
			return compositeHashCode(
					ChangeExtendedDataDescriptor.class,
					target, key, oldValue, newValue);
		}

		@Override
		public IChange createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			ModelObject m = target.lookup(context, r);
			if (m == null)
				throw new ChangeCreationException(this,
						"" + target + " didn't resolve to a ModelObject");
			return m.changeExtendedData(key, newValue, validator, normaliser);
		}

		@Override
		public ChangeExtendedDataDescriptor inverse() {
			return new ChangeExtendedDataDescriptor(
					getTarget(), getKey(), getNewValue(), getOldValue(),
					getValidator(), getNormaliser());
		}
		
		@Override
		public String toString() {
			return "ChangeDescriptor(set extended data field " + key + " of " +
					target + " to " + newValue + ")"; 
		}
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
