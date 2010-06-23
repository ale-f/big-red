package dk.itu.big_red.figure.adornments;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;

public class CentreAnchor extends AbstractConnectionAnchor {

	public CentreAnchor(IFigure owner) {
		super(owner);
	}
	
	@Override
	public Point getLocation(Point reference) {
		Point p = getOwner().getBounds().getCenter();
		getOwner().translateToAbsolute(p);
		return p;
	}

}
