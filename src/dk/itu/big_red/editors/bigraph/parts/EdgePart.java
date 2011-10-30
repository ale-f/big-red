package dk.itu.big_red.editors.bigraph.parts;

import org.eclipse.draw2d.IFigure;
import dk.itu.big_red.editors.bigraph.figures.EdgeFigure;
import dk.itu.big_red.editors.bigraph.figures.assistants.FixedPointAnchor.Orientation;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Link;

/**
 * EdgeParts represent {@link Edge}s, the container for - and target point of -
 * {@link Link.Connection}s.
 * @see Edge
 * @see Link.Connection
 * @see LinkConnectionPart
 * @author alec
 *
 */
public class EdgePart extends LinkPart {
	@Override
	protected IFigure createFigure() {
		return new EdgeFigure();
	}
	
	@Override
	public void refreshVisuals() {
		super.refreshVisuals();
		setResizable(false);
	}
	
	@Override
	public Orientation getAnchorOrientation() {
		return Orientation.CENTER;
	}
	
	@Override
	public String getDisplayName() {
		return "Edge " + getModel().getName();
	}
}
