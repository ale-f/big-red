package org.bigraph.model;

import org.bigraph.model.Control.ChangeAddPortSpecDescriptor;
import org.bigraph.model.Control.ChangeKindDescriptor;
import org.bigraph.model.Control.ChangeRemovePortSpecDescriptor;
import org.bigraph.model.HandlerUtilities.DescriptorHandlerImpl;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

final class ControlDescriptorHandler extends DescriptorHandlerImpl {
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final PropertyScratchpad scratch = context.getScratch();
		final Resolver resolver = context.getResolver();
		if (change instanceof ChangeKindDescriptor) {
			ChangeKindDescriptor cd = (ChangeKindDescriptor)change;
			tryLookup(cd, cd.getTarget(), scratch, resolver, Control.class);
		} else if (change instanceof ChangeAddPortSpecDescriptor) {
			ChangeAddPortSpecDescriptor cd =
					(ChangeAddPortSpecDescriptor)change;
			Control c = tryLookup(cd, cd.getSpec().getControl(),
					scratch, resolver, Control.class);
			
			HandlerUtilities.checkName(scratch, cd,
					cd.getSpec(), c.getNamespace(), cd.getSpec().getName());
		} else if (change instanceof ChangeRemovePortSpecDescriptor) {
			ChangeRemovePortSpecDescriptor cd =
					(ChangeRemovePortSpecDescriptor)change;
			PortSpec p = tryLookup(cd,
					cd.getSpec(), scratch, resolver, PortSpec.class);
			
			if (p.getExtendedDataMap(scratch).size() != 0)
				throw new ChangeCreationException(cd,
						"" + cd.getSpec() + " still has extended data " +
						"that must be removed");
		} else return false;
		return true;
	}
	
	@Override
	public boolean executeChange(Resolver resolver, IChangeDescriptor change) {
		if (change instanceof ChangeKindDescriptor) {
			ChangeKindDescriptor cd = (ChangeKindDescriptor)change;
			cd.getTarget().lookup(null, resolver).setKind(cd.getNewValue());
		} else if (change instanceof ChangeAddPortSpecDescriptor) {
			ChangeAddPortSpecDescriptor cd =
					(ChangeAddPortSpecDescriptor)change;
			Control c = cd.getSpec().getControl().lookup(null, resolver);
			PortSpec p = new PortSpec();
			c.addPort(p);
			p.setName(c.getNamespace().put(cd.getSpec().getName(), p));
		} else if (change instanceof ChangeRemovePortSpecDescriptor) {
			ChangeRemovePortSpecDescriptor cd =
					(ChangeRemovePortSpecDescriptor)change;
			PortSpec p = cd.getSpec().lookup(null, resolver);
			Control c = p.getControl();
			c.removePort(p);
			c.getNamespace().remove(p.getName());
		} else return false;
		return true;
	}
}
