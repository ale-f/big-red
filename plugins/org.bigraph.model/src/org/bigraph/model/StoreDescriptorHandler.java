package org.bigraph.model;

import java.util.Map.Entry;

import org.bigraph.model.Store.FromStoreDescriptor;
import org.bigraph.model.Store.StoreChangeDescriptor;
import org.bigraph.model.Store.ToStoreDescriptor;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

final class StoreDescriptorHandler
		extends HandlerUtilities.DescriptorHandlerImpl {
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final PropertyScratchpad scratch = context.getScratch();
		final Resolver resolver = context.getResolver();
		if (change instanceof StoreChangeDescriptor) {
			StoreChangeDescriptor cd = (StoreChangeDescriptor)change;
			
			ModelObject.Identifier sourceI, targetI;
			if (cd instanceof FromStoreDescriptor) {
				sourceI = cd.getEntryID();
				targetI = cd.getID();
			} else if (cd instanceof ToStoreDescriptor) {
				sourceI = cd.getID();
				targetI = cd.getEntryID();
			} else return false;
			
			tryLookup(cd, sourceI, scratch, resolver, ModelObject.class);
			ModelObject
				target = tryLookup(cd,
						targetI, scratch, resolver, ModelObject.class);
			
			if (!target.getExtendedDataMap(scratch).isEmpty())
				throw new ChangeCreationException(cd,
						"" + targetI + " mustn't have any extended data");
		} else return false;
		return true;
	}

	@Override
	public boolean executeChange(Resolver resolver, IChangeDescriptor change) {
		if (change instanceof StoreChangeDescriptor) {
			StoreChangeDescriptor cd = (StoreChangeDescriptor)change;
			
			ModelObject
				source = (cd instanceof FromStoreDescriptor ?
						cd.getEntryID() : cd.getID()).lookup(null, resolver),
				target = (cd instanceof FromStoreDescriptor ?
						cd.getID() : cd.getEntryID()).lookup(null, resolver);

			for (Entry<String, Object> i :
					source.getExtendedDataMap().entrySet())
				target.setExtendedData(i.getKey(), i.getValue());
			source.getExtendedDataMap().clear();
			
			if (cd instanceof FromStoreDescriptor)
				((Store)source.getProperty(
						Store.DummyModelObject.PROPERTY_STORE)).
								drop(cd.getEntryID());
		} else return false;
		return true;
	}
}
