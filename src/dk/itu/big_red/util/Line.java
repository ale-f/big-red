package dk.itu.big_red.util;

import org.eclipse.draw2d.geometry.Geometry;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class Line {
	private Point p1 = new Point(),
	              p2 = new Point();
	private Rectangle bounds = new Rectangle();
	
	public Line() {
	}
	
	public Point getFirstPoint() {
		return p1;
	}
	
	public void setFirstPoint(Point p) {
		if (p != null) {
			p1.setLocation(p);
			updateBounds();
		}
	}
	
	public Point getSecondPoint() {
		return p1;
	}
	
	public void setSecondPoint(Point p) {
		if (p != null) {
			p2.setLocation(p);
			updateBounds();
		}
	}
	
	private void updateBounds() {
		bounds.scale(0);
		bounds.setLocation(p1);
		bounds.union(p2);
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
	 * Returns the point where the line perpendicular to this line and passing
	 * through point <code>p</code> meets this line.
	 * @return the point of intersection, or <code>null</code> if there isn't
	 *         one
	 */
	public Point getIntersection(Point p3) {
		return getIntersection(new Point(), p3);
	}
	
	public Point getIntersection(Point target, Point p3) {
		if (p1.x == p2.x) {
			target.setLocation(p1.x, p3.y);
		} else if (p1.y == p2.y){
			target.setLocation(p3.x, p1.y);
		} else {
			double m_ = getGradient(),
			       m = getPerpendicularGradient();
			double x = ((m_ * p1.x) - p1.y - (m * p3.x) + p3.y) / (m_ - m),
			       y = m_ * (x - p1.x) + p1.y;
			target.setLocation((int)x, (int)y);
		}
		return (bounds.contains(target) ? target : null);
	}
}
