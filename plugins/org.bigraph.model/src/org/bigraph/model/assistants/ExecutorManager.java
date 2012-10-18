package org.bigraph.model.assistants;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepExecutor;

public class ExecutorManager {
	private static final class Holder {
		private static final ExecutorManager INSTANCE =
				new ExecutorManager(ValidatorManager.getInstance());
	}
	
	public static ExecutorManager getInstance() {
		return Holder.INSTANCE;
	}
	
	private final ValidatorManager validator;
	
	public ExecutorManager(ValidatorManager validator) {
		this.validator = validator;
	}
	
	private List<IStepExecutor> executors =
			new ArrayList<IStepExecutor>();
	
	public void addExecutor(IStepExecutor executor) {
		executors.add(executor);
	}
	
	public void removeExecutor(IStepExecutor executor) {
		executors.remove(executor);
	}
	
	protected List<? extends IStepExecutor> getExecutors() {
		return executors;
	}
	
	public void tryExecuteChange(IChange change)
			throws ChangeRejectedException {
		if (validator != null)
			validator.tryValidateChange(change);
		
		Process p = new Process();
		IChange ch = p.run(change);
		if (ch != null)
			throw new Error(
					"BUG: " + ch + " passed validation but couldn't" +
					"be executed");
	}
	
	private class Process {
		public IChange run(IChange c) {
			c.beforeApply();
			if (!(c instanceof ChangeGroup)) {
				boolean passes = false;
				for (IStepExecutor i : getExecutors()) {
					passes = i.executeChange(c);
					if (passes)
						break;
				}
				return (passes ? null : c);
			} else {
				for (IChange i : (ChangeGroup)c) {
					IChange j = run(i);
					if (j != null)
						return j;
				}
				return null;
			}
		}
	}
}