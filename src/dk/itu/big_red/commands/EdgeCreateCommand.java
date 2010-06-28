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
			this.target = ((EdgeConnection)e).getParent().getEdgeTarget();
	}
	
	public void setSource(Object e) {
		if (e instanceof IConnectable)
			this.source = (IConnectable)e;
		else if (e instanceof EdgeConnection)
			this.source = ((EdgeConnection)e).getParent().getEdgeTarget();
	}
	
	public boolean canExecute() {
		return (target != null && source != null);
	}
	
	public void execute() {
		if (edge != null) {
			if (!(source instanceof EdgeTarget))
				edge.addPoint(source);
			if (!(target instanceof EdgeTarget))
				edge.addPoint(target);
		} else
		/*
		 * If either source or target is an EdgeTarget, then we can simply add
		 * a new EdgeConnection to the existing Edge.
		 */
		if (target instanceof EdgeTarget) {
			EdgeTarget target = (EdgeTarget)this.target;
			target.getParent().addPoint(source);
			target.averagePosition();
			
			edge = target.getParent();
		} else if (source instanceof EdgeTarget) {
			EdgeTarget source = (EdgeTarget)this.source;
			source.getParent().addPoint(target);
			source.averagePosition();
			
			edge = source.getParent();
		} else {
			/*
			 * Create a new Edge.
			 */
			edge = new Edge();
			edge.addPoint(source);
			edge.addPoint(target);
			edge.getEdgeTarget().averagePosition();
		}
	}
	
	public boolean canUndo() {
		return (edge != null);
	}
	
	public void undo() {
		if (!(source instanceof EdgeTarget))
			edge.removePoint(source);
		if (!(target instanceof EdgeTarget))
			edge.removePoint(target);
	}
}
