package org.bigraph.model;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.NamedModelObject.Identifier;
import org.bigraph.model.NamedModelObject.ChangeNameDescriptor;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.names.Namespace;

final class NamedModelObjectDescriptorHandler
		extends HandlerUtilities.DescriptorHandlerImpl {
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final PropertyScratchpad scratch = context.getScratch();
		final Resolver resolver = context.getResolver();
		if (change instanceof ChangeNameDescriptor) {
			ChangeNameDescriptor cd = (ChangeNameDescriptor)change;
			
			NamedModelObject object = tryLookup(cd,
					cd.getTarget(), scratch, resolver, NamedModelObject.class);
			
			checkName(scratch, cd, cd.getTarget(),
					object.getGoverningNamespace(scratch), cd.getNewName());
		} else return false;
		return true;
	}

	@Override
	public boolean executeChange(Resolver resolver, IChangeDescriptor change) {
		if (change instanceof ChangeNameDescriptor) {
			ChangeNameDescriptor cd = (ChangeNameDescriptor)change;
			cd.getTarget().lookup(null, resolver).applyRename(cd.getNewName());
		} else return false;
		return true;
	}
	
	static void checkName(
			PropertyScratchpad context, IChangeDescriptor c, Identifier object,
			Namespace<? extends NamedModelObject> ns, String newName)
			throws ChangeCreationException {
		String rationale = HandlerUtilities.checkNameCore(context, object, ns, newName);
		if (rationale != null)
			throw new ChangeCreationException(c, rationale);
	}
}
