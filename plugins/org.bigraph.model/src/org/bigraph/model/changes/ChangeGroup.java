package org.bigraph.model.changes;

import java.util.ArrayList;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

/**
 * A <strong>ChangeGroup</strong> is a collection of {@link IChange}s.
 * @author alec
 * @see ArrayList
 */
public class ChangeGroup extends ChangeDescriptorGroup implements IChange {
	private static final long serialVersionUID = -5459931168098216973L;

	public ChangeGroup() {
		super();
	}
	
	@Override
	public ChangeGroup inverse() {
		ChangeGroup changes = new ChangeGroup();
		for (IChangeDescriptor c : this)
			if (c != null)
				changes.add(0, c.inverse());
		return changes;
	}

	@Override
	public void simulate(PropertyScratchpad context, Resolver resolver) {
		try {
			for (IChangeDescriptor c : this)
				if (c != null)
					c.simulate(context, null);
		} catch (ChangeCreationException e) {
			/* XXX: do nothing */
		}
	}
}
