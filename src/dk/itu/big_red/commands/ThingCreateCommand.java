package dk.itu.big_red.commands;



import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Thing;

public class ThingCreateCommand extends Command {
	private Thing container;
	private Thing node;
	
	public ThingCreateCommand() {
		super();
		container = null;
		node = null;
	}
	
	public void setObject(Object s) {
		if (s instanceof Thing)
			this.node = (Thing)s;
	}
	
	public void setContainer(Object e) {
		if (e instanceof Thing)
			this.container = (Thing)e;
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
		return (node != null && container != null ? container.contains(node) : false);
	}
	
	public void undo() {
		container.removeChild(node);
	}
}
