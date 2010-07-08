package dk.itu.big_red.model;

import org.eclipse.core.runtime.IAdaptable;

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
	 * The position of a Port on its parent {@link Node} is governed by its
	 * <code>distance</code>, a value in the range [0,1) that specifies a
	 * fractional clockwise offset on the Node's outline. Here's a terrible
	 * attempt at a diagram:
	 * 
	 * <pre>
	 *             0
	 *         +-------+
	 *         |       |
	 *    0.75 |       | 0.25
	 *         |       |
	 *         +-------+
	 *            0.5</pre>
	 */
	private double distance = 0.0;
	
	public Port(String name, double distance) {
		setName(name);
		setDistance(distance);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Gets this Port's {@link Port#distance distance}.
	 * @see Port#distance
	 */
	public double getDistance() {
		return distance;
	}
	
	/**
	 * Sets this Port's {@link Port#distance distance}.
	 * @param distance the new distance value, which must be in the range [0,1)
	 * @see Port#distance
	 */
	public void setDistance(double distance) {
		if (distance >= 0 && distance < 1)
			this.distance = distance;
	}

	@Override
	public Bigraph getBigraph() {
		return getParent().getBigraph();
	}
}
