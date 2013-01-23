package org.bigraph.model.loaders;

import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;

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
			c.simulate(scratch);
		}
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
		} catch (ChangeRejectedException cre) {
			throw new LoadFailedException(cre);
		}
	}
}
