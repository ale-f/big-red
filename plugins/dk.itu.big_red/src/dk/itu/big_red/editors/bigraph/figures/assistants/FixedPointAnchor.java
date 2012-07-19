package dk.itu.big_red.editors.bigraph.figures.assistants;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * FixedPointAnchors always return a fixed point on the bounding box of their
 * owning {@link IFigure}.
 * @author alec
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
		Point p = null;
		Rectangle b = getOwner().getBounds();
		switch (orientation) {
		case NORTH_WEST:
			p = b.getTopLeft();
			break;
		case NORTH:
			p = b.getTop();
			break;
		case NORTH_EAST:
			p = b.getTopRight();
			break;
		case EAST:
			p = b.getRight();
			break;
		case SOUTH_EAST:
			p = b.getBottomRight();
			break;
		case SOUTH:
			p = b.getBottom();
			break;
		case SOUTH_WEST:
			p = b.getBottomLeft();
			break;
		case WEST:
			p = b.getLeft();
			break;
		case CENTER:
		default:
			p = b.getCenter();
			break;
		}
		getOwner().translateToAbsolute(p);
		return p;
	}
}
