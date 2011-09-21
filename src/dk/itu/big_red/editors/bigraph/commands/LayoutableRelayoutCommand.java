package dk.itu.big_red.editors.bigraph.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeLayout;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;

public class LayoutableRelayoutCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
	public LayoutableRelayoutCommand() {
		setChange(cg);
	}
	
	private LayoutableModelObject model;
	private Rectangle layout;
	
	public void setConstraint(Object rect) {
		if (rect instanceof Rectangle)
			layout = (Rectangle)rect;
		prepareGroup();
	}

	public void setModel(Object model) {
		if (model instanceof LayoutableModelObject) {
			this.model = (LayoutableModelObject)model;
			setTarget(this.model.getBigraph());
		}
		prepareGroup();
	}
	
	private void prepareGroup() {
		cg.clear();
		if (model != null && layout != null &&
				parentLayoutCanContainChildLayout() && noOverlap() &&
				boundariesSatisfied()) {
			cg.add(new BigraphChangeLayout(model, layout));
		}
	}
	
	public boolean noOverlap() {
		for (ILayoutable i : model.getParent().getChildren()) {
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
		int top = layout.y,
		    bottom = layout.y + layout.height;
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
	
	public boolean parentLayoutCanContainChildLayout() {
		return (model.getParent() instanceof Bigraph ||
				(layout.x >= 0 && layout.y >= 0 &&
				 layout.x + layout.width <= model.getParent().getLayout().width &&
				 layout.y + layout.height <= model.getParent().getLayout().height));
	}
}
