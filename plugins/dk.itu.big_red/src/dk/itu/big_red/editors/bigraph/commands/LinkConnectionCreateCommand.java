package dk.itu.big_red.editors.bigraph.commands;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Edge;
import org.bigraph.model.Link;
import org.bigraph.model.Point;
import org.bigraph.model.changes.ChangeGroup;

import dk.itu.big_red.editors.bigraph.parts.LinkPart;

/**
 * A LinkConnectionCreateCommand is in charge of creating and updating {@link
 * Link}s on a {@link Bigraph} in response to user input. It can either join a
 * {@link Point} to an existing {@link Link}, or join two {@link Point}s
 * together, creating a new {@link Edge} in the process.
 * </ul>
 * @author alec
 */
public class LinkConnectionCreateCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	private Object first = null, second = null;
	
	public LinkConnectionCreateCommand() {
		setChange(cg);
	}

	public void setFirst(Object e) {
		if (!(e instanceof LinkPart.Connection)) {
			first = e;
		} else first = ((LinkPart.Connection)e).getLink();
	}
	
	public void setSecond(Object e) {
		if (!(e instanceof LinkPart.Connection)) {
			second = e;
		} else second = ((LinkPart.Connection)e).getLink();
	}

	@Override
	public LinkConnectionCreateCommand prepare() {
		cg.clear();
		if (first instanceof Point && second instanceof Point) {
			Bigraph b = ((Point)first).getBigraph();
			setTarget(b);
			Edge ed = new Edge();
			cg.add(b.changeAddChild(ed, b.getFirstUnusedName(ed)));
			cg.add(((Point)first).changeConnect(ed));
			cg.add(((Point)second).changeConnect(ed));
		} else if (first instanceof Point && second instanceof Link) {
			setTarget(((Point)first).getBigraph());
			cg.add(((Point)first).changeConnect((Link)second));
		} else if (first instanceof Link && second instanceof Point) {
			setTarget(((Link)first).getBigraph());
			cg.add(((Point)second).changeConnect((Link)first));
		}
		return this;
	}
}
