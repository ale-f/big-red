package dk.itu.big_red.model;

import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.assistants.Ellipse;
import dk.itu.big_red.model.assistants.Line;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IPort;

/**
 * Ports are one of the two kinds of object that can be connected by an
 * {@link Edge} (the other being the {@link InnerName}). Ports are only ever found
 * on a {@link Node}, and inherit their name from a {@link Control}.
 * @author alec
 *
 */
public class Port extends Point implements IPort {
	private PortSpec spec;
	
	private void setSpec(PortSpec spec) {
		this.spec = spec;
	}
	
	public PortSpec getSpec() {
		return spec;
	}
	
	@Override
	public void setLayout(Rectangle newLayout) {
		/* do nothing */
	}
	
	public Port(PortSpec i) {
		setSpec(i);
	}
	
	@Override
	public Node getParent() {
		return (Node)super.getParent();
	}
	
	@Override
	public String getName() {
		return getSpec().getName();
	}
	
	@Override
	public Rectangle getLayout() {
		Rectangle r = new Rectangle(0, 0, 10, 10);
		PointList polypt = getParent().getFittedPolygon();
		double distance = getSpec().getDistance();
		if (polypt != null) {
			int segment = getSpec().getSegment();
			org.eclipse.draw2d.geometry.Point p1 = polypt.getPoint(segment),
			      p2 = polypt.getPoint((segment + 1) % polypt.size());
			r.setLocation(new Line(p1, p2).
					getPointFromOffset(distance).translate(-5, -5));
		} else {
			r.setLocation(
				new Ellipse(
						getParent().getLayout().getCopy().setLocation(0, 0)).
					getPointFromOffset(distance).translate(-5, -5));
		}
		return r;
	}

	@Override
	public INode getNode() {
		return getParent();
	}
	
	/**
	 * This method should never be called; {@link Port}s are created only when
	 * a {@link Control} is given to a {@link Node}.
	 */
	@Override
	public Point clone() {
		return null;
	}
}
