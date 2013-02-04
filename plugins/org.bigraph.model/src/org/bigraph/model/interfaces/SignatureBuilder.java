package org.bigraph.model.interfaces;

import org.bigraph.model.Control;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Signature;
import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.BoundDescriptor;

public class SignatureBuilder {
	private Signature s = new Signature();
	private ChangeGroup cg = new ChangeGroup();
	private PropertyScratchpad scratch = new PropertyScratchpad();
	
	private void addChange(IChange ch) {
		if (ch == null)
			return;
		cg.add(ch);
		getScratch().executeChange(ch);
	}
	
	private PropertyScratchpad getScratch() {
		return scratch;
	}
	
	public IControl newControl(String name) {
		Control c = new Control();
		addChange(s.changeAddControl(c, name));
		return c;
	}
	
	public IPort newPort(IControl control, String name) {
		PortSpec p = new PortSpec();
		addChange(new BoundDescriptor(s,
				new Control.ChangeAddPortSpecDescriptor(
						new PortSpec.Identifier(name,
								((Control)control).
										getIdentifier(getScratch())))));
		return p;
	}
	
	public Signature finish() {
		try {
			ExecutorManager.getInstance().tryApplyChange(cg);
		} catch (ChangeRejectedException cre) {
			return null;
		}
		return s;
	}
}
