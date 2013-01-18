package org.bigraph.model.changes.descriptors;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;
import org.bigraph.model.changes.descriptors.IDescriptorStepValidator.Callback;
import org.bigraph.model.process.IParticipantHost;

/**
 * A <strong>BoundDescriptor</strong> is an {@link IChange} consisting of an
 * {@link IChangeDescriptor} coupled to a {@link Resolver}; it serves as a
 * bridge between IChange- and IChangeDescriptor-based APIs.
 * @author alec
 */
public class BoundDescriptor implements IChange {
	private final Resolver resolver;
	private final IChangeDescriptor descriptor;
	
	private final class ProcessWrapper
			implements IDescriptorStepValidator.Process {
		private final IStepValidator.Process changeProcess;
		
		public ProcessWrapper(IStepValidator.Process changeProcess) {
			this.changeProcess = changeProcess;
		}
		
		@Override
		public void addCallback(final Callback c) {
			changeProcess.addCallback(new IStepValidator.Callback() {
				@Override
				public void run() throws ChangeRejectedException {
					try {
						c.run();
					} catch (ChangeCreationException cce) {
						throw new ChangeRejectedException(BoundDescriptor.this,
								cce.getRationale());
					}
				}
			});
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
			implements IStepValidator, IStepExecutor {
		@Override
		public void setHost(IParticipantHost host) {
			/* do nothing */
		}
		
		private static final Handler INSTANCE = new Handler();
		
		@Override
		public boolean tryValidateChange(Process context, IChange change)
				throws ChangeRejectedException {
			if (change instanceof BoundDescriptor) {
				BoundDescriptor bd = (BoundDescriptor)change;
				try {
					DescriptorExecutorManager.getInstance().tryValidateChange(
							bd.new ProcessWrapper(context),
							bd.getDescriptor());
				} catch (ChangeCreationException cce) {
					throw new ChangeRejectedException(bd, cce.getRationale());
				}
			} else return false;
			return true;
		}
		
		@Override
		public boolean executeChange(IChange change) {
			if (change instanceof BoundDescriptor) {
				BoundDescriptor bd = (BoundDescriptor)change;
				try {
					DescriptorExecutorManager.getInstance().tryApplyChange(
							bd.getResolver(), bd.getDescriptor());
				} catch (ChangeCreationException cce) {
					throw new Error(
							"BUG: BoundDescriptor passed validation " +
							"but couldn't be executed");
				}
			} else return false;
			return true;
		}
	}
	
	static {
		ExecutorManager.getInstance().addHandler(Handler.INSTANCE);
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
	public boolean canInvert() {
		return true;
	}

	@Override
	public void beforeApply() {
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void simulate(PropertyScratchpad context) {
		try {
			getDescriptor().simulate(context, getResolver());
		} catch (ChangeCreationException cce) {
		}
	}
}
