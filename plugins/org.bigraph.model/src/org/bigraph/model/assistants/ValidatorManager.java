package org.bigraph.model.assistants;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepValidator;
import org.bigraph.model.changes.IStepValidator.Callback;

public class ValidatorManager {
	private static final class Holder {
		private static final ValidatorManager INSTANCE =
				new ValidatorManager();
	}
	
	public static ValidatorManager getInstance() {
		return Holder.INSTANCE;
	}
	
	private List<IStepValidator> validators =
			new ArrayList<IStepValidator>();
	
	public void addValidator(IStepValidator validator) {
		validators.add(validator);
	}
	
	public void removeValidator(IStepValidator validator) {
		validators.remove(validator);
	}
	
	public List<? extends IStepValidator> getValidators() {
		return validators;
	}
	
	public void tryValidateChange(IChange change)
			throws ChangeRejectedException {
		tryValidateChange(null, change);
	}
	
	public boolean tryValidateChange(
			PropertyScratchpad context, IChange change)
			throws ChangeRejectedException {
		Process p = new Process(new PropertyScratchpad(context));
		IChange ch = p.run(change);
		if (ch != null) {
			throw new ChangeRejectedException(ch,
					"" + ch + " was not recognised by the validator");
		} else return true;
	}
	
	private final class Process implements IStepValidator.Process {
		private final PropertyScratchpad scratch;
		private ArrayList<Callback> callbacks = new ArrayList<Callback>();
		
		@Override
		public void addCallback(Callback c) {
			callbacks.add(c);
		}
		
		public List<? extends Callback> getCallbacks() {
			return callbacks;
		}
		
		private Process(PropertyScratchpad scratch) {
			this.scratch = scratch;
		}
		
		@Override
		public PropertyScratchpad getScratch() {
			return scratch;
		}
		
		public IChange run(IChange c) throws ChangeRejectedException {
			IChange i = doValidation(c);
			if (i == null)
				for (Callback j : getCallbacks())
					j.run();
			return i;
		}
		
		protected IChange doValidation(IChange c)
				throws ChangeRejectedException {
			if (c == null || !c.isReady()) {
				throw new ChangeRejectedException(c, "" + c + " is not ready");
			} else if (!(c instanceof ChangeGroup)) {
				boolean passes = false;
				for (IStepValidator i : getValidators())
					passes |= i.tryValidateChange(this, c);
				if (passes)
					c.simulate(getScratch());
				return (passes ? null : c);
			} else {
				for (IChange i : (ChangeGroup)c) {
					IChange j = doValidation(i);
					if (j != null)
						return j;
				}
				return null;
			}
		}
	}
}
