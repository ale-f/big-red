package dk.itu.big_red.model.changes.bigraph;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.changes.Change;

public class BigraphChangeLayout extends Change {
	public Layoutable model;
	public Rectangle newLayout;
	
	public BigraphChangeLayout(Layoutable model, Rectangle newLayout) {
		this.model = model;
		this.newLayout = newLayout;
	}

	private Rectangle oldLayout;
	@Override
	public void beforeApply() {
		oldLayout = model.getLayout();
	}
	
	@Override
	public Change inverse() {
		return new BigraphChangeLayout(model, oldLayout);
	}
	
	@Override
	public boolean canInvert() {
		return (oldLayout != null);
	}
	
	@Override
	public boolean isReady() {
		return (model != null && newLayout != null);
	}
	
	@Override
	public String toString() {
		return "Change(set layout of " + model + " to " + newLayout + ")";
	}
}
