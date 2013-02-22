package org.bigraph.model;

import java.util.HashMap;
import java.util.Map;

import org.bigraph.model.assistants.IObjectIdentifier;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

public class Store implements Resolver {
	private static final class Holder {
		private static final Store INSTANCE = new Store();
	}
	
	public static final Store getInstance() {
		return Holder.INSTANCE;
	}
	
	protected static abstract class StoreChangeDescriptor
			implements IChangeDescriptor {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(
					new StoreDescriptorHandler());
		}
		
		private final ModelObject.Identifier id;
		private final EntryIdentifier eid;
		
		protected StoreChangeDescriptor(
				ModelObject.Identifier id, EntryIdentifier eid) {
			this.id = id;
			this.eid = eid;
		}
		
		public ModelObject.Identifier getID() {
			return id;
		}
		
		public EntryIdentifier getEntryID() {
			return eid;
		}
		
		protected static void doSimulate(PropertyScratchpad context,
				ModelObject source, ModelObject target) {
			Map<String, Object>
				sourceMap = source.getModifiableExtendedDataMap(context),
				targetMap = target.getModifiableExtendedDataMap(context);
			targetMap.putAll(sourceMap);
			sourceMap.clear();
		}
	}
	
	public static final class ToStoreDescriptor
			extends StoreChangeDescriptor {
		public ToStoreDescriptor(
				ModelObject.Identifier id, EntryIdentifier eid) {
			super(id, eid);
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new FromStoreDescriptor(getID(), getEntryID());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			doSimulate(context, getID().lookup(context, r),
					getEntryID().lookup(context, r));
		}
		
		@Override
		public String toString() {
			return "ChangeDescriptor(store extended data from " + getID() +
					" into " + getEntryID() + ")";
		}
	}
	
	public static final class FromStoreDescriptor
			extends StoreChangeDescriptor {
		public FromStoreDescriptor(
				ModelObject.Identifier id, EntryIdentifier eid) {
			super(id, eid);
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ToStoreDescriptor(getID(), getEntryID());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			doSimulate(context, getEntryID().lookup(context, r),
					getID().lookup(context, r));
		}
		
		@Override
		public String toString() {
			return "ChangeDescriptor(load extended data from " + getEntryID() +
					" into " + getID() + ")";
		}
	}
	
	final class DummyModelObject extends ModelObject {
		final static String PROPERTY_STORE = ".dmo.internal.store";
		
		private final Identifier id;
		
		public DummyModelObject(Identifier id) {
			this.id = id;
		}
		
		@Override
		public Identifier getIdentifier() {
			return id;
		}
		
		@Override
		public Identifier getIdentifier(PropertyScratchpad context) {
			return id;
		}
		
		@Override
		protected void firePropertyChange(String propertyName, Object oldValue,
				Object newValue) {
			/* do nothing */
		}
		
		@Override
		protected Object getProperty(String name) {
			if (PROPERTY_STORE.equals(name)) {
				return Store.this;
			} else return super.getProperty(name);
		}
	}
	
	private Long nextID = new Long(0);
	
	private long getNextID() {
		synchronized (nextID) {
			return nextID++;
		}
	}
	
	private Map<EntryIdentifier, DummyModelObject> entries =
			new HashMap<EntryIdentifier, DummyModelObject>();
	
	public EntryIdentifier createID() {
		return new EntryIdentifier(getNextID());
	}
	
	public boolean drop(EntryIdentifier id) {
		return (entries.remove(id) != null);
	}
	
	public static class EntryIdentifier implements ModelObject.Identifier {
		private final long id;
		
		public EntryIdentifier(long id) {
			this.id = id;
		}
		
		public long getID() {
			return id;
		}
		
		@Override
		public boolean equals(Object obj) {
			return (ModelObject.safeClassCmp(this, obj) &&
					((EntryIdentifier)obj).getID() == getID());
		}
		
		@Override
		public int hashCode() {
			return ModelObject.compositeHashCode(getClass(), getID());
		}
		
		@Override
		public ModelObject lookup(PropertyScratchpad context, Resolver r) {
			Object o = r.lookup(context, this);
			if (o == null)
				o = Store.getInstance().lookup(context, this);
			return ModelObject.require(o, ModelObject.class);
		}
		
		@Override
		public String toString() {
			return "store entry " + getID();
		}
	}
	
	@Override
	public Object lookup(
			PropertyScratchpad context, IObjectIdentifier identifier) {
		if (identifier instanceof EntryIdentifier) {
			EntryIdentifier eid = (EntryIdentifier)identifier;
			DummyModelObject result;
			if (!entries.containsKey(eid)) {
				entries.put(eid, result = new DummyModelObject(eid));
			} else result = entries.get(eid);
			return result;
		} else return null;
	}
}
