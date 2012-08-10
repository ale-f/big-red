package org.bigraph.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeGroup;
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
	
	public interface ExtendedDataNormaliser {
		Object normalise(ChangeExtendedData c, Object rawValue);
	}
	
	/**
	 * The <strong>ChangeExtendedData</strong> class represents a change to one
	 * of a {@link ModelObject}'s extended data properties.
	 * @author alec
	 * @see ModelObject#setExtendedData(String, Object)
	 */
	public class ChangeExtendedData extends ModelObjectChange {
		public final String key;
		public final Object newValue;
		public final ExtendedDataValidator immediateValidator, finalValidator;
		public final ExtendedDataNormaliser normaliser;
		
		protected ChangeExtendedData(String key, Object newValue,
				ExtendedDataValidator immediateValidator,
				ExtendedDataValidator finalValidator,
				ExtendedDataNormaliser normaliser) {
			this.key = key;
			this.newValue = newValue;
			this.immediateValidator = immediateValidator;
			this.finalValidator = finalValidator;
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
					key, oldValue, immediateValidator, finalValidator,
					normaliser);
		}
		
		@Override
		public String toString() {
			return "Change(set extended data field " + key + " of " +
					getCreator() + " to " + newValue + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			context.setProperty(getCreator(), key, (normaliser == null ?
					newValue : normaliser.normalise(this, newValue)));
		}
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
		return null;
	}
	
	protected Object getProperty(PropertyScratchpad context, String name) {
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
		return changeExtendedData(key, newValue, validator, null);
	}
	
	public Change changeExtendedData(String key, Object newValue,
			ExtendedDataValidator immediateValidator,
			ExtendedDataValidator finalValidator) {
		return changeExtendedData(
				key, newValue, immediateValidator, finalValidator, null);
	}
	
	public Change changeExtendedData(String key, Object newValue,
			ExtendedDataValidator immediateValidator,
			ExtendedDataValidator finalValidator,
			ExtendedDataNormaliser normaliser) {
		return new ChangeExtendedData(
				key, newValue, immediateValidator, finalValidator, normaliser);
	}
	
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
	
	private Map<String, Object> extendedData = new HashMap<String, Object>();
	
	/**
	 * Retrieves one of this object's extended data properties.
	 * @param key the property name
	 * @return an {@link Object}, or <code>null</code> if the named property
	 * has no content
	 */
	public Object getExtendedData(String key) {
		return extendedData.get(key);
	}
	
	public Object getExtendedData(PropertyScratchpad context, String key) {
		if (context == null || !context.hasProperty(this, key)) {
			return getExtendedData(key);
		} else return context.getProperty(this, key);
	}
	
	/**
	 * Sets one of this object's extended data properties, broadcasting a
	 * property change event in the process.
	 * @param key the property name
	 * @param value an {@link Object} to assign to the named property, or
	 * <code>null</code> to remove an existing assignment
	 */
	public void setExtendedData(String key, Object value) {
		if (key == null)
			return;
		Object oldValue = (value != null ?
				extendedData.put(key, value) : extendedData.remove(key));
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
	
	protected boolean doChange(IChange c_) {
		c_.beforeApply();
		if (c_ instanceof ChangeGroup) {
			for (IChange c : (ChangeGroup)c_)
				if (!doChange(c))
					throw new Error("Couldn't apply " + c +
							" (how did it pass validation?)");
		} else if (c_ instanceof ChangeExtendedData) {
			ChangeExtendedData c = (ChangeExtendedData)c_;
			c.getCreator().setExtendedData(c.key, (c.normaliser == null ?
					c.newValue : c.normaliser.normalise(c, c.newValue)));
		} else return false;
		return true;
	}
	
	/**
	 * Classes extending <strong>Identifier</strong> are <i>abstract object
	 * identifiers</i> &mdash; that is, they refer to {@link ModelObject}s by
	 * name rather than by a Java object reference.
	 * @author alec
	 */
	public static abstract class Identifier {
		/**
		 * Classes implementing <strong>Resolver</strong> can resolve {@link
		 * Object}-{@link String} pairs into {@link Object}s.
		 * @author alec
		 */
		public interface Resolver {
			Object lookup(
					PropertyScratchpad context, Object type, String name);
		}
		
		private final String name;
		
		public Identifier(String name) {
			this.name = name;
		}
		
		/**
		 * Returns this {@link Identifier}'s name.
		 * @return a name; can be <code>null</code>
		 */
		public String getName() {
			return name;
		}
		
		protected static <T> T require(Object o, Class<? extends T> klass) {
			return (klass.isInstance(o) ? klass.cast(o) : null);
		}
		
		@Override
		public boolean equals(Object obj_) {
			return safeClassCmp(this, obj_) &&
					safeEquals(getName(), ((Identifier)obj_).getName());
		}
		
		@Override
		public int hashCode() {
			return compositeHashCode(getClass(), getName());
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
		public abstract ModelObject lookup(
				PropertyScratchpad context, Resolver r);
		
		/**
		 * Returns a copy of this {@link Identifier} with a different name.
		 * (Other identifying properties, if there are any, will not be
		 * changed.)
		 * @param name a new name
		 * @return a renamed copy of this {@link Identifier}
		 */
		public abstract Identifier getRenamed(String name);
	}
	
	public static class ChangeExtendedDataDescriptor
			implements IChangeDescriptor {
		private final Identifier target;

		private final String key;
		private final Object oldValue, newValue;
		private final ExtendedDataValidator immediateValidator, finalValidator;
		private final ExtendedDataNormaliser normaliser;

		public ChangeExtendedDataDescriptor(Identifier target, String key,
				Object oldValue, Object newValue,
				ExtendedDataValidator immediateValidator,
				ExtendedDataValidator finalValidator,
				ExtendedDataNormaliser normaliser) {
			this.target = target;
			this.key = key;
			this.newValue = newValue;
			this.oldValue = oldValue;
			this.immediateValidator = immediateValidator;
			this.finalValidator = finalValidator;
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
		
		public ExtendedDataValidator getImmediateValidator() {
			return immediateValidator;
		}
		
		public ExtendedDataValidator getFinalValidator() {
			return finalValidator;
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
						safeEquals(getNewValue(), obj.getNewValue());
			} else return false;
		}

		@Override
		public int hashCode() {
			return compositeHashCode(
					ChangeExtendedDataDescriptor.class, target, key, newValue);
		}

		@Override
		public Change createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			ModelObject m = target.lookup(context, r);
			if (m == null)
				throw new ChangeCreationException(this,
						"" + target + " didn't resolve to a ModelObject");
			return m.changeExtendedData(key,
					newValue, immediateValidator, finalValidator, normaliser);
		}

		@Override
		public String toString() {
			return "ChangeDescriptor(set extended data field " + key + " of " +
					target + " to " + newValue + ")"; 
		}
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
