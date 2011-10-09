package dk.itu.big_red.editors.bigraph.commands;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.assistants.LinkConnection;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeEdgeReposition;

/**
 * A LinkConnectionCreateCommand is in charge of creating and updating {@link
 * Link}s on a {@link Bigraph} in response to user input. It can either join a
 * {@link Point} to an existing {@link Link}, or join two {@link Point}s
 * together, creating a new {@link Edge} in the process.
 * </ul>
 * @author alec
 *
 */
public class LinkConnectionCreateCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	private Object first = null, second = null;
	
	public LinkConnectionCreateCommand() {
		setChange(cg);
	}

	public void setFirst(Object e) {
		if (!(e instanceof LinkConnection)) {
			first = e;
		} else first = ((LinkConnection)e).getLink();
	}
	
	public void setSecond(Object e) {
		if (!(e instanceof LinkConnection)) {
			second = e;
		} else second = ((LinkConnection)e).getLink();
	}

	@Override
	public void prepare() {
		cg.clear();
		if (first instanceof Point && second instanceof Point) {
			Bigraph b = ((Point)first).getBigraph();
			setTarget(b);
			Edge ed = new Edge();
			cg.add(b.changeAddChild(ed),
					((Point)first).changeConnect(ed),
					((Point)second).changeConnect(ed),
					new BigraphChangeEdgeReposition(ed));
		} else if (first instanceof Point && second instanceof Link) {
			setTarget(((Point)first).getBigraph());
			cg.add(((Point)first).changeConnect((Link)second));
		} else if (first instanceof Link && second instanceof Point) {
			setTarget(((Link)first).getBigraph());
			cg.add(((Point)second).changeConnect((Link)first));
		}
	}
}
