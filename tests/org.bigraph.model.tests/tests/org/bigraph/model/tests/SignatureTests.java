package org.bigraph.model.tests;

import org.junit.Test;
import static org.junit.Assert.*;

import org.bigraph.model.Control;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Signature;
import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.descriptors.BoundDescriptor;

import static org.bigraph.model.tests.BigraphTests.cg;

public class SignatureTests {
	@Test
	public void addSimpleControl() throws ChangeRejectedException {
		Signature s = new Signature();
		ExecutorManager.getInstance().tryApplyChange(s.changeAddControl(new Control(), "c0"));
	}
	
	@Test
	public void addComplexControl() throws ChangeRejectedException {
		Signature s = new Signature();
		Control c0 = new Control();
		ExecutorManager.getInstance().tryApplyChange(cg(
				s.changeAddControl(c0, "c0"),
				new BoundDescriptor(s,
						new Control.ChangeAddPortSpecDescriptor(
								new PortSpec.Identifier("p0",
										new Control.Identifier("c0")))),
				new BoundDescriptor(s,
						new Control.ChangeKindDescriptor(
								new Control.Identifier("c0"),
								Kind.ACTIVE, Kind.ATOMIC))));
	}
	
	@Test
	public void removeControl() throws ChangeRejectedException {
		Signature s = new Signature();
		try {
			ExecutorManager.getInstance().tryApplyChange(s.changeAddControl(new Control(), "c0"));
		} catch (ChangeRejectedException e) {
			fail(e.getRationale());
		}
		ExecutorManager.getInstance().tryApplyChange(s.getControl("c0").changeRemove());
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void removeAbsentControl() throws ChangeRejectedException {
		ExecutorManager.getInstance().tryApplyChange(
				new Control().changeRemove());
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void addDuplicateName() throws ChangeRejectedException {
		Signature s = new Signature();
		Control
			c0 = new Control(),
			c1 = new Control();
		ExecutorManager.getInstance().tryApplyChange(cg(
				s.changeAddControl(c0, "c0"),
				s.changeAddControl(c1, "c0")));
	}
	
	@Test
	public void addNestedSignature() throws ChangeRejectedException {
		Signature s = new Signature();
		ExecutorManager.getInstance().tryApplyChange(new BoundDescriptor(s,
				new Signature.ChangeAddSignatureDescriptor(
						new Signature.Identifier(), -1, new Signature())));
	}
}
