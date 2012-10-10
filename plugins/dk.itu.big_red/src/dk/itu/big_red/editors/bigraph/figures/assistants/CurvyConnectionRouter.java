package dk.itu.big_red.editors.bigraph.figures.assistants;

import org.eclipse.draw2d.AbstractRouter;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;

public class CurvyConnectionRouter extends AbstractRouter {
	@Override
	public void route(Connection connection) {
		PointList pl = connection.getPoints();
		pl.removeAllPoints();
		
		Point source = getStartPoint(connection).getCopy(),
				target = getEndPoint(connection).getCopy();
		
		connection.translateToRelative(source);
		connection.translateToRelative(target);
		
		QuadraticBezierCurve b = new QuadraticBezierCurve();
		b.setPoint0(source);
		b.setPoint1(new Point(target.x, source.y));
		b.setPoint2(target);
		
		PrecisionPoint p = new PrecisionPoint();
		pl.addPoint(source);
		for (double i = 0.0; i < 1.0; i += 0.05)
			pl.addPoint(b.getPoint(p, i));
		pl.addPoint(target);
		
		connection.setPoints(pl);
	}
}
