package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Node;

import dk.itu.big_red.model.changes.BigraphChangeLayout;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;

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
	
	public boolean canContain(ILayoutable child) {
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
	
	protected Container _overwrite(Container orig) throws CloneNotSupportedException {
		setParent(orig.getParent());
		setLayout(new Rectangle(
			orig.getLayout().x + 10, orig.getLayout().y + 10,
			orig.getLayout().width, orig.getLayout().height));
		
		for (LayoutableModelObject child : orig.getChildren()) {
			LayoutableModelObject childClone = child.clone();
			addChild(childClone);
			childClone.setLayout(child.getLayout());
		}
		
		return this;
	}
	
	@Override
	public Container clone() throws CloneNotSupportedException {
		return new Container()._overwrite(this);
	}
	
	public void relayout(ChangeGroup cg) {
		int leftProgress = 10;
		int maxHeight = 0;
		int topOffset = 10;
		
		if (this instanceof Bigraph)
			topOffset += ((Bigraph)this).upperRootBoundary;
		
		for (LayoutableModelObject i : getChildren()) {
			Rectangle layout = i.getLayout();
			if (maxHeight < layout.height)
				maxHeight = layout.height;
			layout.setLocation(leftProgress, topOffset);
			leftProgress += layout.width + 10;
			cg.add(new BigraphChangeLayout(i, layout));
		}
		cg.add(new BigraphChangeLayout(this,
				getLayout().setSize(leftProgress, maxHeight + 20)));
	}
}
