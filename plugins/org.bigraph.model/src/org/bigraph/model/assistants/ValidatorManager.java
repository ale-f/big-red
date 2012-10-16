package org.bigraph.model.assistants;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeValidator2;
import org.bigraph.model.changes.IChangeValidator2.Callback;

public class ValidatorManager {
	static {
		getInstance().addValidator(new EditValidator());
		getInstance().addValidator(new BigraphValidator());
		getInstance().addValidator(new SignatureValidator());
		getInstance().addValidator(new ModelObjectValidator());
	}
	
	private static final class Holder {
		private static final ValidatorManager INSTANCE =
				new ValidatorManager();
	}
	
	public static ValidatorManager getInstance() {
		return Holder.INSTANCE;
	}
	
	private List<IChangeValidator2> validators =
			new ArrayList<IChangeValidator2>();
	
	public void addValidator(IChangeValidator2 validator) {
		validators.add(validator);
	}
	
	public void removeValidator(IChangeValidator2 validator) {
		validators.remove(validator);
	}
	
	public List<? extends IChangeValidator2> getValidators() {
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
	
	private final class Process implements IChangeValidator2.Process {
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
			if (!c.isReady()) {
				throw new ChangeRejectedException(c, "" + c + " is not ready");
			} else if (!(c instanceof ChangeGroup)) {
				boolean passes = false;
				for (IChangeValidator2 i : getValidators()) {
					if (i.tryValidateChange(Process.this, c)) {
						passes = true;
						break;
					}
				}
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
