package org.bigraph.model.changes.descriptors;

import java.util.ArrayList;
import java.util.Collection;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeGroup;

/**
 * A <strong>ChangeDescriptorGroup</strong> is a collection of {@link
 * IChangeDescriptor}s.
 * @author alec
 * @see ArrayList
 */
public class ChangeDescriptorGroup extends ArrayList<IChangeDescriptor>
		implements IChangeDescriptor {
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
	public ChangeGroup createChange(PropertyScratchpad context, Resolver r)
			throws ChangeCreationException {
		ChangeGroup cg = new ChangeGroup();
		context = new PropertyScratchpad(context);
		for (IChangeDescriptor one : this)
			cg.add(context.executeChange(one.createChange(context, r)));
		return cg;
	}
	
	@Override
	public void simulate(PropertyScratchpad context, Resolver r)
			throws ChangeCreationException {
		context.executeChange(createChange(context, r));
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