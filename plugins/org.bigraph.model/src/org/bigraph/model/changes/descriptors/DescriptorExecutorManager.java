package org.bigraph.model.changes.descriptors;

import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
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
	
	@Override
	public void addParticipant(IParticipant participant) {
		if (participant instanceof DescriptorExecutorManager)
			participant =
					((DescriptorExecutorManager)participant).getHandler();
		super.addParticipant(participant);
	}
	
	public void tryApplyChange(Resolver resolver, IChangeDescriptor change)
			throws ChangeCreationException {
		tryValidateChange(resolver, change);
		
		IChangeDescriptor ch = run(resolver, change);
		if (ch != null)
			throw new RuntimeException(
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
		if (!(c instanceof IChangeDescriptor.Group)) {
			return step(r, c);
		} else {
			for (IChangeDescriptor i : (IChangeDescriptor.Group)c) {
				IChangeDescriptor j = run(r, i);
				if (j != null)
					return j;
			}
			return null;
		}
	}
	
	final Handler handler = new Handler();
	
	Handler getHandler() {
		return handler;
	}
	
	final class Handler
			implements IDescriptorStepExecutor, IDescriptorStepValidator {
		@Override
		public final void setHost(IParticipantHost host) {
			/* do nothing */
		}
		
		@Override
		public boolean executeChange(Resolver r, IChangeDescriptor change_) {
			return (run(r, change_) == null);
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
