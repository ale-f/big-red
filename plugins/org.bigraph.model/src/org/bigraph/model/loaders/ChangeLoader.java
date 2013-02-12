package org.bigraph.model.loaders;

import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

public abstract class ChangeLoader extends Loader implements IChangeLoader {
	public ChangeLoader() {
	}
	
	public ChangeLoader(Loader parent) {
		super(parent);
	}
	
	private ChangeGroup cg = new ChangeGroup();
	private PropertyScratchpad scratch = new PropertyScratchpad();
	
	@Override
	public void addChange(IChange c) {
		if (c != null) {
			cg.add(c);
			c.simulate(scratch, null);
		}
	}
	
	protected void addChange(IChangeDescriptor c) {
		addChange(new BoundDescriptor(getResolver(), c));
	}
	
	@Override
	public ChangeGroup getChanges() {
		return cg;
	}
	
	@Override
	public PropertyScratchpad getScratch() {
		return scratch;
	}
	
	protected void executeChanges() throws LoadFailedException {
		try {
			ExecutorManager.getInstance().tryApplyChange(getChanges());
		} catch (ChangeCreationException cre) {
			throw new LoadFailedException(cre);
		}
	}
}
