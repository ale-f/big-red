package dk.itu.big_red.editors.bigraph.figures.assistants;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;

/**
 * The <strong>QuadraticBezierCurve</strong> does exactly what it says on the
 * tin: it models quadratic Bézier curves.
 * @author alec
 */
class QuadraticBezierCurve {
	private PrecisionPoint p0, p1, p2;
	
	/**
	 * Sets the curve's first control point.
	 * @param p a {@link Point}
	 * @return <code>this</code>, for convenience
	 */
	public QuadraticBezierCurve setPoint0(Point p) {
		p0 = new PrecisionPoint(p);
		return this;
	}
	
	/**
	 * Sets the curve's second control point.
	 * @param p a {@link Point}
	 * @return <code>this</code>, for convenience
	 */
	public QuadraticBezierCurve setPoint1(Point p) {
		p1 = new PrecisionPoint(p);
		return this;
	}
	
	/**
	 * Sets the curve's third control point.
	 * @param p a {@link Point}
	 * @return <code>this</code>, for convenience
	 */
	public QuadraticBezierCurve setPoint2(Point p) {
		p2 = new PrecisionPoint(p);
		return this;
	}
	
	/**
	 * @see #getPoint(PrecisionPoint, double)
	 */
	public PrecisionPoint getPoint(double t) {
		return getPoint(new PrecisionPoint(), t);
	}
	
	/**
	 * Calculates a point on the curve.
	 * @param p the destination {@link PrecisionPoint}; must not be
	 * <code>null</code>
	 * @param t the parameter (must be between <code>0.0</code> and
	 * <code>1.0</code> inclusive)
	 * @return a {@link PrecisionPoint} on the curve
	 */
	public PrecisionPoint getPoint(PrecisionPoint p, double t) {
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