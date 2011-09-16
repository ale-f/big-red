package dk.itu.big_red.commands;

import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.LinkConnection;
import dk.itu.big_red.model.Point;

/**
 * A LinkConnectionCreateCommand 
 * @author alec
 *
 */
public class LinkConnectionCreateCommand extends Command {
	private Object first = null, second = null;
	private Link modifiedLink = null;
	
	public LinkConnectionCreateCommand() {
		super();
	}

	public void setFirst(Object e) {
		if (!(e instanceof LinkConnection))
			first = e;
		else first = ((LinkConnection)e).getLink();
	}
	
	public void setSecond(Object e) {
		if (!(e instanceof LinkConnection))
			second = e;
		else second = ((LinkConnection)e).getLink();
	}
	
	@Override
	public boolean canExecute() {
		return (first != null && second != null &&
				first != second &&
				((first instanceof Point && second instanceof Point) ||
				 (first instanceof Point && second instanceof Link) ||
				 (first instanceof Link && second instanceof Point)));
	}
	
	@Override
	public void execute() {
		if (modifiedLink != null) {
			if (first instanceof Point)
				modifiedLink.addPoint((Point)first);
			if (second instanceof Point)
				modifiedLink.addPoint((Point)second);
		} else
		/*
		 * If either point2 or point1 is an EdgeTarget, then we can simply add
		 * a new LinkConnection to the existing Edge.
		 */
		if (first instanceof Link) {
			Link link = (Link)first;
			link.addPoint((Point)second);
			
			modifiedLink = link;
		} else if (second instanceof Link) {
			Link link = (Link)second;
			link.addPoint((Point)first);
			
			modifiedLink = link;
		} else if (first instanceof Point && second instanceof Point) {
			/*
			 * Create a new Edge.
			 */
			Edge e = new Edge();
			e.setParent(((Point)first).getBigraph());
			e.addPoint((Point)second);
			e.addPoint((Point)first);
			e.averagePosition();
			
			modifiedLink = e;
		}
	}
	
	@Override
	public boolean canUndo() {
		return (modifiedLink != null);
	}
	
	@Override
	public void undo() {
		if (first instanceof Point)
			modifiedLink.removePoint((Point)first);
		if (second instanceof Point)
			modifiedLink.removePoint((Point)second);
	}
}
