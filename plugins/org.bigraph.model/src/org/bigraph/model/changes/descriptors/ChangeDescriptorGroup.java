package org.bigraph.model.changes.descriptors;

import java.util.ArrayList;
import java.util.Collection;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;

/**
 * A <strong>ChangeDescriptorGroup</strong> is a collection of {@link
 * IChangeDescriptor}s.
 * @author alec
 * @see ArrayList
 */
public class ChangeDescriptorGroup extends ArrayList<IChangeDescriptor>
		implements IChangeDescriptor.Group {
	private static final long serialVersionUID = 8660898241398280925L;
	
	public ChangeDescriptorGroup() {
		super();
	}

	public ChangeDescriptorGroup(Collection<? extends IChangeDescriptor> c) {
		super(c);
	}

	public ChangeDescriptorGroup(int initialCapacity) {
		super(initialCapacity);
	}

	@Override
	public ChangeDescriptorGroup clone() {
		return new ChangeDescriptorGroup(this);
	}
	
	@Override
	public void simulate(PropertyScratchpad context, Resolver r) {
		for (IChangeDescriptor cd : this)
			if (cd != null)
				cd.simulate(context, r);
	}
	
	@Override
	public ChangeDescriptorGroup inverse() {
		ChangeDescriptorGroup cdg = new ChangeDescriptorGroup();
		for (IChangeDescriptor cd : this)
			if (cd != null)
				cdg.add(0, cd.inverse());
		return cdg;
	}
}