package org.bigraph.model.assistants;

import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.IDescriptorStepValidator;
import org.bigraph.model.process.IParticipant;
import org.bigraph.model.process.IParticipantHost;

public class ExecutorManager extends ValidatorManager {
	private static final class Holder {
		private static final ExecutorManager INSTANCE = new ExecutorManager();
	}
	
	public static ExecutorManager getInstance() {
		return Holder.INSTANCE;
	}
	
	@Override
	public void addParticipant(IParticipant participant) {
		if (participant instanceof ExecutorManager)
			participant = ((ExecutorManager)participant).getHandler();
		super.addParticipant(participant);
	}
	
	public void tryApplyChange(IChange change)
			throws ChangeCreationException {
		tryValidateChange(change);
		
		IChange ch = run(change);
		if (ch != null)
			throw new Error(
					"BUG: " + ch + " passed validation but couldn't " +
					"be executed");
	}
	
	private IChange step(IChange c) {
		boolean passes = false;
		for (IDescriptorStepExecutor i :
				getParticipants(IDescriptorStepExecutor.class))
			passes |= i.executeChange(null, c);
		return (passes ? null : c);
	}
	
	private IChange run(IChange c) {
		if (!(c instanceof IChange.Group)) {
			return step(c);
		} else {
			for (IChange i : (IChange.Group)c) {
				IChange j = run(i);
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
		public boolean executeChange(
				Resolver resolver, IChangeDescriptor change_) {
			return (change_ instanceof IChange ?
					run((IChange)change_) == null : false);
		}
		
		@Override
		public boolean tryValidateChange(
				Process context, IChangeDescriptor change)
				throws ChangeCreationException {
			return ExecutorManager.this.tryValidateChange(context, change);
		}
	}
}
