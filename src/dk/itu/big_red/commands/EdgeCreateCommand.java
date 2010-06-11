package dk.itu.big_red.commands;


import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.*;
import dk.itu.big_red.model.interfaces.IConnectable;

public class EdgeCreateCommand extends Command {
	private IConnectable target, source;
	
	public EdgeCreateCommand() {
		super();
		target = source = null;
	}

	public void setTarget(Object e) {
		if (e instanceof IConnectable)
			this.target = (IConnectable)e;
	}
	
	public void setSource(Object e) {
		if (e instanceof IConnectable)
			this.source = (IConnectable)e;
	}
	
	public boolean canExecute() {
		return (target != null && source != null);
	}
	
	public void execute() {
		/*
		 * If either source or target is an EdgeTarget, then we can simply add
		 * a new EdgeConnection to the existing Edge.
		 */
		if (target instanceof EdgeTarget) {
			EdgeTarget target = (EdgeTarget)this.target;
			target.getParent().addPoint(source);
		} else if (source instanceof EdgeTarget) {
			EdgeTarget source = (EdgeTarget)this.source;
			source.getParent().addPoint(target);
		} else {
			/*
			 * Create a new Edge.
			 */
			Edge e = new Edge();
			e.addPoint(source);
			e.addPoint(target);
		}
	}
	
	public boolean canUndo() {
		return false;
	}
	
	public void undo() {
	}
}
