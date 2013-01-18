package org.bigraph.model.changes.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.process.IParticipantHost;

public class DescriptorExecutorManager extends DescriptorValidatorManager {
	private static final class Holder {
		private static final DescriptorExecutorManager INSTANCE =
				new DescriptorExecutorManager();
	}
	
	public static DescriptorExecutorManager getInstance() {
		return Holder.INSTANCE;
	}
	
	public DescriptorExecutorManager() {
	}
	
	private List<IDescriptorStepExecutor> executors =
			new ArrayList<IDescriptorStepExecutor>();
	
	public <T extends IDescriptorStepExecutor & IDescriptorStepValidator>
	void addHandler(T handler) {
		addValidator(handler);
		executors.add(handler);
	}
	
	public void addHandler(DescriptorExecutorManager manager) {
		addHandler(manager.new Handler());
	}
	
	public void addExecutor(IDescriptorStepExecutor executor) {
		executors.add(executor);
	}
	
	public void removeExecutor(IDescriptorStepExecutor executor) {
		executors.remove(executor);
	}
	
	protected List<? extends IDescriptorStepExecutor> getExecutors() {
		return executors;
	}
	
	public void tryApplyChange(Resolver resolver, IChangeDescriptor change)
			throws ChangeCreationException {
		tryValidateChange(resolver, change);
		
		IChangeDescriptor ch = run(resolver, change);
		if (ch != null)
			throw new Error(
					"BUG: " + ch + " passed validation but couldn't " +
					"be executed");
	}
	
	private IChangeDescriptor step(Resolver r, IChangeDescriptor c) {
		boolean passes = false;
		for (IDescriptorStepExecutor i : getExecutors())
			passes |= i.executeChange(r, c);
		return (passes ? null : c);
	}
	
	private IChangeDescriptor run(Resolver r, IChangeDescriptor c) {
		if (!(c instanceof ChangeDescriptorGroup)) {
			return step(r, c);
		} else {
			for (IChangeDescriptor i : (ChangeDescriptorGroup)c) {
				IChangeDescriptor j = run(r, i);
				if (j != null)
					return j;
			}
			return null;
		}
	}
	
	private final class Handler
			implements IDescriptorStepExecutor, IDescriptorStepValidator {
		@Override
		public final void setHost(IParticipantHost host) {
			/* do nothing */
		}
		
		@Override
		public boolean executeChange(Resolver r, IChangeDescriptor change_) {
			return (step(r, change_) == null);
		}
		
		@Override
		public boolean tryValidateChange(
				Process context, IChangeDescriptor change)
				throws ChangeCreationException {
			return DescriptorExecutorManager.this.tryValidateChange(
					context, change);
		}
	}
}
