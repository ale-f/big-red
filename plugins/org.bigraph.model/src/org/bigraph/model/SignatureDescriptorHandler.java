package org.bigraph.model;

import java.util.List;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.Signature.ChangeAddControlDescriptor;
import org.bigraph.model.Signature.ChangeAddSignatureDescriptor;
import org.bigraph.model.Signature.ChangeRemoveSignatureDescriptor;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.experimental.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.experimental.IDescriptorStepValidator;

final class SignatureDescriptorHandler implements IDescriptorStepExecutor,
		IDescriptorStepValidator {
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
			
			/* XXX: can "new Control()" be improved upon? */
			NamedModelObjectDescriptorHandler.checkName(scratch, cd,
					new Control(), s.getNamespace(),
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
			
			int position = cd.getPosition();
			if (position < 0 ||
					position > s.getSignatures(scratch).size())
				throw new ChangeCreationException(cd,
						"" + position + " is not a valid position");
		} else if (change instanceof ChangeRemoveSignatureDescriptor) {
			ChangeRemoveSignatureDescriptor cd =
					(ChangeRemoveSignatureDescriptor)change;
			Signature s = cd.getTarget().lookup(scratch, resolver);
			
			if (s == null)
				throw new ChangeCreationException(cd,
						"" + cd.getTarget() + ": lookup failed");
			
			List<? extends Signature> sigs = s.getSignatures(scratch);
			int position = cd.getPosition();
			if (position < 0 || position >= sigs.size())
				throw new ChangeCreationException(cd,
						"" + position + " is not a valid position");
			
			if (!sigs.get(position).equals(cd.getSignature()))
				throw new ChangeCreationException(cd,
						"" + cd.getSignature() +
						" is not at position " + position);
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
