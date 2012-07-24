package dk.itu.big_red.model;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;

public class Ellipse {
	public static final Ellipse SINGLETON = new Ellipse();
	
	private Rectangle bb = new Rectangle();
	
	public Ellipse() {
	}
	
	public Ellipse(Rectangle bb) {
		setBounds(bb);
	}
	
	/**
	 * @return <code>this</code>, for convenience
	 */
	public Ellipse setBounds(Rectangle bb) {
		this.bb.setBounds(bb);
		return this;
	}
	
	/**
	 * Forces the argument into the interval <code>[0, 1)</code>.
	 * @param offset a double
	 * @return a value in the interval <code>[0, 1)</code>
	 */
	private static double fix(double offset) {
		if (offset < 0.0) {
			offset = Math.abs(offset);
			return 1.0 - (offset - Math.floor(offset));
		} else if (offset >= 1.0) {
			return offset - Math.floor(offset);
		} else return offset;
	}
	
	public Point getPointFromOffset(double offset) {
		Point centre = bb.getCenter();
		offset = fix(offset) - 0.25;
		double t = offset * (2 * Math.PI),
		       a = bb.width() / 2,
		       b = bb.height() / 2;
		return new PrecisionPoint(centre.x + (a * Math.cos(t)),
				centre.y + (b * Math.sin(t)));
	}
	
	private static double sgn(double x) {
		return (x < 0 ? -1 : 1);
	}
	
	private static double sq(double x) {
		return Math.pow(x, 2);
	}
	
	private static final double M_2PI = Math.PI * 2.0;
	
	/**
	 * Returns the offset value specifying the point on this ellipse closest to
	 * the given point.
	 * @param p a Point
	 * @return an offset
	 */
	public double getClosestOffset(Point p) {
		Point elc = bb.getCenter();
		double elw = bb.width(), elh = bb.height();
		double xscale = 1, yscale = 1;
		
		if (bb.width() > bb.height())
			xscale = elh / elw;
		else yscale = elw / elh;
		
		elc.scale(xscale, yscale);
		elw *= xscale; elh *= yscale;
		
		Point tr = elc.getNegated();
		p = p.getCopy().scale(xscale, yscale).translate(tr);
		elc.translate(tr);
		
		double dx = -p.x, dy = -p.y,
		       dr2 = sq(dx) + sq(dy),
		       r = elw / 2,
		       sqrt = Math.sqrt(sq(r) * dr2),
		       x0 = (sgn(dy) * dx * sqrt) / dr2,
		       x1 = (-(sgn(dy) * dx * sqrt)) / dr2,
		       y0 = (Math.abs(dy) * sqrt) / dr2,
		       y1 = (-(Math.abs(dy) * sqrt)) / dr2;
		
		Point p0 = new PrecisionPoint(x0, y0),
		      p1 = new PrecisionPoint(x1, y1);
		
		double d0 = p.getDistance(p0),
		       d1 = p.getDistance(p1),
		       t = (d0 < d1 ? Math.atan2(y0, x0) : Math.atan2(y1, x1));
		
		if (t < 0)
			t += M_2PI;
		t = (t / M_2PI) + 0.25;
		return (t >= 1 ? t - 1 : t);
	}
	
	public Point getClosestPoint(Point p) {
		return getPointFromOffset(getClosestOffset(p));
	}

	/**
	 * Gets a polygon that fits within this {@link Ellipse}.
	 * <p>The polygon's <i>i</i>th point will be located at offset <code>i /
	 * sides</code>.
	 * @param sides the number of sides the polygon should have (must be 3 or
	 * more)
	 * @return a {@link PointList} specifying a polygon
	 */
	public PointList getPolygon(int sides) {
		if (sides < 3)
			return null;
		PointList r = new PointList(sides);
		for (int i = 0; i < sides; i++)
			r.addPoint(getPointFromOffset(((double)i) / sides));
		return r;
	}
}
