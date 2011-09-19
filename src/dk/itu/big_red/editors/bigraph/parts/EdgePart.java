package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import dk.itu.big_red.editors.bigraph.figures.EdgeFigure;
import dk.itu.big_red.editors.bigraph.figures.assistants.FixedPointAnchor.Orientation;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.LinkConnection;

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
	public Link getModel() {
		return super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new EdgeFigure();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		refreshVisuals();
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
}
