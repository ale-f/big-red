package org.bigraph.model;

import java.util.HashMap;
import java.util.Map;

import org.bigraph.model.assistants.IObjectIdentifier;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

public class Store implements Resolver {
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
			sourceMap.putAll(targetMap);
			targetMap.clear();
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
			return new ToStoreDescriptor(getID(), getEntryID());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			doSimulate(context, getID().lookup(context, r),
					getEntryID().lookup(context, r));
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
		public void simulate(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			doSimulate(context, getEntryID().lookup(context, r),
					getID().lookup(context, r));
		}
	}
	
	private static final class DummyModelObject extends ModelObject {
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
	}
	
	private Long nextID = new Long(0);
	
	private long getNextID() {
		synchronized (nextID) {
			return nextID++;
		}
	}
	
	private Map<Long, ModelObject> entries =
			new HashMap<Long, ModelObject>();
	
	public EntryIdentifier createID() {
		return new EntryIdentifier(getNextID());
	}
	
	public boolean drop(long id) {
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
			return ModelObject.require(
					r.lookup(context, this), ModelObject.class);
		}
	}
	
	@Override
	public Object lookup(
			PropertyScratchpad context, IObjectIdentifier identifier) {
		if (identifier instanceof EntryIdentifier) {
			EntryIdentifier eid = (EntryIdentifier)identifier;
			long id = eid.getID();
			ModelObject result;
			if (!entries.containsKey(id)) {
				entries.put(id, result = new DummyModelObject(eid));
			} else result = entries.get(id);
			return result;
		} else return null;
	}
}
