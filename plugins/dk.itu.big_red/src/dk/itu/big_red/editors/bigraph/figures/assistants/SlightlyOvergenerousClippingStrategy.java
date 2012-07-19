package dk.itu.big_red.editors.bigraph.figures.assistants;

import org.eclipse.draw2d.IClippingStrategy;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * SlightlyOvergenerousClippingStrategies expand the clipping region of {@link
 * IFigure}s to include a five-pixel margin on all sides.
 * @author alec
 */
public class SlightlyOvergenerousClippingStrategy implements IClippingStrategy {
	/**
	 * Gets a clipping region for the given figure which is five pixels larger
	 * than its bounds in all directions.
	 */
	@Override
	public Rectangle[] getClip(IFigure childFigure) {
		return new Rectangle[] {
			childFigure.getBounds().getCopy().expand(10, 10)
		};
	}
}
