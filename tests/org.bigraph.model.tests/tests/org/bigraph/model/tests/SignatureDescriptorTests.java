package org.bigraph.model.tests;

import org.bigraph.model.Control;
import org.bigraph.model.Signature;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.junit.Test;

public class SignatureDescriptorTests extends DescriptorTestRunner {
	@Test
	public void addChildSignature() throws ChangeCreationException {
		Signature
			s1 = new Signature(),
			s2 = new Signature();
		run(s1,
				new Signature.ChangeAddSignatureDescriptor(
						new Signature.Identifier(), 0, s2));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void removeAbsentChildSignature() throws ChangeCreationException {
		Signature
			s1 = new Signature(),
			s2 = new Signature();
		run(s1,
				new Signature.ChangeRemoveSignatureDescriptor(
						new Signature.Identifier(), 0, s2));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void removeWrongChildSignature() throws ChangeCreationException {
		Signature
			s1 = new Signature(),
			s2 = new Signature(),
			s3 = new Signature();
		run(s1,
				new Signature.ChangeAddSignatureDescriptor(
						new Signature.Identifier(), 0, s2),
				new Signature.ChangeRemoveSignatureDescriptor(
						new Signature.Identifier(), 0, s3));
	}
	
	@Test
	public void addControl() throws ChangeCreationException {
		Signature
			s1 = new Signature();
		Control.Identifier cI = new Control.Identifier("a");
		run(s1,
				new Signature.ChangeAddControlDescriptor(
						new Signature.Identifier(), cI));
	}
}
