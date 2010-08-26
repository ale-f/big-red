package dk.itu.big_red.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;

public class ILayoutableCreateCommand extends Command {
	private ILayoutable container;
	private ILayoutable node;
	
	public ILayoutableCreateCommand() {
		super();
		container = null;
		node = null;
	}
	
	public void setObject(Object s) {
		if (s instanceof ILayoutable)
			this.node = (ILayoutable)s;
	}
	
	public void setContainer(Object e) {
		if (e instanceof ILayoutable)
			this.container = (ILayoutable)e;
	}
	
	public void setLayout(Rectangle r) {
		if (node == null) {
			return;
		} else node.setLayout(r);
	}
	
	public boolean noOverlap() {
		for (ILayoutable i : container.getChildren()) {
			if (i instanceof Edge)
				continue;
			else if (i.getLayout().intersects(node.getLayout()))
				return false;
		}
		return true;
	}
	
	private boolean boundariesSatisfied() {
		if (!(container instanceof Bigraph))
			return true;
		Bigraph bigraph = (Bigraph)container;
		int top = node.getLayout().y,
		    bottom = node.getLayout().y + node.getLayout().height;
		if (node instanceof OuterName) {
			if (bottom > bigraph.getLowerOuterNameBoundary())
				return false;
		} else if (node instanceof Root) {
			if (top < bigraph.getUpperRootBoundary() ||
					bottom > bigraph.getLowerRootBoundary())
				return false;
		} else if (node instanceof InnerName) {
			if (top < bigraph.getUpperInnerNameBoundary())
				return false;
		}
		return true;
	}
	
	public boolean canExecute() {
		return (node != null && container != null && boundariesSatisfied() &&
				noOverlap());
	}
	
	public void execute() {
		container.addChild(node);
	}
	
	public boolean canUndo() {
		return (node != null && container != null ? node.getParent() == container : false);
	}
	
	public void undo() {
		container.removeChild(node);
	}
}
