package org.bigraph.model;

import org.bigraph.model.Signature.ChangeAddControlDescriptor;
import org.bigraph.model.Signature.ChangeAddSignatureDescriptor;
import org.bigraph.model.Signature.ChangeRemoveControlDescriptor;
import org.bigraph.model.Signature.ChangeRemoveSignatureDescriptor;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

final class SignatureDescriptorHandler
		extends HandlerUtilities.DescriptorHandlerImpl {
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final PropertyScratchpad scratch = context.getScratch();
		final Resolver resolver = context.getResolver();
		if (change instanceof ChangeAddControlDescriptor) {
			ChangeAddControlDescriptor cd = (ChangeAddControlDescriptor)change;
			Signature s = tryLookup(cd,
					cd.getTarget(), scratch, resolver, Signature.class);
			
			HandlerUtilities.checkName(scratch, cd,
					cd.getControl(), s.getNamespace(),
					cd.getControl().getName()); 
		} else if (change instanceof ChangeRemoveControlDescriptor) {
			ChangeRemoveControlDescriptor cd =
					(ChangeRemoveControlDescriptor)change;
			tryLookup(cd, cd.getTarget(), scratch, resolver, Signature.class);
			Control c = tryLookup(
					cd, cd.getControl(), scratch, resolver, Control.class);
			
			if (c.getPorts(scratch).size() != 0)
				throw new ChangeCreationException(cd,
						"" + cd.getControl() + " still has ports " +
						"that must be removed first");
			
			if (c.getExtendedDataMap(scratch).size() != 0)
				throw new ChangeCreationException(cd,
						"" + cd.getControl() + " still has extended data " +
						"that must be removed first");
		} else if (change instanceof ChangeAddSignatureDescriptor) {
			ChangeAddSignatureDescriptor cd =
					(ChangeAddSignatureDescriptor)change;
			Signature s = tryLookup(cd,
					cd.getTarget(), scratch, resolver, Signature.class);
			
			Signature ch = cd.getSignature();
			if (ch == null)
				throw new ChangeCreationException(cd,
						"Can't insert a null signature");
			
			HandlerUtilities.checkAddBounds(cd,
					s.getSignatures(scratch), cd.getPosition());
		} else if (change instanceof ChangeRemoveSignatureDescriptor) {
			ChangeRemoveSignatureDescriptor cd =
					(ChangeRemoveSignatureDescriptor)change;
			Signature s = tryLookup(cd,
					cd.getTarget(), scratch, resolver, Signature.class);
			
			HandlerUtilities.checkRemove(cd,
					s.getSignatures(scratch),
					cd.getSignature(), cd.getPosition());
		} else return false;
		return true;
	}

	@Override
	public boolean executeChange(Resolver resolver, IChangeDescriptor change) {
		if (change instanceof ChangeAddControlDescriptor) {
			ChangeAddControlDescriptor cd = (ChangeAddControlDescriptor)change;
			Signature s = cd.getTarget().lookup(null, resolver);
			Control c = new Control();
			
			c.setName(s.getNamespace().put(cd.getControl().getName(), c));
			s.addControl(c);
		} else if (change instanceof ChangeRemoveControlDescriptor) {
			ChangeRemoveControlDescriptor cd =
					(ChangeRemoveControlDescriptor)change;
			Signature s = cd.getTarget().lookup(null, resolver);
			Control c = cd.getControl().lookup(null, resolver);
			
			s.removeControl(c);
			s.getNamespace().remove(c.getName());
		} else if (change instanceof ChangeAddSignatureDescriptor) {
			ChangeAddSignatureDescriptor cd =
					(ChangeAddSignatureDescriptor)change;
			cd.getTarget().lookup(null, resolver).addSignature(
					cd.getPosition(), cd.getSignature());
		} else if (change instanceof ChangeRemoveSignatureDescriptor) {
			ChangeRemoveSignatureDescriptor cd =
					(ChangeRemoveSignatureDescriptor)change;
			cd.getTarget().lookup(null, resolver).removeSignature(
					cd.getSignature());
		} else return false;
		return true;
	}
}
