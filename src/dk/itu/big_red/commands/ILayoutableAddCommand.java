package dk.itu.big_red.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.interfaces.ILayoutable;

public class ILayoutableAddCommand extends Command {
	private ILayoutable parent = null;
	private ILayoutable child = null;
	private Rectangle constraint = null, oldConstraint = null;
	
	public void setParent(Object parent) {
		if (parent instanceof ILayoutable)
			this.parent = (ILayoutable)parent;
	}
	
	public void setChild(Object child) {
		if (child instanceof ILayoutable)
			this.child = (ILayoutable)child;
	}
	
	public void setConstraint(Object constraint) {
		if (constraint instanceof Rectangle)
			this.constraint = (Rectangle)constraint;
	}
	
	public boolean parentLayoutCanContainChildConstraint() {
		return (constraint.x >= 0 && constraint.y >= 0 &&
				constraint.x + constraint.width <= parent.getLayout().width &&
				constraint.y + constraint.height <= parent.getLayout().height);
	}
	
	public boolean canExecute() {
		return (this.parent != null && this.child != null &&
				this.constraint != null &&
				parentLayoutCanContainChildConstraint());
	}
	
	public void execute() {
		parent.addChild(child);
		oldConstraint = child.getLayout();
		child.setLayout(constraint);
	}
	
	public void undo() {
		child.setLayout(oldConstraint);
		parent.removeChild(child);
	}

}
