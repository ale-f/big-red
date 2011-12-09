package dk.itu.big_red.editors.bigraph.commands;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.util.geometry.Rectangle;

public class LayoutableRelayoutCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
	public LayoutableRelayoutCommand() {
		setChange(cg);
	}
	
	private Layoutable model;
	private Rectangle layout;
	
	public void setConstraint(Object rect) {
		if (rect instanceof Rectangle)
			layout = (Rectangle)rect;
		else if (rect instanceof org.eclipse.draw2d.geometry.Rectangle)
			layout = new Rectangle((org.eclipse.draw2d.geometry.Rectangle)rect);
	}

	public void setModel(Object model) {
		if (model instanceof Layoutable && !(model instanceof Bigraph))
			this.model = (Layoutable)model;
	}
	
	@Override
	public LayoutableRelayoutCommand prepare() {
		cg.clear();
		if (model == null || layout == null)
			return this;
		setTarget(model.getBigraph());
		if (noOverlap() && boundariesSatisfied())
			cg.add(model.changeLayout(layout));
		return this;
	}
	
	public boolean noOverlap() {
		for (Layoutable i : model.getParent().getChildren()) {
			if (i instanceof Edge || i == model)
				continue;
			else if (i.getLayout().intersects(layout))
				return false;
		}
		return true;
	}
	
	private boolean boundariesSatisfied() {
		if (!(model.getParent() instanceof Bigraph))
			return true;
		Bigraph bigraph = (Bigraph)model.getParent();
		int top = layout.getY(),
		    bottom = layout.getY() + layout.getHeight();
		if (model instanceof OuterName) {
			if (bottom > bigraph.getLowerOuterNameBoundary())
				return false;
		} else if (model instanceof Root) {
			if (top < bigraph.getUpperRootBoundary() ||
					bottom > bigraph.getLowerRootBoundary())
				return false;
		} else if (model instanceof InnerName) {
			if (top < bigraph.getUpperInnerNameBoundary())
				return false;
		}
		return true;
	}
}
