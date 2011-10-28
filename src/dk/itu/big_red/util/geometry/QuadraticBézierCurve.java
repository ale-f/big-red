package dk.itu.big_red.util.geometry;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;

/**
 * The <strong>QuadraticBézierCurve</strong> does exactly what it says on the
 * tin: it models quadratic Bézier curves.
 * @author alec
 *
 */
public class QuadraticBézierCurve {
	private PrecisionPoint p0, p1, p2;
	
	/**
	 * Sets the curve's first control point.
	 * @param p a {@link Point}
	 * @return <code>this</code>, for convenience
	 */
	public QuadraticBézierCurve setPoint0(Point p) {
		p0 = new PrecisionPoint(p);
		return this;
	}
	
	/**
	 * Sets the curve's second control point.
	 * @param p a {@link Point}
	 * @return <code>this</code>, for convenience
	 */
	public QuadraticBézierCurve setPoint1(Point p) {
		p1 = new PrecisionPoint(p);
		return this;
	}
	
	/**
	 * Sets the curve's third control point.
	 * @param p a {@link Point}
	 * @return <code>this</code>, for convenience
	 */
	public QuadraticBézierCurve setPoint2(Point p) {
		p2 = new PrecisionPoint(p);
		return this;
	}
	
	/**
	 * Calculates a point on the curve.
	 * @param t the parameter (must be between <code>0.0</code> and
	 * <code>1.0</code> inclusive)
	 * @return a {@link PrecisionPoint} on the curve
	 */
	public PrecisionPoint getPoint(double t) {
		PrecisionPoint p = new PrecisionPoint();
		double t_ = 1 - t;
		return
			p.setPreciseX(
				(Math.pow(t_, 2) * p0.preciseX()) +
				(2 * t_ * t * p1.preciseX()) +
				(Math.pow(t, 2) * p2.preciseX()))
			.setPreciseY(
				(Math.pow(t_, 2) * p0.preciseY()) +
				(2 * t_ * t * p1.preciseY()) +
				(Math.pow(t, 2) * p2.preciseY()));
	}
}