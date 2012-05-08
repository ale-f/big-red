package dk.itu.big_red.model.interfaces;

import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;

public class SignatureBuilder {
	private Signature s = new Signature();
	private ChangeGroup cg = new ChangeGroup();
	
	public IControl newControl(String name) {
		Control c = new Control();
		cg.add(s.changeAddControl(c));
		cg.add(c.changeName(name));
		return c;
	}
	
	public IPort newPort(IControl control, String name) {
		PortSpec p = new PortSpec();
		cg.add(((Control)control).changeAddPort(p, name));
		return p;
	}
	
	public Signature finish() {
		for (Control c : s.getControls()) {
			double l = c.getPorts().size();
			for (int i = 0; i < l; i++) {
				PortSpec p = c.getPorts().get(i);
				
				double d = (i / l) * 4.0;
				if (d >= 0.0 && d < 1.0) {
					cg.add(p.changeSegment(0));
					cg.add(p.changeDistance(d));
				} else if (d >= 1.0 && d < 2.0) {
					cg.add(p.changeSegment(1));
					cg.add(p.changeDistance(d - 1.0));
				} else if (d >= 2.0 && d < 3.0) {
					cg.add(p.changeSegment(2));
					cg.add(p.changeDistance(d - 2.0));
				} else if (d >= 3.0 && d < 4.0) {
					cg.add(p.changeSegment(2));
					cg.add(p.changeDistance(d - 3.0));
				}
			}
		}
		try {
			s.tryApplyChange(cg);
		} catch (ChangeRejectedException cre) {
			return null;
		}
		return s;
	}
}
