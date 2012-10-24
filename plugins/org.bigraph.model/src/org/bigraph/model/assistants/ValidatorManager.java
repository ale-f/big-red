package org.bigraph.model.assistants;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepValidator;

public class ValidatorManager implements IStepValidator {
	private List<IStepValidator> validators =
			new ArrayList<IStepValidator>();
	
	public void addValidator(IStepValidator validator) {
		validators.add(validator);
	}
	
	public void removeValidator(IStepValidator validator) {
		validators.remove(validator);
	}
	
	protected List<? extends IStepValidator> getValidators() {
		return validators;
	}
	
	public void tryValidateChange(IChange change)
			throws ChangeRejectedException {
		tryValidateChange((PropertyScratchpad)null, change);
	}
	
	public boolean tryValidateChange(
			PropertyScratchpad context, IChange change)
			throws ChangeRejectedException {
		StandaloneProcess p =
				new StandaloneProcess(new PropertyScratchpad(context));
		IChange ch = p.run(change);
		if (ch != null) {
			throw new ChangeRejectedException(ch,
					"" + ch + " was not recognised by the validator");
		} else return true;
	}
	
	@Override
	public boolean tryValidateChange(Process context, IChange change)
			throws ChangeRejectedException {
		return (new ParticipantProcess(context).step(change) == null);
	}
	
	private abstract class AbstractProcess implements Process {
		protected IChange step(IChange c) throws ChangeRejectedException {
			boolean passes = false;
			for (IStepValidator i : getValidators())
				passes |= i.tryValidateChange(this, c);
			return (passes ? null : c);
		}
	}
	
	private final class StandaloneProcess extends AbstractProcess {
		private final PropertyScratchpad scratch;
		private final ArrayList<Callback> callbacks =
				new ArrayList<Callback>();
		
		@Override
		public void addCallback(Callback c) {
			callbacks.add(c);
		}
		
		public List<? extends Callback> getCallbacks() {
			return callbacks;
		}
		
		private StandaloneProcess(PropertyScratchpad scratch) {
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
				IChange d = step(c);
				if (d == null)
					c.simulate(getScratch());
				return d;
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
	
	private final class ParticipantProcess extends AbstractProcess {
		private final IStepValidator.Process process;
		
		public ParticipantProcess(IStepValidator.Process process) {
			this.process = process;
		}
		
		@Override
		public PropertyScratchpad getScratch() {
			return process.getScratch();
		}

		@Override
		public void addCallback(Callback c) {
			process.addCallback(c);
		}
	}
}
