package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.w3c.dom.Node;

import dk.itu.big_red.model.assistants.CloneMap;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeAddChild;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeRemoveChild;
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
	protected ArrayList<Layoutable> children = new ArrayList<Layoutable>();

	/**
	 * The property name fired when a child is added or removed.
	 */
	public static final String PROPERTY_CHILD = "ContainerChild";
	
	public Container() {
		layout = new Rectangle(10, 10, 100, 100);
	}
	
	public boolean canContain(Layoutable child) {
		return false;
	}
	
	protected void addChild(Layoutable child) {
		boolean added = children.add(child);
		if (added) {
			child.setParent(this);
			firePropertyChange(PROPERTY_CHILD, null, child);
		}
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
	
	@Override
	public String toString() {
		String name = getName();
		if (name == null)
			name = "(anonymous)";
		String children = "";
		if (getChildren().size() != 0)
			children = " " + getChildren();
		return getClass().getSimpleName() + " " + name + children;
	}
	
	public Change changeAddChild(Layoutable child) {
		return new BigraphChangeAddChild(this, child);
	}
	
	public Change changeRemoveChild(Layoutable child) {
		return new BigraphChangeRemoveChild(this, child);
	}
}
