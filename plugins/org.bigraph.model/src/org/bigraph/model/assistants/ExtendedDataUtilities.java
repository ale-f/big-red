package org.bigraph.model.assistants;

import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
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
		
		protected V getNormalisedNewValue(
				PropertyScratchpad context, Resolver r) {
			return getNewValue();
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			ModelObject mo = getTarget().lookup(context, r);
			mo.setExtendedData(context, getKey(),
					getNormalisedNewValue(context, r));
		}
	}
	
	public static final class SimpleHandler
			extends ChangeExtendedDataDescriptor.Handler {
		private final
				Class<? extends ChangeExtendedDataDescriptor<?, ?>> klass;
		
		public SimpleHandler(
				Class<? extends ChangeExtendedDataDescriptor<?, ?>> klass) {
			this.klass = klass;
		}
		
		@Override
		public boolean tryValidateChange(Process context,
				IChangeDescriptor change) throws ChangeCreationException {
			final PropertyScratchpad scratch = context.getScratch();
			final Resolver resolver = context.getResolver();
			if (klass.isInstance(change)) {
				ChangeExtendedDataDescriptor<?, ?> cd = klass.cast(change);
				ModelObject mo = cd.getTarget().lookup(scratch, resolver);
				if (mo == null)
					throw new ChangeCreationException(cd,
							"" + cd.getTarget() + ": lookup failed");
			} else return false;
			return true;
		}
		
		@Override
		public boolean executeChange(Resolver resolver,
				IChangeDescriptor change) {
			if (klass.isInstance(change)) {
				ChangeExtendedDataDescriptor<?, ?> cd = klass.cast(change);
				ModelObject mo = cd.getTarget().lookup(null, resolver);
				mo.setExtendedData(cd.getKey(),
						cd.getNormalisedNewValue(null, resolver));
			} else return false;
			return true;
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
