package dk.itu.big_red.model;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.interfaces.internal.ILayoutable;

public abstract class LayoutableModelObject extends ModelObject implements ILayoutable {
	protected Rectangle layout;
	protected Container parent;
	
	public LayoutableModelObject() {
		layout = new Rectangle(10, 10, 100, 100);
	}
	
	@Override
	public Rectangle getLayout() {
		return new Rectangle(layout);
	}

	@Override
	public Rectangle getRootLayout() {
		return new Rectangle(getLayout()).translate(getParent().getRootLayout().getTopLeft());
	}

	@Override
	public void setLayout(Rectangle newLayout) {
		Rectangle oldLayout = layout;
		layout = new Rectangle(newLayout);
		firePropertyChange(PROPERTY_LAYOUT, oldLayout, layout);
	}

	@Override
	public Bigraph getBigraph() {
		return getParent().getBigraph();
	}

	@Override
	public Container getParent() {
		return parent;
	}

	@Override
	public void setParent(Container parent) {
		this.parent = parent;
	}
	
	@Override
	public abstract ILayoutable clone() throws CloneNotSupportedException;
}
