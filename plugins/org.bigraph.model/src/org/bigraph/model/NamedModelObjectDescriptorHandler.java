package org.bigraph.model;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.NamedModelObject.Identifier;
import org.bigraph.model.NamedModelObject.ChangeNameDescriptor;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.IDescriptorStepValidator;
import org.bigraph.model.names.Namespace;

import static org.bigraph.model.NamedModelObjectHandler.checkNameCore;

final class NamedModelObjectDescriptorHandler
		implements IDescriptorStepExecutor, IDescriptorStepValidator {
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final PropertyScratchpad scratch = context.getScratch();
		final Resolver resolver = context.getResolver();
		if (change instanceof ChangeNameDescriptor) {
			ChangeNameDescriptor cd = (ChangeNameDescriptor)change;
			
			NamedModelObject object = cd.getTarget().lookup(scratch, resolver);
			if (object == null)
				throw new ChangeCreationException(cd,
						"" + cd.getTarget() + ": lookup failed");
			
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
		String rationale = checkNameCore(context, object, ns, newName);
		if (rationale != null)
			throw new ChangeCreationException(c, rationale);
	}
}
