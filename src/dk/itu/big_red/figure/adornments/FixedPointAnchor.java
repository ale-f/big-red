package dk.itu.big_red.figure.adornments;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;

/**
 * FixedPointAnchors always return a fixed point on the bounding box of their
 * owning {@link IFigure}.
 * @author alec
 *
 */
public class FixedPointAnchor extends AbstractConnectionAnchor {
	public enum Orientation {
		NORTH_WEST,
		NORTH,
		NORTH_EAST,
		EAST,
		SOUTH_EAST,
		SOUTH,
		SOUTH_WEST,
		WEST,
		CENTER
	};
	
	private Orientation orientation = Orientation.CENTER;
	
	public FixedPointAnchor(IFigure owner) {
		super(owner);
	}
	
	public FixedPointAnchor(IFigure owner, Orientation orientation) {
		super(owner);
		this.orientation = orientation;
	}
	
	@Override
	public Point getLocation(Point reference) {
		switch (orientation) {
		case NORTH_WEST:
			return getOwner().getBounds().getTopLeft();
		case NORTH:
			return getOwner().getBounds().getTop();
		case NORTH_EAST:
			return getOwner().getBounds().getTopRight();
		case EAST:
			return getOwner().getBounds().getRight();
		case SOUTH_EAST:
			return getOwner().getBounds().getBottomRight();
		case SOUTH:
			return getOwner().getBounds().getBottom();
		case SOUTH_WEST:
			return getOwner().getBounds().getBottomLeft();
		case WEST:
			return getOwner().getBounds().getLeft();
		case CENTER:
		default:
			return getOwner().getBounds().getCenter();
		}
	}

}
