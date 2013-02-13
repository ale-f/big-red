package org.bigraph.model.changes.descriptors;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.IDescriptorStepValidator.Process;
import org.bigraph.model.changes.descriptors.IDescriptorStepValidator.Callback;
import org.bigraph.model.process.IParticipantHost;

/**
 * A <strong>BoundDescriptor</strong> is an {@link IChangeDescriptor} coupled
 * to a {@link Resolver}.
 * @author alec
 */
public class BoundDescriptor implements IChangeDescriptor {
	private final Resolver resolver;
	private final IChangeDescriptor descriptor;
	
	private final class ProcessWrapper implements Process {
		private final Process changeProcess;
		
		public ProcessWrapper(Process changeProcess) {
			this.changeProcess = changeProcess;
		}
		
		@Override
		public void addCallback(final Callback c) {
			changeProcess.addCallback(c);
		}
		
		@Override
		public Resolver getResolver() {
			return BoundDescriptor.this.getResolver();
		}
		
		@Override
		public PropertyScratchpad getScratch() {
			return changeProcess.getScratch();
		}
	}
	
	private static final class Handler
			implements IDescriptorStepValidator, IDescriptorStepExecutor {
		@Override
		public void setHost(IParticipantHost host) {
			/* do nothing */
		}
		
		private static final Handler INSTANCE = new Handler();
		
		@Override
		public boolean tryValidateChange(
				Process context, IChangeDescriptor change)
				throws ChangeCreationException {
			if (change instanceof BoundDescriptor) {
				BoundDescriptor bd = (BoundDescriptor)change;
				DescriptorExecutorManager.getInstance().tryValidateChange(
						bd.new ProcessWrapper(context),
						bd.getDescriptor());
			} else return false;
			return true;
		}
		
		@Override
		public boolean executeChange(Resolver resolver, IChangeDescriptor change) {
			if (change instanceof BoundDescriptor) {
				BoundDescriptor bd = (BoundDescriptor)change;
				return DescriptorExecutorManager.getInstance().getHandler().
						executeChange(bd.getResolver(), bd.getDescriptor());
			} else return false;
		}
	}
	
	static {
		DescriptorExecutorManager.getInstance().addParticipant(
				Handler.INSTANCE);
	}
	
	public BoundDescriptor(Resolver resolver, IChangeDescriptor descriptor) {
		this.resolver = resolver;
		this.descriptor = descriptor;
	}

	public Resolver getResolver() {
		return resolver;
	}
	
	public IChangeDescriptor getDescriptor() {
		return descriptor;
	}
	
	@Override
	public BoundDescriptor inverse() {
		return new BoundDescriptor(getResolver(), getDescriptor().inverse());
	}

	@Override
	public String toString() {
		return "BoundDescriptor(" + getResolver() +
				", " + getDescriptor() + ")";
	}

	@Override
	public void simulate(PropertyScratchpad context, Resolver resolver) {
		getDescriptor().simulate(context, getResolver());
	}
}
