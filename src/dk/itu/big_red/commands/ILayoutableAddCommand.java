package dk.itu.big_red.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.interfaces.ILayoutable;

public class ILayoutableAddCommand extends Command {
	private ILayoutable parent = null;
	private ILayoutable child = null;
	private Rectangle constraint = null, oldConstraint = null;
	private boolean constraintTranslated = false;
	
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
		return (child instanceof Edge ||
				(constraint.x >= 0 && constraint.y >= 0 &&
				 constraint.x + constraint.width <= parent.getLayout().width &&
				 constraint.y + constraint.height <= parent.getLayout().height));
	}
	
	public boolean canExecute() {
		return (this.parent != null && this.child != null &&
				(child instanceof Edge || this.parent.canContain(this.child)) &&
				this.constraint != null &&
				parentLayoutCanContainChildConstraint());
	}
	
	public void execute() {
		if (!(child instanceof Edge))
			parent.addChild(child);
		oldConstraint = child.getLayout();
		if (child instanceof Edge && !constraintTranslated) {
			constraint = new Rectangle(constraint).translate(parent.getRootLayout().getTopLeft());
			constraintTranslated = true;
		}
		child.setLayout(constraint);
	}
	
	public void undo() {
		child.setLayout(oldConstraint);
		if (!(child instanceof Edge))
			parent.removeChild(child);
	}

}
