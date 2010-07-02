package dk.itu.big_red.commands;

import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.*;
import dk.itu.big_red.model.interfaces.IConnectable;

public class EdgeCreateCommand extends Command {
	private IConnectable target, source;
	private Edge edge = null;
	
	public EdgeCreateCommand() {
		super();
		target = source = null;
	}

	public void setTarget(Object e) {
		if (e instanceof IConnectable)
			this.target = (IConnectable)e;
		else if (e instanceof EdgeConnection)
			this.target = ((EdgeConnection)e).getParent();
	}
	
	public void setSource(Object e) {
		if (e instanceof IConnectable)
			this.source = (IConnectable)e;
		else if (e instanceof EdgeConnection)
			this.source = ((EdgeConnection)e).getParent();
	}
	
	public boolean canExecute() {
		return (target != null && source != null);
	}
	
	public void execute() {
		if (edge != null) {
			if (!(source instanceof Edge))
				edge.addPoint(source);
			if (!(target instanceof Edge))
				edge.addPoint(target);
		} else
		/*
		 * If either source or target is an EdgeTarget, then we can simply add
		 * a new EdgeConnection to the existing Edge.
		 */
		if (target instanceof Edge) {
			Edge target = (Edge)this.target;
			target.addPoint(source);
			target.averagePosition();
			
			edge = target;
		} else if (source instanceof Edge) {
			Edge source = (Edge)this.source;
			source.addPoint(target);
			source.averagePosition();
			
			edge = source;
		} else {
			/*
			 * Create a new Edge.
			 */
			edge = new Edge();
			edge.setParent(source.getBigraph());
			edge.addPoint(source);
			edge.addPoint(target);
			edge.averagePosition();
		}
	}
	
	public boolean canUndo() {
		return (edge != null);
	}
	
	public void undo() {
		if (!(source instanceof Edge))
			edge.removePoint(source);
		if (!(target instanceof Edge))
			edge.removePoint(target);
	}
}
