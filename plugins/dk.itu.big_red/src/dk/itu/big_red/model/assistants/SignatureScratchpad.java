package dk.itu.big_red.model.assistants;

import java.util.ArrayList;
import java.util.List;

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
	
	private List<Control> getModifiableControls() {
		List<Control> c;
		if (hasProperty(getSignature(), Signature.PROPERTY_CONTROL)) {
			c = getSignature().getControls(this);
		} else {
			setProperty(getSignature(), Signature.PROPERTY_CONTROL,
					c = new ArrayList<Control>(getSignature().getControls()));
		}
		return c;
	}
	
	private List<PortSpec> getModifiablePorts(Control b) {
		List<PortSpec> c;
		if (hasProperty(getSignature(), Control.PROPERTY_PORT)) {
			c = b.getPorts(this);
		} else {
			setProperty(getSignature(), Control.PROPERTY_PORT,
					c = new ArrayList<PortSpec>(b.getPorts()));
		}
		return c;
	}
	
	public void addControl(Control c) {
		getModifiableControls().add(c);
		setProperty(c, Control.PROPERTY_SIGNATURE, getSignature());
	}
	
	public void removeControl(Control c) {
		getModifiableControls().remove(c);
		setProperty(c, Control.PROPERTY_SIGNATURE, null);
	}
	
	public void setNameFor(Control c, String name) {
		setProperty(c, Control.PROPERTY_NAME, name);
	}
	
	public void addPortFor(Control c, PortSpec p) {
		getModifiablePorts(c).add(p);
		setProperty(p, PortSpec.PROPERTY_CONTROL, c);
	}
	
	public void removePortFor(Control c, PortSpec p) {
		getModifiablePorts(c).remove(p);
		setProperty(p, PortSpec.PROPERTY_CONTROL, null);
	}
}
