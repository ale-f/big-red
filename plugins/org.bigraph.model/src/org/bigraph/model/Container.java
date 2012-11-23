package org.bigraph.model;

import java.util.ArrayList;
import java.util.List;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

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
	
	public final class ChangeAddChild extends ContainerChange {
		public final Layoutable child;
		public final String name;
		protected final int position;
		
		public ChangeAddChild(Layoutable child, String name) {
			this(child, name, -1);
		}
		
		protected ChangeAddChild(Layoutable child, String name, int position) {
			this.child = child;
			this.name = name;
			this.position = position;
		}
		
		@Override
		public Change inverse() {
			return child.new ChangeRemove();
		}
		
		@Override
		public boolean isReady() {
			return (child != null && name != null);
		}
		
		@Override
		public String toString() {
			return "Change(add child " + child + " to parent " + 
					getCreator() + " with name \"" + name + "\")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			List<Layoutable> children = context.<Layoutable>getModifiableList(
					getCreator(), Container.PROPERTY_CHILD, getChildren());
			if (position == -1) {
				children.add(child);
			} else children.add(position, child);
			context.setProperty(child,
					Layoutable.PROPERTY_PARENT, getCreator());
			
			getCreator().getBigraph(context).getNamespace(child).
					put(context, name, child);
			context.setProperty(child, Layoutable.PROPERTY_NAME, name);
		}
	}
	
	static {
		ExecutorManager.getInstance().addHandler(new ContainerHandler());
	}
	
	protected ArrayList<Layoutable> children = new ArrayList<Layoutable>();
	
	protected void addChild(int position, Layoutable child) {
		if (position == -1) {
			children.add(child);
		} else children.add(position, child);
		child.setParent(this);
		firePropertyChange(PROPERTY_CHILD, null, child);
	}
	
	protected void removeChild(Layoutable child) {
		if (children.remove(child)) {
			child.setParent(null);
			firePropertyChange(PROPERTY_CHILD, child, null);
		}
	}
	
	public List<? extends Layoutable> getChildren() {
		return children;
	}

	@SuppressWarnings("unchecked")
	public List<? extends Layoutable> getChildren(PropertyScratchpad context) {
		return getProperty(context, PROPERTY_CHILD, List.class);
	}
	
	@Override
	protected Container clone(Bigraph m) {
		Container c = (Container)super.clone(m);
		
		for (Layoutable child : getChildren())
			c.addChild(-1, child.clone(m));
		
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
	@SuppressWarnings("unchecked")
	protected <V> ArrayList<V> only(PropertyScratchpad context, Class<V> klass) {
		ArrayList<V> r = new ArrayList<V>();
		for (Layoutable i : getChildren(context))
			if (klass.isInstance(i))
				r.add((V)i);
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
	
	public static class ChangeAddChildDescriptor implements IChangeDescriptor {
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
		public IChange createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			Container c = parent.lookup(context, r);
			if (c != null) {
				Layoutable l = null;
				if (child instanceof Root.Identifier) {
					l = new Root();
				} else if (child instanceof Site.Identifier) {
					l = new Site();
				} else if (child instanceof InnerName.Identifier) {
					l = new InnerName();
				} else if (child instanceof Edge.Identifier) {
					l = new Edge();
				} else if (child instanceof OuterName.Identifier) {
					l = new OuterName();
				} else if (child instanceof Node.Identifier) {
					Node.Identifier id = (Node.Identifier)child;
					/* There shouldn't be any changes to the signature in this
					 * context */
					l = new Node(id.getControl().lookup(null, r));
				}
				if (l != null) {
					return c.changeAddChild(l, child.getName());
				} else throw new ChangeCreationException(this,
						"Couldn't create a new child object from " + child);
			} else throw new ChangeCreationException(this,
					"" + parent + " didn't resolve to a Container");
		}
		
		@Override
		public String toString() {
			return "ChangeDescriptor(add child " + child + " to parent " + 
					parent + ")";
		}
		
		@Override
		public ChangeRemoveDescriptor inverse() {
			return new ChangeRemoveDescriptor(getChild(), getParent());
		}
	}
}
