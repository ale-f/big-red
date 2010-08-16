package dk.itu.big_red.util;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class Ellipse {
	private Rectangle bb = new Rectangle();
	
	public Ellipse() {
	}
	
	public Ellipse(Rectangle bb) {
		this.bb.setBounds(bb);
	}
	
	public Point getPointFromOffset(double offset) {
		Point centre = bb.getCenter();
		offset -= 0.25;
		double t = offset * (2 * Math.PI),
		       a = bb.width / 2,
		       b = bb.height / 2;
		return new Point(centre.x + (a * Math.cos(t)),
				centre.y + (b * Math.sin(t)));
	}
	
	public double getOffsetFromPoint(Point p) {
		Point centre = bb.getCenter();
		double a = bb.width / 2, b = bb.height / 2,
		       x = p.x - centre.x, y = p.y - centre.y;
		/* x = a * Math.cos(t), y = b * Math.cos(t) */
		x /= a; y /= b; /* x = Math.cos(t), y = Math.sin(t) */
		x = Math.acos(x); y = Math.asin(y); /* x = t, y = t */
		System.out.println(x + ", " + y);
		return (x + y) / 2;
	}
}
