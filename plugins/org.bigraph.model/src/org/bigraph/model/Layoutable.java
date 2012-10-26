package org.bigraph.model;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.names.Namespace;

/**
 * All of the objects which can actually appear as part of a bigraph are
 * instances of <strong>Layoutable</strong>.
 * @author alec
 * @see ModelObject
 */
public abstract class Layoutable extends NamedModelObject {
	/**
	 * The property name fired when the parent changes.
	 */
	@RedProperty(fired = Container.class, retrieved = Container.class)
	public static final String PROPERTY_PARENT = "LayoutableParent";
	
	abstract class LayoutableChange extends ModelObjectChange {
		@Override
		public Layoutable getCreator() {
			return Layoutable.this;
		}
	}

	@Override
	protected Namespace<Layoutable> getGoverningNamespace(
			PropertyScratchpad context) {
		return getBigraph(context).getNamespace(this);
	}
	
	@Override
	protected void applyRename(String name) {
		setName(getGoverningNamespace(null).rename(getName(), name));
	}
	
	@Override
	protected void simulateRename(PropertyScratchpad context, String name) {
		context.setProperty(this, PROPERTY_NAME,
				getGoverningNamespace(context).rename(
						context, getName(context), name));
	}
	
	public final class ChangeRemove extends LayoutableChange {
		private String oldName;
		private Container oldParent;
		private int oldPosition;
		@Override
		public void beforeApply() {
			Layoutable l = getCreator();
			oldName = l.getName();
			oldParent = l.getParent();
			oldPosition = l.getParent().getChildren().indexOf(l);
		}
		
		@Override
		public boolean canInvert() {
			return (oldName != null && oldParent != null);
		}
		
		@Override
		public Change inverse() {
			return oldParent.new ChangeAddChild(
					getCreator(), oldName, oldPosition);
		}
		
		@Override
		public String toString() {
			return "Change(remove child " + getCreator() + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			Layoutable l = getCreator();
			Container c = l.getParent(context);
			
			context.<Layoutable>getModifiableList(
					c, Container.PROPERTY_CHILD, c.getChildren()).
				remove(l);
			context.setProperty(l, Layoutable.PROPERTY_PARENT, null);
			
			c.getBigraph(context).getNamespace(l).
					remove(context, l.getName(context));
			context.setProperty(l, Layoutable.PROPERTY_NAME, null);
		}
	}
	
	private Container parent = null;
	
	/**
	 * Returns the {@link Bigraph} that ultimately contains this object.
	 * @return a Bigraph
	 */
	public Bigraph getBigraph() {
		return getBigraph(null);
	}

	public Bigraph getBigraph(PropertyScratchpad context) {
		if (getParent(context) == null) {
			return null;
		} else return getParent(context).getBigraph(context);
	}
	
	/**
	 * Returns the parent of this object.
	 * @return an {@link Container}
	 */
	public Container getParent() {
		return parent;
	}

	public Container getParent(PropertyScratchpad context) {
		return (Container)getProperty(context, PROPERTY_PARENT);
	}
	
	/**
	 * Changes the parent of this object.
	 * @param p the new parent {@link Container}
	 */
	void setParent(Container parent) {
		Container oldParent = this.parent;
		this.parent = parent;
		firePropertyChange(PROPERTY_PARENT, oldParent, parent);
	}
	
	protected Layoutable clone(Bigraph m) {
		Layoutable l = (Layoutable)super.clone();
		m.getNamespace(l).put(getName(), l);
		return l;
	}
	
	@Override
	public IChange changeName(String newName) {
		return new ChangeName(newName);
	}
	
	public IChange changeRemove() {
		return new ChangeRemove();
	}
	
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_PARENT.equals(name)) {
			return getParent();
		} else return super.getProperty(name);
	}
	
	@Override
	public void dispose() {
		parent = null;
		
		super.dispose();
	}
	
	public abstract Identifier getIdentifier();
	public abstract Identifier getIdentifier(PropertyScratchpad context);
	
	public static abstract class Identifier
			extends NamedModelObject.Identifier {
		public Identifier(String name) {
			super(name);
		}
		
		@Override
		public abstract Layoutable lookup(
				PropertyScratchpad context, Resolver r);
		
		@Override
		public abstract Identifier getRenamed(String name);
	}
	
	public static class ChangeRemoveDescriptor implements IChangeDescriptor {
		private final Identifier target;
		private final Container.Identifier parent;
		
		public ChangeRemoveDescriptor(Identifier target) {
			this.target = target;
			parent = null;
		}
		
		public ChangeRemoveDescriptor(
				Identifier target, Container.Identifier parent) {
			this.target = target;
			this.parent = parent;
		}
		
		public Identifier getTarget() {
			return target;
		}
		
		public Container.Identifier getParent() {
			return parent;
		}
		
		@Override
		public boolean equals(Object obj_) {
			return safeClassCmp(this, obj_) &&
					safeEquals(getTarget(),
							((ChangeRemoveDescriptor)obj_).getTarget());
		}
		
		@Override
		public int hashCode() {
			return compositeHashCode(ChangeRemoveDescriptor.class, target);
		}
		
		@Override
		public IChange createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			Layoutable l = target.lookup(context, r);
			if (l == null)
				throw new ChangeCreationException(this,
						"" + target + " didn't resolve to a Layoutable");
			return l.changeRemove();
		}
		
		@Override
		public String toString() {
			return "ChangeDescriptor(remove child " + target + ")";
		}
	}
}
