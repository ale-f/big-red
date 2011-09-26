package dk.itu.big_red.model.changes.bigraph;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.changes.Change;

public class BigraphChangeAddChild extends Change {
	public Container parent;
	public Layoutable child;
	public Rectangle newLayout;
	
	public BigraphChangeAddChild(Container parent, Layoutable child, Rectangle newLayout) {
		this.parent = parent;
		this.child = child;
		this.newLayout = newLayout;
	}
	
	@Override
	public Change inverse() {
		return new BigraphChangeRemoveChild(parent, child);
	}
	
	@Override
	public boolean isReady() {
		return (parent != null && child != null && newLayout != null);
	}
	
	@Override
	public String toString() {
		return "Change(add child " + child + " to parent " + parent + " with layout " + newLayout + ")";
	}
}
