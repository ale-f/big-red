package dk.itu.big_red.commands;

import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.LinkConnection;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.interfaces.internal.IConnectable;

public class EdgeCreateCommand extends Command {
	private IConnectable point1, point2;
	private Link link = null;
	
	public EdgeCreateCommand() {
		super();
		point1 = point2 = null;
	}

	public void setTarget(Object e) {
		if (e instanceof IConnectable)
			this.point1 = (IConnectable)e;
		else if (e instanceof LinkConnection)
			this.point1 = ((LinkConnection)e).getTarget();
	}
	
	public void setSource(Object e) {
		if (e instanceof IConnectable)
			this.point2 = (IConnectable)e;
		else if (e instanceof LinkConnection)
			this.point2 = ((LinkConnection)e).getTarget();
	}
	
	public boolean canExecute() {
		return (point1 != null && point2 != null &&
				point1 != point2 &&
				!(point1 instanceof Link && point2 instanceof Link));
	}
	
	public void execute() {
		if (link != null) {
			if (point2 instanceof Point)
				link.addPoint((Point)point2);
			if (point1 instanceof Point)
				link.addPoint((Point)point1);
		} else
		/*
		 * If either point2 or point1 is an EdgeTarget, then we can simply add
		 * a new LinkConnection to the existing Edge.
		 */
		if (point1 instanceof Link) {
			Link point1 = (Link)this.point1;
			point1.addPoint((Point)point2);
			
			link = point1;
		} else if (point2 instanceof Link) {
			Link point2 = (Link)this.point2;
			point2.addPoint((Point)point1);
			
			link = point2;
		} else {
			/*
			 * Create a new Edge.
			 */
			Edge e = new Edge();
			e.setParent(point2.getBigraph());
			e.addPoint((Point)point2);
			e.addPoint((Point)point1);
			e.averagePosition();
			
			link = e;
		}
	}
	
	public boolean canUndo() {
		return (link != null);
	}
	
	public void undo() {
		if (point2 instanceof Point)
			link.removePoint((Point)point2);
		if (point1 instanceof Point)
			link.removePoint((Point)point1);
	}
}
