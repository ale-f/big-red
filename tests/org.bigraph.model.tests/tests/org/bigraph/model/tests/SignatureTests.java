package org.bigraph.model.tests;

import org.junit.Test;
import static org.junit.Assert.*;

import org.bigraph.model.Control;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Signature;
import org.bigraph.model.changes.ChangeRejectedException;

import static org.bigraph.model.tests.BigraphTests.cg;

public class SignatureTests {
	@Test
	public void addSimpleControl() throws ChangeRejectedException {
		Signature s = new Signature();
		s.tryApplyChange(s.changeAddControl(new Control(), "c0"));
	}
	
	@Test
	public void addComplexControl() throws ChangeRejectedException {
		Signature s = new Signature();
		Control c0 = new Control();
		s.tryApplyChange(cg(
				s.changeAddControl(c0, "c0"),
				c0.changeAddPort(new PortSpec(), "p0"),
				c0.changeKind(Kind.ATOMIC)));
	}
	
	@Test
	public void removeControl() throws ChangeRejectedException {
		Signature s = new Signature();
		try {
			s.tryApplyChange(s.changeAddControl(new Control(), "c0"));
		} catch (ChangeRejectedException e) {
			fail(e.getRationale());
		}
		s.tryApplyChange(s.getControl("c0").changeRemove());
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void removeAbsentControl() throws ChangeRejectedException {
		new Signature().tryApplyChange(new Control().changeRemove());
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void addDuplicateName() throws ChangeRejectedException {
		Signature s = new Signature();
		Control
			c0 = new Control(),
			c1 = new Control();
		s.tryApplyChange(cg(
				s.changeAddControl(c0, "c0"),
				s.changeAddControl(c1, "c0")));
	}
	
	@Test
	public void addNestedSignature() throws ChangeRejectedException {
		new Signature().changeAddSignature(new Signature());
	}
}
