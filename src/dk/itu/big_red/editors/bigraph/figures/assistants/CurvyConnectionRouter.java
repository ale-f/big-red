package dk.itu.big_red.editors.bigraph.figures.assistants;

import org.eclipse.draw2d.AbstractRouter;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

import dk.itu.big_red.util.geometry.QuadraticBézierCurve;

public class CurvyConnectionRouter extends AbstractRouter {
	@Override
	public void route(Connection connection) {
		PointList pl = connection.getPoints();
		pl.removeAllPoints();
		
		Point source = getStartPoint(connection).getCopy(),
				target = getEndPoint(connection).getCopy();
		
		connection.translateToRelative(source);
		connection.translateToRelative(target);
		
		QuadraticBézierCurve b = new QuadraticBézierCurve();
		b.setPoint0(source);
		b.setPoint1(new Point(target.x, source.y));
		b.setPoint2(target);
		
		pl.addPoint(source);
		for (double i = 0.0; i < 1.0; i += 0.05)
			pl.addPoint(b.getPoint(i));
		pl.addPoint(target);
		
		connection.setPoints(pl);
	}

}
