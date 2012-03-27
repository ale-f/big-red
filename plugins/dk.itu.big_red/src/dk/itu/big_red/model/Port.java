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
	/**
	 * The property name fired when this Port's {@link #segment} changes. The
	 * property values are {@link Integer}s.
	 */
	public static final String PROPERTY_SEGMENT = "PortSegment";
	
	/**
	 * The property name fired when this Port's {@link #distance} changes. The
	 * property values are {@link Double}s.
	 */
	public static final String PROPERTY_DISTANCE = "PortDistance";
	
	private PortSpec spec;
	
	private void setSpec(PortSpec spec) {
		this.spec = spec;
	}
	
	public PortSpec getSpec() {
		return spec;
	}
	
	public Port() {
		setLayout(new Rectangle(5, 5, 10, 10));
	}
	
	@Override
	public void setLayout(Rectangle newLayout) {
		super.setLayout(newLayout.setSize(10, 10));
	}
	
	public Port(PortSpec i) {
		setLayout(new Rectangle(5, 5, 10, 10));
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
	
	/**
	 * Gets this Port's {@link #distance distance}.
	 * @see #distance
	 */
	public double getDistance() {
		return getSpec().getDistance();
	}

	/**
	 * Gets this Port's {@link #segment segment}.
	 * @see #segment
	 * @return
	 */
	public int getSegment() {
		return getSpec().getSegment();
	}
	
	@Override
	public Rectangle getLayout() {
		Rectangle r = super.getLayout().getCopy();
		PointList polypt = getParent().getFittedPolygon();
		if (polypt != null) {
			int segment = getSegment();
			org.eclipse.draw2d.geometry.Point p1 = polypt.getPoint(segment),
			      p2 = polypt.getPoint((segment + 1) % polypt.size());
			r.setLocation(new Line(p1, p2).
					getPointFromOffset(getDistance()).translate(-5, -5));
		} else {
			r.setLocation(
				new Ellipse(
						getParent().getLayout().getCopy().setLocation(0, 0)).
					getPointFromOffset(getDistance()).translate(-5, -5));
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
	
	@Override
	public Object getProperty(String name) {
		if (PROPERTY_DISTANCE.equals(name)) {
			return getDistance();
		} else if (PROPERTY_SEGMENT.equals(name)) {
			return getSegment();
		} else return super.getProperty(name);
	}
}
