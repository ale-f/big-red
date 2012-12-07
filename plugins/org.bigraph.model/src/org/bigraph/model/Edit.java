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
	
	protected abstract class EditChange extends ModelObjectChange {
		@Override
		public Edit getCreator() {
			return Edit.this;
		}
	}
	
	public final class ChangeDescriptorAdd extends EditChange {
		public final int index;
		public final IChangeDescriptor descriptor;
		
		public ChangeDescriptorAdd(int index, IChangeDescriptor descriptor) {
			this.index = index;
			this.descriptor = descriptor;
		}
		
		@Override
		public ChangeDescriptorRemove inverse() {
			return new ChangeDescriptorRemove(index, descriptor);
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			getCreator().getModifiableDescriptors(context).add(index, descriptor);
		}
		
		@Override
		public String toString() {
			return "Change(add descriptor " + descriptor + " at index " +
					index + " to " + getCreator() + ")";
		}
	}
	
	public final class ChangeDescriptorRemove extends EditChange {
		public final int index;
		public final IChangeDescriptor descriptor;
		
		public ChangeDescriptorRemove(
				int index, IChangeDescriptor descriptor) {
			this.index = index;
			this.descriptor = descriptor;
		}
		
		@Override
		public ChangeDescriptorAdd inverse() {
			return new ChangeDescriptorAdd(index, descriptor);
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			getCreator().getModifiableDescriptors(context).remove(index);
		}
		
		@Override
		public String toString() {
			return "Change(remove descriptor " + descriptor + " at index " +
					index + " from " + getCreator() + ")";
		}
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
	
	static {
		ExecutorManager.getInstance().addHandler(new EditHandler());
		DescriptorExecutorManager.getInstance().addHandler(
				new EditDescriptorHandler());
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
	
	public IChange changeDescriptorAdd(int index,
			IChangeDescriptor descriptor) {
		return new ChangeDescriptorAdd(index, descriptor);
	}
	
	public IChange changeDescriptorRemove(int index,
			IChangeDescriptor descriptor) {
		return new ChangeDescriptorRemove(index, descriptor);
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
			return NamedModelObject.Identifier.require(r.lookup(context, this),
					Edit.class);
		}
	}
	
	abstract static class EditChangeDescriptor
			extends ModelObjectChangeDescriptor {
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
		public IChange createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			Edit ed = getTarget().lookup(context, r);
			if (ed == null)
				throw new ChangeCreationException(this,
						"Couldn't resolve " + getTarget() + " to an Edit");
			return ed.changeDescriptorAdd(getPosition(), getDescriptor());
		}

		@Override
		public IChangeDescriptor inverse() {
			return new ChangeDescriptorRemoveDescriptor(
					getTarget(), getPosition(), getDescriptor());
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
		public IChange createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			Edit ed = getTarget().lookup(context, r);
			if (ed == null)
				throw new ChangeCreationException(this,
						"Couldn't resolve " + getTarget() + " to an Edit");
			return ed.changeDescriptorRemove(getPosition(), getDescriptor());
		}

		@Override
		public IChangeDescriptor inverse() {
			return new ChangeDescriptorAddDescriptor(
					getTarget(), getPosition(), getDescriptor());
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
