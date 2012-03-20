package dk.itu.big_red.editors.bigraph.parts;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;

import dk.itu.big_red.editors.bigraph.figures.assistants.FixedPointAnchor;
import dk.itu.big_red.editors.bigraph.figures.assistants.FixedPointAnchor.Orientation;

abstract class ConnectablePart extends AbstractPart
		implements NodeEditPart {
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new FixedPointAnchor(getFigure(), getAnchorOrientation());
    }
    
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new FixedPointAnchor(getFigure(), getAnchorOrientation());
    }
	
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new FixedPointAnchor(getFigure(), getAnchorOrientation());
    }
    
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new FixedPointAnchor(getFigure(), getAnchorOrientation());
    }
	
	/**
	 * Returns the {@link Orientation} which should be given to any of this
	 * PointPart's target {@link FixedPointAnchor}s.
	 * @return an Orientation
	 */
	abstract protected Orientation getAnchorOrientation();
}
