package org.bigraph.model.interfaces;

import org.bigraph.model.Control;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Signature;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

public class SignatureBuilder {
	private Signature s = new Signature();
	
	private void doChange(IChangeDescriptor ch) {
		try {
			DescriptorExecutorManager.getInstance().tryApplyChange(s, ch);
		} catch (ChangeCreationException cce) {
			/* do nothing */
		}
	}
	
	public IControl newControl(String name) {
		Control.Identifier cid = new Control.Identifier(name);
		doChange(new Signature.ChangeAddControlDescriptor(
				new Signature.Identifier(), cid));
		return cid.lookup(null, s);
	}
	
	public IPort newPort(IControl control, String name) {
		PortSpec.Identifier pid = new PortSpec.Identifier(
				name, ((Control)control).getIdentifier());
		doChange(new Control.ChangeAddPortSpecDescriptor(pid));
		return pid.lookup(null, s);
	}
	
	public Signature finish() {
		return s;
	}
}
