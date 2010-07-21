package dk.itu.big_red.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.interfaces.ILayoutable;

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
	
	public boolean canExecute() {
		return node != null && container != null;
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
