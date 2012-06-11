package dk.itu.big_red.model.interfaces;

import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.Signature;

public class SignatureBuilder {
	private Signature s = new Signature();
	private ChangeGroup cg = new ChangeGroup();
	
	public IControl newControl(String name) {
		Control c = new Control();
		cg.add(s.changeAddControl(c));
		cg.add(ExtendedDataUtilities.changeControlName(c, name));
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
					cg.add(ExtendedDataUtilities.changeSegment(p, 0));
					cg.add(ExtendedDataUtilities.changeDistance(p, d));
				} else if (d >= 1.0 && d < 2.0) {
					cg.add(ExtendedDataUtilities.changeSegment(p, 1));
					cg.add(ExtendedDataUtilities.changeDistance(p, d - 1.0));
				} else if (d >= 2.0 && d < 3.0) {
					cg.add(ExtendedDataUtilities.changeSegment(p, 2));
					cg.add(ExtendedDataUtilities.changeDistance(p, d - 2.0));
				} else if (d >= 3.0 && d < 4.0) {
					cg.add(ExtendedDataUtilities.changeSegment(p, 2));
					cg.add(ExtendedDataUtilities.changeDistance(p, d - 3.0));
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
