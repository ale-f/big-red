package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;

import org.bigraph.model.Edge;
import org.bigraph.model.Link;
import org.eclipse.draw2d.IFigure;

import dk.itu.big_red.editors.AbstractGEFEditor;
import dk.itu.big_red.editors.bigraph.figures.EdgeFigure;
import dk.itu.big_red.editors.bigraph.figures.assistants.FixedPointAnchor.Orientation;

/**
 * EdgeParts represent {@link Edge}s, the container for - and target point of -
 * {@link Link.Connection}s.
 * @see Edge
 * @see Link.Connection
 * @see LinkConnectionPart
 * @author alec
 */
public class EdgePart extends LinkPart {
	@Override
	protected IFigure createFigure() {
		return new EdgeFigure();
	}
	
	@Override
	public void activate() {
		super.activate();
		getViewer().addPropertyChangeListener(this);
	}
	
	@Override
	public void deactivate() {
		getViewer().removePropertyChangeListener(this);
		super.deactivate();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getViewer()) {
			if (AbstractGEFEditor.PROPERTY_DISPLAY_EDGES.equals(
					evt.getPropertyName()))
				refreshVisuals();
			return;
		}
		super.propertyChange(evt);
	}
	
	@Override
	public EdgeFigure getFigure() {
		return (EdgeFigure)super.getFigure();
	}
	
	@Override
	public void refreshVisuals() {
		super.refreshVisuals();
		
		Object displayEdgesObj = getViewer().getProperty(
				AbstractGEFEditor.PROPERTY_DISPLAY_EDGES);
		boolean displayEdges = (displayEdgesObj instanceof Boolean ?
				(Boolean)displayEdgesObj : true);
		
		getFigure().setRender(displayEdges);
		if (displayEdges) /* otherwise irrelevant */
			getFigure().setSingle(getModel().getPoints().size() == 1);
		getFigure().repaint();
	}
	
	@Override
	public Orientation getAnchorOrientation() {
		return Orientation.CENTER;
	}
	
	@Override
	public String getToolTip() {
		return "Edge " + getModel().getName();
	}
}
