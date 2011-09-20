package dk.itu.big_red.model.changes;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.LayoutableModelObject;

public class BigraphChangeLayout extends Change {
	public LayoutableModelObject model;
	public Rectangle newLayout;
	
	public BigraphChangeLayout(LayoutableModelObject model, Rectangle newLayout) {
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
}
