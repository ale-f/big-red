package dk.itu.big_red.editors.bigraph.commands;

import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.assistants.LinkConnection;

/**
 * A LinkConnectionCreateCommand is in charge of creating and updating {@link
 * Link}s on a {@link Bigraph} in response to user input. It can either join a
 * {@link Point} to an existing {@link Link}, or join two {@link Point}s
 * together, creating a new {@link Edge} in the process.
 * </ul>
 * @author alec
 *
 */
public class LinkConnectionCreateCommand extends Command {
	private Point point1 = null, point2 = null;
	private Link link = null, modifiedLink = null;
	
	public LinkConnectionCreateCommand() {
		super();
	}

	public void setFirst(Object e) {
		if (e instanceof Point) {
			point1 = (Point)e;
			if (point1.getLink() != null)
				point1 = null;
		} else if (e instanceof Link) {
			link = (Link)e;
		} else if (e instanceof LinkConnection) {
			link = ((LinkConnection)e).getLink();
		}
	}
	
	public void setSecond(Object e) {
		if (e instanceof Point) {
			point2 = (Point)e;
			if (point2.getLink() != null)
				point2 = null;
		} else if (e instanceof Link) {
			link = (Link)e;
		} else if (e instanceof LinkConnection) {
			link = ((LinkConnection)e).getLink();
		}
	}
	
	@Override
	public boolean canExecute() {
		return (point1 != null && point2 != null) ||
				(point1 != null && link != null) ||
				(link != null && point2 != null);
	}
	
	@Override
	public void execute() {
		if (modifiedLink != null) { /* redo */
			modifiedLink.addPoint(point1);
			modifiedLink.addPoint(point2);
		} else if (point1 != null && point2 != null) {
			Edge e = new Edge();
			point1.getBigraph().addChild(e);
			e.addPoint(point1);
			e.addPoint(point2);
			e.averagePosition();
			
			modifiedLink = e;
		} else if (point1 != null && link != null) {
			modifiedLink = link;
			link.addPoint(point1);
		} else if (link != null && point2 != null) {
			modifiedLink = link;
			link.addPoint(point2);
		}
	}
	
	@Override
	public boolean canUndo() {
		return (modifiedLink != null);
	}
	
	@Override
	public void undo() {
		modifiedLink.removePoint(point1);
		modifiedLink.removePoint(point2);
	}
}
