package org.bigraph.model.assistants;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.IDescriptorStepValidator;
import org.bigraph.model.process.IParticipantHost;

public abstract class ExtendedDataUtilities {
	public static abstract class ChangeExtendedDataDescriptor<
			T extends ModelObject.Identifier, V>
			extends ModelObject.ModelObjectChangeDescriptor {
		public static abstract class Handler
				implements IDescriptorStepExecutor, IDescriptorStepValidator {
			@Override
			public void setHost(IParticipantHost host) {
				/* do nothing */
			}
		}
		
		private final String key;
		private final T target;
		private final V oldValue, newValue;
		
		public ChangeExtendedDataDescriptor(
				String key, T target, V oldValue, V newValue) {
			this.key = key;
			this.target = target;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
		
		protected String getKey() {
			return key;
		}
		
		public T getTarget() {
			return target;
		}
		
		public V getOldValue() {
			return oldValue;
		}
		
		public V getNewValue() {
			return newValue;
		}
		
		protected V getNormalisedNewValue() {
			return getNewValue();
		}
		
		@Override
		public IChange createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			return new BoundDescriptor(r, this);
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			ModelObject mo = getTarget().lookup(context, r);
			mo.setExtendedData(context, getKey(), getNormalisedNewValue());
		}
	}

	private ExtendedDataUtilities() {}
	
	public static <T> T getProperty(PropertyScratchpad context, ModelObject o,
			String name, Class<T> klass) {
		if (o != null && name != null) {
			try {
				return klass.cast(o.getExtendedData(context, name));
			} catch (ClassCastException ex) {
				return null;
			}
		} else return null;
	}
	
	public static void setProperty(PropertyScratchpad context, ModelObject o,
			String name, Object value) {
		if (o == null || name == null)
			return;
		o.setExtendedData(context, name, value);
	}
}
