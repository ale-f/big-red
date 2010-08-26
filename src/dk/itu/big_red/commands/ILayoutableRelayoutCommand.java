package dk.itu.big_red.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;

public class ILayoutableRelayoutCommand extends Command {
	private ILayoutable model;
	private Rectangle layout, oldLayout;
	
	public void setConstraint(Object rect) {
		if (rect instanceof Rectangle)
			this.layout = (Rectangle)rect;
	}

	public void setModel(Object model) {
		if (model instanceof ILayoutable) {
			this.model = (ILayoutable)model;
			this.oldLayout = this.model.getLayout();
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
	
	public boolean canExecute() {
		return (model != null && layout != null && oldLayout != null &&
				parentLayoutCanContainChildLayout() && noOverlap() &&
				boundariesSatisfied());
	}
	
	public void execute() {
		model.setLayout(layout);
		if (model.getParent() instanceof Bigraph)
			model.getBigraph().updateBoundaries();
	}

	public void undo() {
		model.setLayout(this.oldLayout);
		if (model.getParent() instanceof Bigraph)
			model.getBigraph().updateBoundaries();
	}
}
