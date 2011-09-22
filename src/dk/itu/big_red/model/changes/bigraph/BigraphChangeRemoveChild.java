package dk.itu.big_red.model.changes.bigraph;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.changes.Change;

public class BigraphChangeRemoveChild extends Change {
	public Container parent;
	public LayoutableModelObject child;
	
	public BigraphChangeRemoveChild(Container parent, LayoutableModelObject child) {
		this.parent = parent;
		this.child = child;
	}

	private Rectangle oldLayout;
	@Override
	public void beforeApply() {
		oldLayout = child.getLayout();
	}
	
	@Override
	public Change inverse() {
		return new BigraphChangeAddChild(parent, child, oldLayout);
	}
	
	@Override
	public boolean isReady() {
		return (parent != null && child != null);
	}
}
