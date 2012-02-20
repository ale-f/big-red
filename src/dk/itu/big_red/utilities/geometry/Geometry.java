package dk.itu.big_red.utilities.geometry;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

/**
 * Utility functions for geometry.
 * @author alec
 *
 */
public class Geometry {
	public static Point getPointOnSegment(Point p1, Point p2, double offset) {
		return new Line(p1, p2).getPointFromOffset(offset);
	}
	
	public static Point getPointOnEllipse(Rectangle el, double offset) {
		return new Ellipse(el).getPointFromOffset(offset);
	}
	
	/**
	 * Scales and resizes the polygon defined by <code>points</code> to fit
	 * within <code>rectangle</code>.
	 * @param points a {@link PointList} defining a polygon
	 * @param rectangle a {@link Rectangle}
	 * @return
	 */
	public static PointList fitPolygonToRectangle(PointList points, ReadonlyRectangle rectangle) {
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
		Rectangle adjustedBounds = new Rectangle(adjustedPoints.getBounds());
		double xScale = rectangle.getWidth() - 2,
		       yScale = rectangle.getHeight() - 2;
		xScale /= adjustedBounds.getWidth() - 1;
		yScale /= adjustedBounds.getHeight() - 1;
		
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

	public static final Point ORIGIN = new Point(0, 0);
}
