package org.bigraph.model.assistants;

import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

public class ExecutorManager extends DescriptorExecutorManager {
	private static final class Holder {
		private static final ExecutorManager INSTANCE = new ExecutorManager();
	}
	
	public static ExecutorManager getInstance() {
		return Holder.INSTANCE;
	}
	
	public boolean tryValidateChange(IChangeDescriptor change)
			throws ChangeCreationException {
		return tryValidateChange((PropertyScratchpad)null, change);
	}
	
	public boolean tryValidateChange(
			PropertyScratchpad context, IChangeDescriptor change)
			throws ChangeCreationException {
		return super.tryValidateChange(context, null, change);
	}
	
	public void tryApplyChange(IChangeDescriptor change)
			throws ChangeCreationException {
		super.tryApplyChange(null, change);
	}
}
