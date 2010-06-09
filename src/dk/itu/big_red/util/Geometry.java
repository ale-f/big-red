package dk.itu.big_red.util;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

public class Geometry {
	/**
	 * Returns the point on the ellipse defined by the given rectangle which is
	 * closest to the given point.
	 * @param r a rectangle defining an ellipse
	 * @param p a point
	 * @return the point on the ellipse defined by r closest to p
	 */
	public static Point getNearestPointOnEllipse(Rectangle r, Point p) {
		return new Point(r.width / 2, 0);
	}
	
	/**
	 * Returns the point on the polygon defined by the given set of points
	 * which is closest to the given point.
	 * @param r a set of points defining a polygon
	 * @param p a point
	 * @return the point on the polygon defined by r closest to p
	 */
	public static Point getNearestPointOnPolygon(PointList r, Point p) {
		return new Point(0, 0);
	}
}
