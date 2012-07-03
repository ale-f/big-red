package org.bigraph.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.Change;

/**
 * The <code>Container</code> is the superclass of anything which can contain
 * {@link Layoutable}s: {@link Bigraph}s, {@link Root}s, and {@link Node}s.
 * With the notable exception of {@link Bigraph}s, they can all be moved around
 * and resized.
 * @author alec
 *
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
	
	public class ChangeAddChild extends ContainerChange {
		public Layoutable child;
		public String name;
		
		public ChangeAddChild(Layoutable child, String name) {
			this.child = child;
			this.name = name;
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
			return "Change(add child " + child + " to parent " + getCreator() + " with name \"" + name + "\")";
		}
	}
	
	protected ArrayList<Layoutable> children = new ArrayList<Layoutable>();
	
	public abstract boolean canContain(Layoutable child);
	
	protected void addChild(Layoutable child) {
		if (children.add(child)) {
			child.setParent(this);
			firePropertyChange(PROPERTY_CHILD, null, child);
		}
	}
	
	public void addChild(
			PropertyScratchpad context, Layoutable child, String name) {
		context.<Layoutable>getModifiableList(
				this, Container.PROPERTY_CHILD, getChildren()).
			add(child);
		context.setProperty(child, Layoutable.PROPERTY_PARENT, this);
		
		getBigraph(context).getNamespace(Bigraph.getNSI(child)).
			put(context, name, child);
		context.setProperty(child, Layoutable.PROPERTY_NAME, name);
	}
	
	protected void removeChild(Layoutable child) {
		if (children.remove(child)) {
			child.setParent(null);
			firePropertyChange(PROPERTY_CHILD, child, null);
		}
	}
	
	public void removeChild(PropertyScratchpad context, Layoutable child) {
		context.<Layoutable>getModifiableList(
				this, Container.PROPERTY_CHILD, getChildren()).
			remove(child);
		context.setProperty(child, Layoutable.PROPERTY_PARENT, null);
		
		getBigraph(context).getNamespace(Bigraph.getNSI(child)).
			remove(context, child.getName(context));
		context.setProperty(child, Layoutable.PROPERTY_NAME, null);
	}
	
	public List<Layoutable> getChildren() {
		return children;
	}

	@SuppressWarnings("unchecked")
	public List<Layoutable> getChildren(PropertyScratchpad context) {
		return (List<Layoutable>)getProperty(context, PROPERTY_CHILD);
	}
	
	@Override
	public Container clone(Map<ModelObject, ModelObject> m) {
		Container c = (Container)super.clone(m);
		
		for (Layoutable child : getChildren())
			c.addChild(child.clone(m));
		
		return c;
	}
	
	public ContainerChange changeAddChild(Layoutable child, String name) {
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
		for (Layoutable i : children)
			i.dispose();
		children.clear();
		
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
}
