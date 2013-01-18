package org.bigraph.model.changes.descriptors;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.process.IParticipant;
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
	
	@Override
	public void addParticipant(IParticipant participant) {
		if (participant instanceof DescriptorExecutorManager)
			participant =
					((DescriptorExecutorManager)participant).new Handler();
		super.addParticipant(participant);
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
		for (IDescriptorStepExecutor i :
				getParticipants(IDescriptorStepExecutor.class))
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
