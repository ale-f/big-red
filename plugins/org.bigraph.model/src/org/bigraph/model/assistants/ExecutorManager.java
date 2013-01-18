package org.bigraph.model.assistants;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;
import org.bigraph.model.process.IParticipantHost;

public class ExecutorManager extends ValidatorManager 
		implements IChangeExecutor {
	private static final class Holder {
		private static final ExecutorManager INSTANCE = new ExecutorManager();
	}
	
	public static ExecutorManager getInstance() {
		return Holder.INSTANCE;
	}
	
	public ExecutorManager() {
	}
	
	private List<IStepExecutor> executors =
			new ArrayList<IStepExecutor>();
	
	public <T extends IStepExecutor & IStepValidator> void addHandler(
			T handler) {
		addValidator(handler);
		executors.add(handler);
	}
	
	public void addHandler(ExecutorManager manager) {
		addHandler(manager.new Handler());
	}
	
	public void addExecutor(IStepExecutor executor) {
		executors.add(executor);
	}
	
	public void removeExecutor(IStepExecutor executor) {
		executors.remove(executor);
	}
	
	protected List<? extends IStepExecutor> getExecutors() {
		return executors;
	}
	
	@Override
	public void tryApplyChange(IChange change)
			throws ChangeRejectedException {
		tryValidateChange(change);
		
		IChange ch = run(change);
		if (ch != null)
			throw new Error(
					"BUG: " + ch + " passed validation but couldn't " +
					"be executed");
	}
	
	private IChange step(IChange c) {
		boolean passes = false;
		for (IStepExecutor i : getExecutors())
			passes |= i.executeChange(c);
		return (passes ? null : c);
	}
	
	private IChange run(IChange c) {
		c.beforeApply();
		if (!(c instanceof ChangeGroup)) {
			return step(c);
		} else {
			for (IChange i : (ChangeGroup)c) {
				IChange j = run(i);
				if (j != null)
					return j;
			}
			return null;
		}
	}
	
	private final class Handler implements IStepExecutor, IStepValidator {
		@Override
		public final void setHost(IParticipantHost host) {
			/* do nothing */
		}
		
		@Override
		public boolean executeChange(IChange change_) {
			return (step(change_) == null);
		}
		
		@Override
		public boolean tryValidateChange(Process context, IChange change)
				throws ChangeRejectedException {
			return ExecutorManager.this.tryValidateChange(context, change);
		}
	}
}
