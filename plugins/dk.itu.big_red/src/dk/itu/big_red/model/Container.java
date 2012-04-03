package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.assistants.IPropertyProviderProxy;
import dk.itu.big_red.model.assistants.RedProperty;
import dk.itu.big_red.model.changes.ChangeGroup;

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
		public ChangeRemoveChild inverse() {
			return new ChangeRemoveChild(child);
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
	
	public class ChangeRemoveChild extends ContainerChange {
		public Layoutable child;
		
		public ChangeRemoveChild(Layoutable child) {
			this.child = child;
		}
		
		private String oldName = null;
		
		@Override
		public void beforeApply() {
			oldName = child.getName();
		}
		
		@Override
		public boolean canInvert() {
			return (oldName != null);
		}
		
		@Override
		public ChangeAddChild inverse() {
			return new ChangeAddChild(child, oldName);
		}
		
		@Override
		public boolean isReady() {
			return (child != null);
		}
		
		@Override
		public String toString() {
			return "Change(remove child " + child + " from " + getCreator() + ")";
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
	
	protected void removeChild(Layoutable child) {
		if (children.remove(child)) {
			child.setParent(null);
			firePropertyChange(PROPERTY_CHILD, child, null);
		}
	}
	
	public List<Layoutable> getChildren() {
		return children;
	}

	@SuppressWarnings("unchecked")
	public List<Layoutable> getChildren(IPropertyProviderProxy context) {
		return (List<Layoutable>)getProperty(context, PROPERTY_CHILD);
	}
	
	@Override
	public Container clone(Map<ModelObject, ModelObject> m) {
		Container c = (Container)super.clone(m);
		
		for (Layoutable child : getChildren())
			c.addChild(child.clone(m));
		
		return c;
	}
	
	/**
	 * Creates {@link ContainerChange}s which will resize this object to a sensible
	 * default size and resize and reposition all of its children.
	 * @param cg a {@link ChangeGroup} to which changes should be appended
	 * @return the proposed new size of this object
	 */
	@Override
	protected Dimension relayout(IPropertyProviderProxy context, ChangeGroup cg) {
		int maxHeight = 0;
		
		HashMap<Layoutable, Dimension> sizes =
				new HashMap<Layoutable, Dimension>();
		
		for (Layoutable i : getChildren(context)) {
			Dimension childSize = i.relayout(context, cg);
			sizes.put(i, childSize);
			if (childSize.height > maxHeight)
				maxHeight = childSize.height;
		}
		
		Rectangle nl = new Rectangle();
		
		int width = PADDING;
		
		for (Layoutable i : getChildren(context)) {
			Rectangle cl = new Rectangle().setSize(sizes.get(i));
			cl.setLocation(width,
					PADDING + ((maxHeight - cl.height()) / 2));
			cg.add(i.changeLayout(cl));
			width += cl.width() + PADDING;
		}
		
		if (width < 50)
			width = 50;
		
		Dimension r =
			new Dimension(width, maxHeight + (PADDING * 2));
		cg.add(changeLayout(nl.setSize(r)));
		return r;
	}
	
	public ContainerChange changeAddChild(Layoutable child, String name) {
		return new ChangeAddChild(child, name);
	}
	
	public ContainerChange changeRemoveChild(Layoutable child) {
		return new ChangeRemoveChild(child);
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
	public Object getProperty(String name) {
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
	protected <V> ArrayList<V> only(IPropertyProviderProxy context, Class<V> klass) {
		ArrayList<V> r = new ArrayList<V>();
		for (Layoutable i : getChildren(context))
			if (klass.isInstance(i))
				r.add((V)i);
		return r;
	}
}
