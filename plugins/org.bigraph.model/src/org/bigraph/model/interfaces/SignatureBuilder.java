package org.bigraph.model.interfaces;

import org.bigraph.model.Control;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Signature;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;

public class SignatureBuilder {
	private Signature s = new Signature();
	private ChangeGroup cg = new ChangeGroup();
	
	public IControl newControl(String name) {
		Control c = new Control();
		cg.add(s.changeAddControl(c, name));
		return c;
	}
	
	public IPort newPort(IControl control, String name) {
		PortSpec p = new PortSpec();
		cg.add(((Control)control).changeAddPort(p, name));
		return p;
	}
	
	public Signature finish() {
		try {
			s.tryApplyChange(cg);
		} catch (ChangeRejectedException cre) {
			return null;
		}
		return s;
	}
}
