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
}
