package dk.itu.big_red.model.assistants;

import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.Signature;

public class SignatureScratchpad extends PropertyScratchpad {
	private Signature signature;
	public SignatureScratchpad(Signature signature) {
		this.signature = signature;
	}
	
	public Signature getSignature() {
		return signature;
	}
	
	public void addControl(Control c) {
		this.<Control>getModifiableList(getSignature(),
				Signature.PROPERTY_CONTROL).add(c);
		setProperty(c, Control.PROPERTY_SIGNATURE, getSignature());
	}
	
	public void removeControl(Control c) {
		this.<Control>getModifiableList(getSignature(),
				Signature.PROPERTY_CONTROL).remove(c);
		setProperty(c, Control.PROPERTY_SIGNATURE, null);
	}
	
	public void setNameFor(Control c, String name) {
		setProperty(c, Control.PROPERTY_NAME, name);
	}
	
	public void addPortFor(Control c, PortSpec p) {
		this.<PortSpec>getModifiableList(c, Control.PROPERTY_PORT).add(p);
		setProperty(p, PortSpec.PROPERTY_CONTROL, c);
	}
	
	public void removePortFor(Control c, PortSpec p) {
		this.<PortSpec>getModifiableList(c, Control.PROPERTY_PORT).remove(p);
		setProperty(p, PortSpec.PROPERTY_CONTROL, null);
	}
}
