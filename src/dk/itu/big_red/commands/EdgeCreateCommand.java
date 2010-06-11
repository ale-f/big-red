package dk.itu.big_red.commands;


import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.*;
import dk.itu.big_red.model.interfaces.IConnectable;

public class EdgeCreateCommand extends Command {
	private IConnectable target, source;
	private Edge edge;
	
	public EdgeCreateCommand() {
		super();
		target = source = null;
		edge = null;
	}
	
	public void setObject(Object s) {
		if (s instanceof Edge)
			this.edge = (Edge)s;
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
		return false;
	}
	
	public void execute() {
	}
	
	public boolean canUndo() {
		return false;
	}
	
	public void undo() {
	}
}
