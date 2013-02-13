package org.bigraph.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.utilities.FilteringIterable;

/**
 * The <code>Container</code> is the superclass of anything which can contain
 * {@link Layoutable}s: {@link Bigraph}s, {@link Root}s, and {@link Node}s.
 * With the notable exception of {@link Bigraph}s, they can all be moved around
 * and resized.
 * @author alec
 */
public abstract class Container extends Layoutable {
	/**
	 * The property name fired when a child is added or removed.
	 */
	@RedProperty(fired = Layoutable.class, retrieved = List.class)
	public static final String PROPERTY_CHILD = "ContainerChild";
	
	abstract class ContainerChange extends LayoutableChange {
		@Override
		public Container getCreator() {
			return (Container)super.getCreator();
		}
	}
	
	abstract static class ContainerChangeDescriptor
			extends LayoutableChangeDescriptor {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(new ContainerDescriptorHandler());
		}
	}
	
	public final class ChangeAddChild extends ContainerChange {
		public final Layoutable child;
		public final String name;
		
		public ChangeAddChild(Layoutable child, String name) {
			this.child = child;
			this.name = name;
		}
		
		@Override
		public IChange inverse() {
			return child.new ChangeRemove();
		}
		
		@Override
		public String toString() {
			return "Change(add child " + child + " to parent " + 
					getCreator() + " with name \"" + name + "\")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver resolver) {
			Set<Layoutable> children = context.<Layoutable>getModifiableSet(
					getCreator(), Container.PROPERTY_CHILD, getChildren());
			children.add(child);
			context.setProperty(child,
					Layoutable.PROPERTY_PARENT, getCreator());
			
			getCreator().getBigraph(context).getNamespace(child).
					put(context, name, child);
			context.setProperty(child, Layoutable.PROPERTY_NAME, name);
		}
	}
	
	static {
		DescriptorExecutorManager.getInstance().addParticipant(new ContainerHandler());
	}
	
	protected HashSet<Layoutable> children = new HashSet<Layoutable>();
	
	protected void addChild(Layoutable child) {
		children.add(child);
		child.setParent(this);
		firePropertyChange(PROPERTY_CHILD, null, child);
	}
	
	protected void removeChild(Layoutable child) {
		if (children.remove(child)) {
			child.setParent(null);
			firePropertyChange(PROPERTY_CHILD, child, null);
		}
	}
	
	public Collection<? extends Layoutable> getChildren() {
		return children;
	}

	@SuppressWarnings("unchecked")
	public Collection<? extends Layoutable> getChildren(
			PropertyScratchpad context) {
		return getProperty(context, PROPERTY_CHILD, Collection.class);
	}
	
	@Override
	protected Container clone(Bigraph m) {
		Container c = (Container)super.clone(m);
		
		for (Layoutable child : getChildren())
			c.addChild(child.clone(m));
		
		return c;
	}
	
	public IChange changeAddChild(Layoutable child, String name) {
		return new ChangeAddChild(child, name);
	}
	
	/**
	 * {@inheritDoc}
	 * <p><strong>Special notes for {@link Container}:</strong>
	 * <ul>
	 * <li>Passing {@link #PROPERTY_CHILD} will return a {@link List}&lt;{@link
	 * Layoutable}&gt;, <strong>not</strong> a {@link Layoutable}.
	 * </ul>
	 */
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_CHILD.equals(name)) {
			return getChildren();
		} else return super.getProperty(name);
	}
	
	@Override
	public void dispose() {
		if (children != null) {
			for (Layoutable i : children)
				i.dispose();
			children.clear();
			children = null;
		}
		
		super.dispose();
	}

	/**
	 * Returns the children of this {@link Container} which are instances of
	 * the given {@link Class}.
	 * @param context TODO
	 * @param klass the {@link Class} to filter by
	 * @return a {@link List} of children of the given {@link Class}
	 */
	protected <V> ArrayList<V> only(
			PropertyScratchpad context, Class<V> klass) {
		ArrayList<V> r = new ArrayList<V>();
		for (V i : new FilteringIterable<V>(klass, getChildren(context)))
			r.add(i);
		return r;
	}
	
	public static abstract class Identifier extends Layoutable.Identifier {
		public Identifier(String name) {
			super(name);
		}
		
		@Override
		public abstract Container lookup(
				PropertyScratchpad context, Resolver r);
		
		@Override
		public abstract Identifier getRenamed(String name);
	}
	
	@Override
	public abstract Identifier getIdentifier();
	@Override
	public abstract Identifier getIdentifier(PropertyScratchpad context);
	
	public static final class ChangeAddChildDescriptor
			extends ContainerChangeDescriptor {
		private final Identifier parent;
		private final Layoutable.Identifier child;
		
		public ChangeAddChildDescriptor(
				Identifier parent, Layoutable.Identifier child) {
			this.parent = parent;
			this.child = child;
		}
		
		public Identifier getParent() {
			return parent;
		}
		
		public Layoutable.Identifier getChild() {
			return child;
		}

		@Override
		public boolean equals(Object obj_) {
			if (safeClassCmp(this, obj_)) {
				ChangeAddChildDescriptor obj = (ChangeAddChildDescriptor)obj_;
				return
						safeEquals(getParent(), obj.getParent()) &&
						safeEquals(getChild(), obj.getChild());
			} else return false;
		}
		
		@Override
		public int hashCode() {
			return compositeHashCode(
					ChangeAddChildDescriptor.class, parent, child);
		}
		
		@Override
		public String toString() {
			return "ChangeDescriptor(add child " + child + " to parent " + 
					parent + ")";
		}
		
		@Override
		public ChangeRemoveChildDescriptor inverse() {
			return new ChangeRemoveChildDescriptor(getParent(), getChild());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			Container self = getParent().lookup(context, r);
			Layoutable child = ContainerDescriptorHandler.instantiate(
					getChild(), context, r);
			
			Set<Layoutable> children = context.<Layoutable>getModifiableSet(
					self, Container.PROPERTY_CHILD, self.getChildren());
			children.add(child);
			context.setProperty(child, Layoutable.PROPERTY_PARENT, self);
			
			String name = getChild().getName();
			self.getBigraph(context).getNamespace(child).
					put(context, name, child);
			context.setProperty(child, Layoutable.PROPERTY_NAME, name);
		}
	}

	public static final class ChangeRemoveChildDescriptor
			extends ContainerChangeDescriptor {
		private final Identifier parent;
		private final Layoutable.Identifier child;
		
		public ChangeRemoveChildDescriptor(
				Identifier parent, Layoutable.Identifier child) {
			this.child = child;
			this.parent = parent;
		}
		
		public Layoutable.Identifier getChild() {
			return child;
		}
		
		public Identifier getParent() {
			return parent;
		}
		
		@Override
		public boolean equals(Object obj_) {
			if (safeClassCmp(this, obj_)) {
				ChangeRemoveChildDescriptor obj =
						(ChangeRemoveChildDescriptor)obj_;
				return
						safeEquals(getChild(), obj.getChild()) &&
						safeEquals(getParent(), obj.getParent());
			} else return false;
		}
		
		@Override
		public int hashCode() {
			return compositeHashCode(ChangeRemoveChildDescriptor.class,
					child, parent);
		}
		
		@Override
		public ChangeAddChildDescriptor inverse() {
			return new ChangeAddChildDescriptor(getParent(), getChild());
		}
		
		@Override
		public String toString() {
			return "ChangeDescriptor(remove child " + child + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			Container self = getParent().lookup(context, r);
			Layoutable child = getChild().lookup(context, r);
			
			context.<Layoutable>getModifiableSet(
					self, PROPERTY_CHILD, self.getChildren()).remove(child);
			context.setProperty(child, Layoutable.PROPERTY_PARENT, null);
			
			self.getBigraph(context).getNamespace(child).
					remove(context, getChild().getName());
			context.setProperty(child, Layoutable.PROPERTY_NAME, null);
		}
	}
}
