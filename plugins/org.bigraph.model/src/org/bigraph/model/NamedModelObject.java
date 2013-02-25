package org.bigraph.model;

import java.util.Collection;
import java.util.Comparator;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.names.Namespace;
import org.bigraph.model.utilities.CollectionUtilities;
import org.bigraph.model.utilities.comparators.ComparatorUtilities;
import org.bigraph.model.utilities.comparators.ComparatorUtilities.Converter;

public abstract class NamedModelObject extends ModelObject {
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String PROPERTY_NAME = "NamedModelObjectName";
	
	abstract static class NamedModelObjectChangeDescriptor
			extends ModelObjectChangeDescriptor {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(new NamedModelObjectDescriptorHandler());
		}
	}
	
	protected abstract Namespace<? extends NamedModelObject>
			getGoverningNamespace(PropertyScratchpad context);
	
	protected void applyRename(String name) {
		setName(getGoverningNamespace(null).rename(getName(), name));
	}
	
	protected void simulateRename(PropertyScratchpad context, String name) {
		context.setProperty(this, PROPERTY_NAME,
				getGoverningNamespace(context).rename(
						context, getName(context), name));
	}
	
	public static abstract class Identifier implements ModelObject.Identifier {
		private final String name;
		
		public Identifier(String name) {
			this.name = name;
		}
		
		/**
		 * Returns this {@link Identifier}'s name.
		 * @return a name; can be <code>null</code>
		 */
		public String getName() {
			return name;
		}
		
		@Override
		public boolean equals(Object obj_) {
			return safeClassCmp(this, obj_) &&
					safeEquals(getName(), ((Identifier)obj_).getName());
		}
		
		@Override
		public int hashCode() {
			return compositeHashCode(getClass(), getName());
		}
		
		@Override
		public abstract NamedModelObject lookup(
				PropertyScratchpad context, Resolver r);
		
		/**
		 * Returns a copy of this {@link Identifier} with a different name.
		 * (Other identifying properties, if there are any, will not be
		 * changed.)
		 * @param name a new name
		 * @return a renamed copy of this {@link Identifier}
		 */
		public abstract Identifier getRenamed(String name);
	}
	
	@Override
	public abstract Identifier getIdentifier();
	@Override
	public abstract Identifier getIdentifier(PropertyScratchpad context);
	
	public static final class ChangeNameDescriptor
			extends NamedModelObjectChangeDescriptor {
		private final Identifier target;
		private final String newName;
		
		public ChangeNameDescriptor(Identifier target, String newName) {
			this.target = target;
			this.newName = newName;
		}
		
		public Identifier getTarget() {
			return target;
		}
		
		public String getNewName() {
			return newName;
		}
		
		@Override
		public boolean equals(Object obj_) {
			if (safeClassCmp(this, obj_)) {
				ChangeNameDescriptor obj = (ChangeNameDescriptor)obj_;
				return
						safeEquals(getTarget(), obj.getTarget()) &&
						safeEquals(getNewName(), obj.getNewName());
			} else return false;
		}
		
		@Override
		public int hashCode() {
			return compositeHashCode(
					ChangeNameDescriptor.class, target, newName);
		}
		
		@Override
		public ChangeNameDescriptor inverse() {
			return new ChangeNameDescriptor(
					getTarget().getRenamed(getNewName()),
					getTarget().getName());
		}
		
		@Override
		public String toString() {
			return "ChangeDescriptor(set name of " + target + " to " + 
					newName + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			NamedModelObject self = getTarget().lookup(context, r);
			self.simulateRename(context, getNewName());
		}
	}

	private String name;
	
	public String getName() {
		return name;
	}
	
	public String getName(PropertyScratchpad context) {
		return getProperty(context, PROPERTY_NAME, String.class);
	}
	
	protected void setName(String name) {
		String oldName = this.name;
		this.name = name;
		firePropertyChange(PROPERTY_NAME, oldName, name);
	}
	
	@Override
	protected NamedModelObject clone() {
		NamedModelObject o = (NamedModelObject)super.clone();
		o.setName(getName());
		return o;
	}
	
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_NAME.equals(name)) {
			return getName();
		} else return super.getProperty(name);
	}
	
	@Override
	public void dispose() {
		name = null;
		
		super.dispose();
	}
	
	public final String toString(PropertyScratchpad context) {
		return getIdentifier(context).toString();
	}
	
	public static final <T extends NamedModelObject>
	Collection<? extends T> order(
			Iterable<? extends T> c, Comparator<String> cmp) {
		return order(null, c, cmp);
	}
	
	public static final <T extends NamedModelObject>
	Collection<? extends T> order(final PropertyScratchpad context,
			Iterable<? extends T> c, Comparator<String> cmp) {
		return CollectionUtilities.collect(c,
				ComparatorUtilities.convertComparator(
						new Converter<NamedModelObject, String>() {
							@Override
							public String convert(NamedModelObject object) {
								return object.getName(context);
							}
						}, cmp));
	}
}
