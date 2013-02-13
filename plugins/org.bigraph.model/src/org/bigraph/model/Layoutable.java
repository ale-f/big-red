package org.bigraph.model;

import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
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
	
	abstract class LayoutableChange implements IChange {
		public Layoutable getCreator() {
			return Layoutable.this;
		}
	}

	abstract static class LayoutableChangeDescriptor
			extends NamedModelObjectChangeDescriptor {
	}
	
	@Override
	protected Namespace<Layoutable> getGoverningNamespace(
			PropertyScratchpad context) {
		return getBigraph(context).getNamespace(this);
	}
	
	public final class ChangeRemove extends LayoutableChange {
		String oldName;
		Container oldParent;
		
		@Override
		public IChange inverse() {
			return oldParent.new ChangeAddChild(getCreator(), oldName);
		}
		
		@Override
		public String toString() {
			return "Change(remove child " + getCreator() + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver resolver) {
			Layoutable l = getCreator();
			Container c = l.getParent(context);
			
			context.<Layoutable>getModifiableSet(
					c, Container.PROPERTY_CHILD, c.getChildren()).
				remove(l);
			context.setProperty(l, Layoutable.PROPERTY_PARENT, null);
			
			c.getBigraph(context).getNamespace(l).
					remove(context, l.getName(context));
			context.setProperty(l, Layoutable.PROPERTY_NAME, null);
		}
	}
	
	static {
		DescriptorExecutorManager.getInstance().addParticipant(new LayoutableHandler());
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
		return getProperty(context, PROPERTY_PARENT, Container.class);
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
	
	@Override
	public abstract Identifier getIdentifier();
	@Override
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
}
