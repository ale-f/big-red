package dk.itu.big_red.model.interfaces;

import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.Signature;

public class SignatureBuilder {
	private Signature s = new Signature();
	
	public SignatureBuilder() {
		
	}
	
	public IControl newControl(String name) {
		Control c = new Control();
		
		c.setLabel(name.substring(0, 1));
		c.setLongName(name);
		
		s.addControl(c);
		return c;
	}
	
	public IPort newPort(IControl control, String name) {
		PortSpec p = new PortSpec(name, 0, 0);
		((Control)control).addPort(p);
		return p;
	}
	
	public Signature finish() {
		for (Control c : s.getControls()) {
			double l = c.getPorts().size();
			for (int i = 0; i < l; i++) {
				PortSpec p = c.getPorts().get(i);
				
				double d = (i / l) * 4.0;
				if (d >= 0.0 && d < 1.0) {
					p.setSegment(0);
					p.setDistance(d);
				} else if (d >= 1.0 && d < 2.0) {
					p.setSegment(1);
					p.setDistance(d - 1.0);
				} else if (d >= 2.0 && d < 3.0) {
					p.setSegment(2);
					p.setDistance(d - 2.0);
				} else if (d >= 3.0 && d < 4.0) {
					p.setSegment(3);
					p.setDistance(d - 3.0);
				}
			}
		}
		return s;
	}
}
