package dk.itu.big_red.util;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

public class Geometry {
	public static Point getPointOnSegment(Point p1, Point p2, double offset) {
		return new Point(p1).translate(p2.getDifference(p1).scale(offset));
	}
	
	/**
	 * Scales and resizes the polygon defined by <code>points</code> to fit
	 * within <code>rectangle</code>.
	 * @param points a {@link PointList} defining a polygon
	 * @param rectangle a {@link Rectangle}
	 * @return
	 */
	public static PointList fitPolygonToRectangle(PointList points, Rectangle rectangle) {
		PointList adjustedPoints = points.getCopy();

		/*
		 * Move the polygon so that its top-left corner is at (0,0).
		 */
		adjustedPoints.translate(
				points.getBounds().getTopLeft().getNegated());
		
		/*
		 * Work out the scaling factors that'll make the polygon fit inside
		 * the layout.
		 * 
		 * (Note that adjustedBounds.width and adjustedBounds.height are
		 * both off-by-one - getBounds() prefers < to <=, it seems.)
		 */
		Rectangle adjustedBounds = adjustedPoints.getBounds();
		double xScale = rectangle.width - 2,
		       yScale = rectangle.height - 2;
		xScale /= adjustedBounds.width - 1; yScale /= adjustedBounds.height - 1;
		
		/*
		 * Scale all of the points.
		 */
		Point tmp = Point.SINGLETON;
		for (int i = 0; i < adjustedPoints.size(); i++) {
			adjustedPoints.getPoint(tmp, i).scale(xScale, yScale).translate(1, 1);
			adjustedPoints.setPoint(tmp, i);
		}
		
		return adjustedPoints;
	}
	
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
	public static Point getNearestPointOnPolygon(PointList l, Point d) {
		double d1 = 0, d2 = 0;
		int i1 = -1, i2 = -1;
		
		for (int i = 0; i < l.size(); i++) {
			Point pt = l.getPoint(i);
			double td = d.getDistance(pt);
			if (td > d1) {
				d1 = td;
				i1 = i;
			} else if (td > d2) {
				d2 = td;
				i2 = i;
			}
		}
		
		System.out.println("Closest two points are " + i1 + " and " + i2);
		
		return new Point(0, 0);
	}
}
