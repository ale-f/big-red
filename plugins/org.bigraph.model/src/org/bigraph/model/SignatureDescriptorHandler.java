package org.bigraph.model;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.Signature.ChangeAddControlDescriptor;
import org.bigraph.model.Signature.ChangeAddSignatureDescriptor;
import org.bigraph.model.Signature.ChangeRemoveSignatureDescriptor;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

final class SignatureDescriptorHandler
		extends DescriptorHandlerUtilities.DescriptorHandlerImpl {
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final PropertyScratchpad scratch = context.getScratch();
		final Resolver resolver = context.getResolver();
		if (change instanceof ChangeAddControlDescriptor) {
			ChangeAddControlDescriptor cd = (ChangeAddControlDescriptor)change;
			Signature s = cd.getTarget().lookup(scratch, resolver);
			
			if (s == null)
				throw new ChangeCreationException(cd,
						"" + cd.getTarget() + ": lookup failed");
			
			NamedModelObjectDescriptorHandler.checkName(scratch, cd,
					cd.getControl(), s.getNamespace(),
					cd.getControl().getName()); 
		} else if (change instanceof ChangeAddSignatureDescriptor) {
			ChangeAddSignatureDescriptor cd =
					(ChangeAddSignatureDescriptor)change;
			Signature s = cd.getTarget().lookup(scratch, resolver);
			
			if (s == null)
				throw new ChangeCreationException(cd,
						"" + cd.getTarget() + ": lookup failed");
			
			Signature ch = cd.getSignature();
			if (ch == null)
				throw new ChangeCreationException(cd,
						"Can't insert a null signature");
			
			DescriptorHandlerUtilities.checkAddBounds(cd,
					s.getSignatures(scratch), cd.getPosition());
		} else if (change instanceof ChangeRemoveSignatureDescriptor) {
			ChangeRemoveSignatureDescriptor cd =
					(ChangeRemoveSignatureDescriptor)change;
			Signature s = cd.getTarget().lookup(scratch, resolver);
			
			if (s == null)
				throw new ChangeCreationException(cd,
						"" + cd.getTarget() + ": lookup failed");
			
			DescriptorHandlerUtilities.checkRemove(cd,
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
			s.addControl(cd.getPosition(), c);
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
