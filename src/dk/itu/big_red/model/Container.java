package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.w3c.dom.Node;

import dk.itu.big_red.model.assistants.CloneMap;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.util.geometry.Rectangle;

/**
 * The <code>Container</code> is the superclass of anything which can contain
 * {@link Layoutable}s: {@link Bigraph}s, {@link Root}s, and {@link Node}s.
 * With the notable exception of {@link Bigraph}s, they can all be moved around
 * and resized.
 * @author alec
 *
 */
public abstract class Container extends Layoutable {
	public class ChangeAddChild extends Change {
		public Container parent;
		public Layoutable child;
		
		public ChangeAddChild(Container parent, Layoutable child) {
			this.parent = parent;
			this.child = child;
		}
		
		@Override
		public Change inverse() {
			return new ChangeRemoveChild(parent, child);
		}
		
		@Override
		public boolean isReady() {
			return (parent != null && child != null);
		}
		
		@Override
		public String toString() {
			return "Change(add child " + child + " to parent " + parent + ")";
		}
	}
	
	public class ChangeRemoveChild extends Change {
		public Container parent;
		public Layoutable child;
		
		public ChangeRemoveChild(Container parent, Layoutable child) {
			this.parent = parent;
			this.child = child;
		}
		
		@Override
		public Change inverse() {
			return new ChangeAddChild(parent, child);
		}
		
		@Override
		public boolean isReady() {
			return (parent != null && child != null);
		}
		
		@Override
		public String toString() {
			return "Change(remove child " + child + " from " + parent + ")";
		}
	}
	
	protected ArrayList<Layoutable> children = new ArrayList<Layoutable>();

	/**
	 * The property name fired when a child is added or removed. The property
	 * values are {@link Layoutable}s.
	 */
	public static final String PROPERTY_CHILD = "ContainerChild";
	
	public boolean canContain(Layoutable child) {
		return false;
	}
	
	protected void addChild(Layoutable child) {
		children.add(child);
		child.setParent(this);
		firePropertyChange(PROPERTY_CHILD, null, child);
	}
	
	protected void removeChild(Layoutable child) {
		boolean removed = children.remove(child);
		if (removed) {
			child.setParent(null);
			firePropertyChange(PROPERTY_CHILD, child, null);
		}
	}
	
	public List<Layoutable> getChildren() {
		return children;
	}

	public boolean hasChild(Layoutable child) {
		return children.contains(child);
	}
	
	@Override
	public Container clone(CloneMap m) {
		Container c = (Container)super.clone(m);
		
		for (Layoutable child : getChildren())
			c.addChild(child.clone(m));
		
		return c;
	}
	
	/**
	 * Creates {@link Change}s which will resize this object to a sensible
	 * default size and resize and reposition all of its children.
	 * @param cg a {@link ChangeGroup} to which changes should be appended
	 * @return the proposed new size of this object
	 */
	@Override
	protected Dimension relayout(ChangeGroup cg) {
		int maxHeight = 0;
		
		HashMap<Layoutable, Dimension> sizes =
				new HashMap<Layoutable, Dimension>();
		
		for (Layoutable i : getChildren()) {
			Dimension childSize = i.relayout(cg);
			sizes.put(i, childSize);
			if (childSize.height > maxHeight)
				maxHeight = childSize.height;
		}
		
		Rectangle nl = new Rectangle();
		
		int width = PADDING;
		
		for (Layoutable i : getChildren()) {
			Rectangle cl =
				new Rectangle().setSize(sizes.get(i));
			cl.setLocation(width,
					PADDING + ((maxHeight - cl.getHeight()) / 2));
			cg.add(i.changeLayout(cl));
			width += cl.getWidth() + PADDING;
		}
		
		if (width < 50)
			width = 50;
		
		Dimension r =
			new Dimension(width, maxHeight + (PADDING * 2));
		cg.add(changeLayout(nl.setSize(r)));
		return r;
	}
	
	public Change changeAddChild(Layoutable child) {
		return new ChangeAddChild(this, child);
	}
	
	public Change changeRemoveChild(Layoutable child) {
		return new ChangeRemoveChild(this, child);
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
		if (name.equals(PROPERTY_CHILD)) {
			return getChildren();
		} else return super.getProperty(name);
	}
}
