package org.bigraph.model.tests;

import org.junit.Test;
import static org.junit.Assert.*;

import org.bigraph.model.Control;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Signature;
import org.bigraph.model.changes.descriptors.ChangeCreationException;

public class SignatureTests {
	@Test
	public void addSimpleControl() throws ChangeCreationException {
		DescriptorTestRunner.run(new Signature(),
				new Signature.ChangeAddControlDescriptor(
						new Signature.Identifier(),
						new Control.Identifier("c0")));
	}
	
	@Test
	public void addComplexControl() throws ChangeCreationException {
		DescriptorTestRunner.run(new Signature(),
				new Signature.ChangeAddControlDescriptor(
						new Signature.Identifier(),
						new Control.Identifier("c0")),
				new Control.ChangeAddPortSpecDescriptor(
						new PortSpec.Identifier("p0",
								new Control.Identifier("c0"))),
				new Control.ChangeKindDescriptor(
						new Control.Identifier("c0"),
						Kind.ACTIVE, Kind.ATOMIC));
	}
	
	@Test
	public void removeControl() throws ChangeCreationException {
		Signature s = new Signature();
		try {
			DescriptorTestRunner.run(s,
					new Signature.ChangeAddControlDescriptor(
							new Signature.Identifier(),
							new Control.Identifier("c0")));
		} catch (ChangeCreationException e) {
			fail(e.getRationale());
		}
		DescriptorTestRunner.run(s,
				new Signature.ChangeRemoveControlDescriptor(
						new Signature.Identifier(),
						new Control.Identifier("c0")));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void removeAbsentControl() throws ChangeCreationException {
		DescriptorTestRunner.run(new Signature(),
				new Signature.ChangeRemoveControlDescriptor(
						new Signature.Identifier(),
						new Control.Identifier("c0")));
	}

	@Test(expected = ChangeCreationException.class)
	public void addDuplicateName() throws ChangeCreationException {
		Signature.ChangeAddControlDescriptor cd =
				new Signature.ChangeAddControlDescriptor(
						new Signature.Identifier(),
						new Control.Identifier("c0"));
		DescriptorTestRunner.run(new Signature(), cd, cd);
	}
	
	@Test
	public void addNestedSignature() throws ChangeCreationException {
		DescriptorTestRunner.run(new Signature(),
				new Signature.ChangeAddSignatureDescriptor(
						new Signature.Identifier(), -1, new Signature()));
	}
}
