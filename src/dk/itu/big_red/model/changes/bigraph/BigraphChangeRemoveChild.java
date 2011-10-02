package dk.itu.big_red.model.changes.bigraph;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.util.geometry.Rectangle;

public class BigraphChangeRemoveChild extends Change {
	public Container parent;
	public Layoutable child;
	
	public BigraphChangeRemoveChild(Container parent, Layoutable child) {
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
	
	@Override
	public String toString() {
		return "Change(remove child " + child + " from " + parent + ")";
	}
}
