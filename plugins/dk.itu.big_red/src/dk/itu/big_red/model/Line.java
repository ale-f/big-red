package dk.itu.big_red.model;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Lines represent line segments.
 * @author alec
 */
public class Line {
	private Point p1 = new Point(),
	              p2 = new Point();
	private Rectangle bounds = new Rectangle();
	
	/**
	 * Constructs a line from <code>(0, 0)</code> to <code>(0, 0)</code>.
	 */
	public Line() {
	}
	
	/**
	 * Constructs a line between the two points given.
	 * @param p1 the first point
	 * @param p2 the second point
	 */
	public Line(Point p1, Point p2) {
		setFirstPoint(p1);
		setSecondPoint(p2);
	}
	
	/**
	 * Gets the first point on this line.
	 * @return the first point
	 */
	public Point getFirstPoint() {
		return p1;
	}
	
	/**
	 * Overwrites the first point on this line with <code>p</code>.
	 * @param p a Point
	 */
	public void setFirstPoint(Point p) {
		if (p != null)
			p1.setLocation(p);
	}

	/**
	 * Gets the second point on this line.
	 * @param p the second point
	 */
	public Point getSecondPoint() {
		return p1;
	}

	/**
	 * Overwrites the second point on this line with <code>p</code>.
	 * @param p a Point
	 */
	public void setSecondPoint(Point p) {
		if (p != null)
			p2.setLocation(p);
	}
	
	/**
	 * Returns the gradient of this line.
	 * @return a gradient
	 */
	public double getGradient() {
		return (p2.y - p1.y) / (double)(p2.x - p1.x); 
	}
	
	/**
	 * Returns the gradient of the line perpendicular to this one.
	 * @return a gradient
	 */
	public double getPerpendicularGradient() {
		return -1 / getGradient(); 
	}
	
	/**
	 * Returns the point where this line and the line perpendicular to it which
	 * passes through point <code>p3</code> meet.
	 * @param p3 a Point
	 * @return the point of intersection, or <code>null</code> if there isn't
	 *         one
	 */
	public Point getIntersection(Point p3) {
		return getIntersection(new Point(), p3);
	}
	
	/**
	 * As {@link #getIntersection(Point)}, but doesn't allocate a new point.
	 * @param target the Point to store the result in
	 * @param p3 a Point
	 * @return the point of intersection, or <code>null</code> if there isn't
	 *         one
	 */
	public Point getIntersection(Point target, Point p3) {
		if (p3.equals(p1)) {
			return target.setLocation(p1);
		} else if (p3.equals(p2)) {
			return target.setLocation(p2);
		} else if (p1.x == p2.x) {
			target.setLocation(p1.x, p3.y);
		} else if (p1.y == p2.y){
			target.setLocation(p3.x, p1.y);
		} else {
			double m_ = getGradient(),
			       m = getPerpendicularGradient();
			double x = ((m_ * p1.x) - p1.y - (m * p3.x) + p3.y) / (m_ - m),
			       y = m_ * (x - p1.x) + p1.y;
			target.setLocation((int)Math.round(x), (int)Math.round(y));
		}
		bounds.setBounds(p1.x, p1.y, 0, 0).union(p2);
		return (bounds.contains(target) ? target : null);
	}
	
	/**
	 * Returns the point at a given offset along the line (where
	 * <code>p1</code> is <code>0.0</code> and <code>p2</code> is
	 * <code>1.0</code>).
	 * @param offset an offset between <code>0</code> and <code>1</code>
	 *        inclusive
	 * @return a Point on this line segment, or <code>null</code> if
	 *         <code>offset</code> is out of bounds
	 */
	public Point getPointFromOffset(double offset) {
		if (offset >= 0.0 && offset <= 1.0)
			return p1.getCopy().translate(p2.getDifference(p1).scale(offset));
		else return null;
	}
	
	/**
	 * Returns the offset of a given point along the line (where
	 * <code>p1</code> is <code>0.0</code> and <code>p2</code> is
	 * <code>1.0</code>).
	 * @param point a Point on this line segment
	 * @return an offset on this line segment, or {@link Double#NaN} if
	 *         <code>point</code> isn't on this line
	 */
	public double getOffsetFromPoint(Point point) {
		if (getIntersection(point).equals(point)) {
			Dimension dp = p1.getDifference(point),
			          dt = p1.getDifference(p2);
			double d1 = (double)dp.width / dt.width,
			       d2 = (double)dp.height / dt.height;
			if (Double.isNaN(d1))
				return d2;
			else return d1;
		} else return Double.NaN;
	}
	
	/**
	 * Returns the length of this line (i.e., the Euclidean distance between
	 * its two points).
	 */
	public double getLength() {
		return p1.getDistance(p2);
	}
}
