package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Node;

import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeLayout;

/**
 * The <code>Container</code> provides the basic functionality shared by most
 * elements of a bigraph. <code>Container</code>s can be moved around, copied, and
 * deleted by the user, so {@link Root}s, {@link Node}s, and {@link Site}s are
 * all <code>Container</code>s.
 * 
 * <p><code>Container</code>s are only useful if they've been added to a {@link
 * Bigraph} - make sure you do that before you manipulate them.
 * @author alec
 *
 */
public class Container extends Layoutable implements IAdaptable {
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
	
	public void addChild(Layoutable child) {
		boolean added = children.add(child);
		if (added) {
			child.setParent(this);
			firePropertyChange(PROPERTY_CHILD, null, child);
		}
	}
	
	public void removeChild(Layoutable child) {
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
	public Container clone() {
		Container c = (Container)super.clone();
		
		for (Layoutable child : getChildren())
			c.addChild(child.clone());
		
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
					PADDING + ((maxHeight - cl.height) / 2));
			cg.add(new BigraphChangeLayout(i, cl));
			width += cl.width + PADDING;
		}
		
		if (width < 50)
			width = 50;
		
		Dimension r =
			new Dimension(width, maxHeight + (PADDING * 2));
		cg.add(new BigraphChangeLayout(this, nl.setSize(r)));
		return r;
	}
}
