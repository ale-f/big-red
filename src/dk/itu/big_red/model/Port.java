package dk.itu.big_red.model;

import org.eclipse.core.runtime.IAdaptable;

import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.interfaces.ILayoutable;

/**
 * Ports are one of the two kinds of object that can be connected by an
 * {@link Edge} (the other being the {@link InnerName}). Ports are only ever found
 * on a {@link Node}, and inherit their name from a {@link Control}.
 * @author alec
 *
 */
public class Port extends Point implements IAdaptable, ILayoutable {
	/**
	 * An integer index specifying the line segment on the parent {@link
	 * Node}'s polygon that this Port falls on. Together with {@link
	 * #distance}, it defines this Port's position.
	 * 
	 * <p>(If the {@link Control} defines an {@link Control.Shape#SHAPE_OVAL
	 * oval} appearance, this value will be <code>0</code>.)
	 */
	private int segment = 0;
	
	/**
	 * A value (<code>0 <= distance < 1</code>) specifying this Port's offset
	 * on its {@link #segment}. Together with <code>segment</code>, it defines
	 * this Port's position.
	 */
	private double distance = 0.0;
	
	public Port(String name, double distance) {
		setName(name);
		setDistance(distance);
	}
	
	@Override
	public Node getParent() {
		return (Node)super.getParent();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Gets this Port's {@link #distance distance}.
	 * @see #distance
	 */
	public double getDistance() {
		return distance;
	}
	
	/**
	 * Sets this Port's {@link #distance distance}.
	 * @param distance the new distance value
	 * @see #distance
	 */
	public void setDistance(double distance) {
		if (distance >= 0 && distance < 1)
			this.distance = distance;
	}

	/**
	 * Gets this Port's {@link #segment segment}.
	 * @see #segment
	 * @return
	 */
	public int getSegment() {
		Control c = getParent().getControl();
		return (c.getShape() == Shape.SHAPE_POLYGON ? segment : 0);
	}
	
	/**
	 * Sets this Port's {@link #segment segment}.
	 * 
	 * <p>Calls to this method will do nothing if the parent {@link Node}'s
	 * {@link Control} isn't a {@link Control.Shape#SHAPE_POLYGON polygon}.
	 * @param segment the new segment value
	 * @see #segment
	 */
	public void setSegment(int segment) {
		Control c = getParent().getControl();
		if (c.getShape() == Shape.SHAPE_POLYGON &&
				distance >= 0 && distance < c.getPoints().size())
			this.segment = segment;
	}
	
	@Override
	public Bigraph getBigraph() {
		return getParent().getBigraph();
	}
}
