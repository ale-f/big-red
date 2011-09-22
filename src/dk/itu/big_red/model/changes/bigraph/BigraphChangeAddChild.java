package dk.itu.big_red.model.changes.bigraph;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.changes.Change;

public class BigraphChangeAddChild extends Change {
	public Container parent;
	public LayoutableModelObject child;
	public Rectangle newLayout;
	
	public BigraphChangeAddChild(Container parent, LayoutableModelObject child, Rectangle newLayout) {
		this.parent = parent;
		this.child = child;
		this.newLayout = newLayout;
	}
	
	@Override
	public Change inverse() {
		return new BigraphChangeRemoveChild(parent, child);
	}
}
