package org.bigraph.model;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.experimental.BoundDescriptor;
import org.bigraph.model.changes.descriptors.experimental.DescriptorExecutorManager;

public class Edit extends ModelObject
		implements IChangeDescriptor, IChangeExecutor,
		ModelObject.Identifier.Resolver {
	@RedProperty(
			fired = IChangeDescriptor.class,
			retrieved = ChangeDescriptorGroup.class)
	public static final String PROPERTY_DESCRIPTOR = "EditDescriptor";
	
	private ChangeDescriptorGroup cdg = new ChangeDescriptorGroup();
	
	public ChangeDescriptorGroup getDescriptors() {
		return cdg;
	}
	
	public ChangeDescriptorGroup getDescriptors(PropertyScratchpad context) {
		return getProperty(
				context, PROPERTY_DESCRIPTOR, ChangeDescriptorGroup.class);
	}
	
	protected ChangeDescriptorGroup getModifiableDescriptors(
			PropertyScratchpad context) {
		if (context.hasProperty(this, PROPERTY_DESCRIPTOR)) {
			return (ChangeDescriptorGroup)
					context.getProperty(this, PROPERTY_DESCRIPTOR);
		} else {
			ChangeDescriptorGroup cdg = getDescriptors().clone();
			context.setProperty(this, PROPERTY_DESCRIPTOR, cdg);
			return cdg;
		}
	}
	
	protected void addDescriptor(int index, IChangeDescriptor cd) {
		cdg.add(index, cd);
		firePropertyChange(PROPERTY_DESCRIPTOR, null, cd);
	}
	
	protected void removeDescriptor(int index) {
		IChangeDescriptor cd = cdg.remove(index);
		firePropertyChange(PROPERTY_DESCRIPTOR, cd, null);
	}
	
	@Override
	public IChange createChange(PropertyScratchpad context, Resolver r)
			throws ChangeCreationException {
		return cdg.createChange(context, r);
	}

	@Override
	public void tryValidateChange(IChange b) throws ChangeRejectedException {
		ExecutorManager.getInstance().tryValidateChange(b);
	}
	
	@Override
	public void tryApplyChange(IChange b) throws ChangeRejectedException {
		ExecutorManager.getInstance().tryApplyChange(b);
	}
	
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_DESCRIPTOR.equals(name)) {
			return getDescriptors();
		} else return super.getProperty(name);
	}
	
	public IChange changeDescriptorAdd(
			int position, IChangeDescriptor descriptor) {
		return new BoundDescriptor(this,
				new ChangeDescriptorAddDescriptor(
						new Edit.Identifier(), position, descriptor));
	}
	
	public IChange changeDescriptorRemove(
			int position, IChangeDescriptor descriptor) {
		return new BoundDescriptor(this,
				new ChangeDescriptorRemoveDescriptor(
						new Edit.Identifier(), position, descriptor));
	}
	
	@Override
	public void simulate(PropertyScratchpad context, Resolver r)
			throws ChangeCreationException {
		context.executeChange(createChange(context, r));
	}
	
	@Override
	public Edit inverse() {
		Edit ed = (Edit)super.clone();
		ed.cdg = cdg.inverse();
		return ed;
	}
	
	@Override
	public void dispose() {
		if (cdg != null) {
			cdg.clear();
			cdg = null;
		}
		
		super.dispose();
	}
	
	public static final class Identifier implements ModelObject.Identifier {
		@Override
		public Edit lookup(PropertyScratchpad context, Resolver r) {
			return require(r.lookup(context, this), Edit.class);
		}
	}
	
	abstract static class EditChangeDescriptor
			extends ModelObjectChangeDescriptor {
		static {
			DescriptorExecutorManager.getInstance().addHandler(
					new EditDescriptorHandler());
		}
		
		@Override
		public IChange createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			return new BoundDescriptor(r, this);
		}
	}
	
	public static class ChangeDescriptorAddDescriptor
			extends EditChangeDescriptor {
		private final Identifier target;
		private final int position;
		private final IChangeDescriptor descriptor;
		
		public ChangeDescriptorAddDescriptor(
				Identifier target, int position,
				IChangeDescriptor descriptor) {
			this.target = target;
			this.position = position;
			this.descriptor = descriptor;
		}

		public Identifier getTarget() {
			return target;
		}
		
		public int getPosition() {
			return position;
		}
		
		public IChangeDescriptor getDescriptor() {
			return descriptor;
		}

		@Override
		public IChangeDescriptor inverse() {
			return new ChangeDescriptorRemoveDescriptor(
					getTarget(), getPosition(), getDescriptor());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			Edit self = getTarget().lookup(context, r);
			self.getModifiableDescriptors(context).add(
					getPosition(), getDescriptor());
		}
	}
	
	public static class ChangeDescriptorRemoveDescriptor
			extends EditChangeDescriptor {
		private final Identifier target;
		private final int position;
		private final IChangeDescriptor descriptor;

		public ChangeDescriptorRemoveDescriptor(
				Identifier target, int position,
				IChangeDescriptor descriptor) {
			this.target = target;
			this.position = position;
			this.descriptor = descriptor;
		}

		public Identifier getTarget() {
			return target;
		}

		public int getPosition() {
			return position;
		}
		
		public IChangeDescriptor getDescriptor() {
			return descriptor;
		}

		@Override
		public IChangeDescriptor inverse() {
			return new ChangeDescriptorAddDescriptor(
					getTarget(), getPosition(), getDescriptor());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			Edit self = getTarget().lookup(context, r);
			self.getModifiableDescriptors(context).remove(getDescriptor());
		}
	}

	@Override
	public Object lookup(PropertyScratchpad context,
			ModelObject.Identifier identifier) {
		if (identifier instanceof Identifier) {
			return this;
		} else return null;
	}
}
