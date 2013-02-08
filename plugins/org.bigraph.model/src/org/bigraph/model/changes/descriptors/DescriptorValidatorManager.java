package org.bigraph.model.changes.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.process.AbstractParticipantHost;
import org.bigraph.model.process.IParticipant;
import org.bigraph.model.process.IParticipantHost;

public class DescriptorValidatorManager
		extends AbstractParticipantHost implements IDescriptorStepValidator {
	@Override
	public final void setHost(IParticipantHost host) {
		/* do nothing */
	}
	
	@Override
	public void removeParticipant(IParticipant participant) {
		super.removeParticipant(participant);
	}
	
	public boolean tryValidateChange(Resolver r, IChangeDescriptor change)
			throws ChangeCreationException {
		return tryValidateChange(null, r, change);
	}
	
	public boolean tryValidateChange(
			PropertyScratchpad context, Resolver r, IChangeDescriptor change)
			throws ChangeCreationException {
		StandaloneProcess p =
				new StandaloneProcess(new PropertyScratchpad(context), r);
		IChangeDescriptor ch = p.run(change);
		if (ch != null) {
			throw new ChangeCreationException(ch,
					"" + ch + " was not recognised by the validator");
		} else return true;
	}
	
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		return (new ParticipantProcess(context).step(change) == null);
	}
	
	private abstract class AbstractProcess implements Process {
		protected IChangeDescriptor step(
				IChangeDescriptor c) throws ChangeCreationException {
			boolean passes = false;
			for (IDescriptorStepValidator i :
					getParticipants(IDescriptorStepValidator.class))
				passes |= i.tryValidateChange(this, c);
			return (passes ? null : c);
		}
	}
	
	private final class StandaloneProcess extends AbstractProcess {
		private final Resolver resolver;
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
		
		private StandaloneProcess(
				PropertyScratchpad scratch, Resolver resolver) {
			this.scratch = scratch;
			this.resolver = resolver;
		}
		
		@Override
		public PropertyScratchpad getScratch() {
			return scratch;
		}
		
		@Override
		public Resolver getResolver() {
			return resolver;
		}
		
		public IChangeDescriptor run(IChangeDescriptor c)
				throws ChangeCreationException {
			IChangeDescriptor i = doValidation(c);
			if (i == null)
				for (Callback j : getCallbacks())
					j.run();
			return i;
		}
		
		protected IChangeDescriptor doValidation(IChangeDescriptor c)
				throws ChangeCreationException {
			if (c == null) {
				throw new ChangeCreationException(c, "" + c + " is not ready");
			} else if (!(c instanceof IChangeDescriptor.Group)) {
				IChangeDescriptor d = step(c);
				if (d == null)
					c.simulate(getScratch(), getResolver());
				return d;
			} else {
				for (IChangeDescriptor i : (IChangeDescriptor.Group)c) {
					IChangeDescriptor j = doValidation(i);
					if (j != null)
						return j;
				}
				return null;
			}
		}
	}
	
	private final class ParticipantProcess extends AbstractProcess {
		private final IDescriptorStepValidator.Process process;
		
		public ParticipantProcess(IDescriptorStepValidator.Process process) {
			this.process = process;
		}
		
		@Override
		public PropertyScratchpad getScratch() {
			return process.getScratch();
		}

		@Override
		public Resolver getResolver() {
			return process.getResolver();
		}
		
		@Override
		public void addCallback(Callback c) {
			process.addCallback(c);
		}
	}
}
