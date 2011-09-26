package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Node;

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
}
