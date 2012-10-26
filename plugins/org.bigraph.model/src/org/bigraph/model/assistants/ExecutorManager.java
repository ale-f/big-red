package org.bigraph.model.assistants;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.changes.IStepExecutor;

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
	
	public void addExecutor(IStepExecutor executor) {
		executors.add(executor);
	}
	
	public void addExecutor(ExecutorManager manager) {
		addValidator(manager);
		executors.add(manager.createStepExecutor());
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
	
	IStepExecutor createStepExecutor() {
		return new IStepExecutor() {
			@Override
			public boolean executeChange(IChange change_) {
				return (step(change_) == null);
			}
		};
	}
}
