package dk.itu.big_red.editors.bigraph.figures.assistants;

import org.eclipse.draw2d.AbstractRouter;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;

public class CurvyConnectionRouter extends AbstractRouter {
	/**
	 * A third-order Bézier curve.
	 * @author alec
	 *
	 */
	private class BézierCurve3 {
		private PrecisionPoint p0, p1, p2;
		
		private BézierCurve3 setP0(Point p0) {
			this.p0 = new PrecisionPoint(p0);
			return this;
		}
		
		private BézierCurve3 setP1(Point p1) {
			this.p1 = new PrecisionPoint(p1);
			return this;
		}
		
		private BézierCurve3 setP2(Point p2) {
			this.p2 = new PrecisionPoint(p2);
			return this;
		}
		
		private PrecisionPoint getPoint(double t) {
			PrecisionPoint p = new PrecisionPoint();
			double t_ = 1 - t;
			p.setPreciseX((Math.pow(t_, 2) * p0.preciseX()) +
					(2 * t_ * t * p1.preciseX()) +
					(Math.pow(t, 2) * p2.preciseX()));
			p.setPreciseY((Math.pow(t_, 2) * p0.preciseY()) +
					(2 * t_ * t * p1.preciseY()) +
					(Math.pow(t, 2) * p2.preciseY()));
			return p;
		}
	}
	
	@Override
	public void route(Connection connection) {
		PointList pl = connection.getPoints();
		pl.removeAllPoints();
		
		Point source = getStartPoint(connection).getCopy(),
				target = getEndPoint(connection).getCopy();
		
		connection.translateToRelative(source);
		connection.translateToRelative(target);
		
		BézierCurve3 b = new BézierCurve3();
		b.setP0(source);
		b.setP1(new Point(target.x, source.y));
		b.setP2(target);
		
		for (double i = 0.0; i < 1.0; i += 0.1)
			pl.addPoint(b.getPoint(i));
		
		connection.setPoints(pl);
	}

}
