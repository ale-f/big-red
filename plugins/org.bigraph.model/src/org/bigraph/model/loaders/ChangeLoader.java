package org.bigraph.model.loaders;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

public abstract class ChangeLoader extends Loader implements IChangeLoader {
	public ChangeLoader() {
	}
	
	public ChangeLoader(Loader parent) {
		super(parent);
	}
	
	private ChangeDescriptorGroup cdg = new ChangeDescriptorGroup();
	private PropertyScratchpad scratch = new PropertyScratchpad();
	
	@Override
	public void addChange(IChangeDescriptor c) {
		if (c == null)
			return;
		try {
			DescriptorExecutorManager.getInstance().tryValidateChange(
					getScratch(), getResolver(), c);
			c.simulate(getScratch(), getResolver());
			cdg.add(c);
		} catch (ChangeCreationException e) {
			/* do nothing */
		}
	}
	
	@Override
	public ChangeDescriptorGroup getChanges() {
		return cdg;
	}
	
	@Override
	public PropertyScratchpad getScratch() {
		return scratch;
	}
	
	protected void executeChanges() throws LoadFailedException {
		try {
			DescriptorExecutorManager.getInstance().tryApplyChange(
					getResolver(), getChanges());
		} catch (ChangeCreationException cre) {
			throw new LoadFailedException(cre);
		}
	}
}
