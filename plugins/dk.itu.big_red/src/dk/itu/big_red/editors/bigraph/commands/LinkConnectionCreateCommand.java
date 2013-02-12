package dk.itu.big_red.editors.bigraph.commands;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.Link;
import org.bigraph.model.Point;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;

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
	private ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
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
	public void prepare() {
		cg.clear();
		Bigraph b = null;
		if (first instanceof Point && second instanceof Point) {
			Point
				firstP = (Point)first,
				secondP = (Point)second;
			b = firstP.getBigraph();
			Edge.Identifier ed = new Edge.Identifier(
					b.getNamespace(Link.class).getNextName());
			cg.add(new BoundDescriptor(b,
					new Container.ChangeAddChildDescriptor(
							new Bigraph.Identifier(), ed)));
			cg.add(new BoundDescriptor(b,
					new Point.ChangeConnectDescriptor(
							firstP.getIdentifier(), ed)));
			cg.add(new BoundDescriptor(b,
					new Point.ChangeConnectDescriptor(
							secondP.getIdentifier(), ed)));
		} else {
			Point p = (first instanceof Point ? (Point)first :
				second instanceof Point ? (Point)second : null);
			Link l = (first instanceof Link ? (Link)first :
				second instanceof Link ? (Link)second : null);
			if (p != null && l != null) {
				b = p.getBigraph();
				cg.add(new BoundDescriptor(b,
						new Point.ChangeConnectDescriptor(
								p.getIdentifier(), l.getIdentifier())));
			}
		}
		setContext(b);
	}
}
