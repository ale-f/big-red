package org.bigraph.model;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

public abstract class NamedModelObject extends ModelObject {
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String PROPERTY_NAME = "NamedModelObjectName";
	
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
		
		protected static <T> T require(Object o, Class<? extends T> klass) {
			return (klass.isInstance(o) ? klass.cast(o) : null);
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
	
	public static class ChangeNameDescriptor implements IChangeDescriptor {
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
		public IChange createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			NamedModelObject o = target.lookup(context, r);
			if (o == null)
				throw new ChangeCreationException(this,
						"" + target + " didn't resolve to a NamedModelObject");
			return o.changeName(newName);
		}
		
		@Override
		public String toString() {
			return "ChangeDescriptor(set name of " + target + " to " + 
					newName + ")";
		}
	}

	private String name;
	
	public String getName() {
		return name;
	}
	
	public String getName(PropertyScratchpad context) {
		return (String)getProperty(context, PROPERTY_NAME);
	}
	
	protected void setName(String name) {
		String oldName = this.name;
		this.name = name;
		firePropertyChange(PROPERTY_NAME, oldName, name);
	}
	
	public abstract IChange changeName(String newName);
	
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
}