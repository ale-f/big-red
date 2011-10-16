package dk.itu.big_red.editors.bigraph.parts;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import dk.itu.big_red.editors.bigraph.figures.EdgeFigure;
import dk.itu.big_red.editors.bigraph.figures.assistants.FixedPointAnchor.Orientation;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Link;

/**
 * EdgeParts represent {@link Edge}s, the container for - and target point of -
 * {@link LinkConnection}s.
 * @see Edge
 * @see LinkConnection
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
	protected List<Link.Connection> getModelTargetConnections() {
		List<Link.Connection> lc = super.getModelTargetConnections();
		getFigure().setVisible(lc.size() != 0);
		return lc;
	}
	
	@Override
	public Orientation getAnchorOrientation() {
		return Orientation.CENTER;
	}
}
