package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Node;

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
public class Container extends LayoutableModelObject implements IAdaptable {
	protected ArrayList<LayoutableModelObject> children = new ArrayList<LayoutableModelObject>();

	/**
	 * The property name fired when a child is added or removed.
	 */
	public static final String PROPERTY_CHILD = "ContainerChild";
	
	public Container() {
		layout = new Rectangle(10, 10, 100, 100);
	}
	
	public boolean canContain(LayoutableModelObject child) {
		return false;
	}
	
	public void addChild(LayoutableModelObject child) {
		boolean added = children.add(child);
		if (added) {
			child.setParent(this);
			firePropertyChange(PROPERTY_CHILD, null, child);
		}
	}
	
	public void removeChild(LayoutableModelObject child) {
		boolean removed = children.remove(child);
		if (removed) {
			child.setParent(null);
			firePropertyChange(PROPERTY_CHILD, child, null);
		}
	}
	
	public List<LayoutableModelObject> getChildren() {
		return children;
	}

	public boolean hasChild(LayoutableModelObject child) {
		return children.contains(child);
	}
	
	@Override
	public Container clone() {
		Container c = (Container)super.clone();
		
		for (LayoutableModelObject child : getChildren())
			c.addChild(child.clone());
		
		return c;
	}
	
	public void relayout(ChangeGroup cg) {
		int left = 20, maxHeight = 10;
		
		for (LayoutableModelObject i : children) {
			int height = i.getLayout().height;
			if (height > maxHeight)
				maxHeight = height;
		}
		
		for (LayoutableModelObject i : children) {
			Rectangle nl = i.getLayout().getCopy();
			cg.add(new BigraphChangeLayout(i,
				nl.setLocation(left, 20 + ((maxHeight - nl.height) / 2))));
			left += nl.width + 20;
		}
		
		if (left < 50)
			left = 50;
		
		cg.add(new BigraphChangeLayout(this,
			getLayout().getCopy().setSize(left, maxHeight + 40)));
	}
}
