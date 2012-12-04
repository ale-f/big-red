package org.bigraph.model;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.NamedModelObject.ChangeNameDescriptor;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.experimental.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.experimental.IDescriptorStepValidator;
import org.bigraph.model.names.Namespace;
import org.bigraph.model.names.policies.INamePolicy;

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
			
			String cdt = cd.getNewName();
			
			Namespace<? extends NamedModelObject> ns =
					object.getGoverningNamespace(scratch);
			
			if (cdt == null || cdt.length() == 0)
				throw new ChangeCreationException(cd, "Names cannot be empty");
			if (ns != null) {
				INamePolicy p = ns.getPolicy();
				String mcdt = (p != null ? p.normalise(cdt) : cdt);
				if (mcdt == null)
					throw new ChangeCreationException(cd,
							"\"" + cdt + "\" is not a valid name for " +
							object);
				NamedModelObject current = ns.get(scratch, mcdt);
				if (current != null && current != object)
					throw new ChangeCreationException(cd,
							"Names must be unique");
			}
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
}
