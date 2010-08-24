package dk.itu.big_red.model;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.interfaces.IConnectable;
import dk.itu.big_red.model.interfaces.pure.IEdge;

/**
  * An Edge is a connection which connects any number of {@link Port}s and
  * {@link InnerName}s. (An Edge which "connects" only one point is perfectly
  * legitimate.)
  * 
  * <p>Note that Edges represent the <i>bigraphical</i> concept of an edge
  * rather than a GEF/GMF {@link Connection}, and so they lack any concept of a
  * "source" or "target"; the Edge is always the target for a connection, and
  * {@link Point}s are always sources.
  * @author alec
  *
  */
public class Edge extends Link implements IEdge {
	/**
	 * Moves this EdgeTarget to the average position of all the
	 * {@link IConnectable}s connected to it.
	 */
	public void averagePosition() {
		int tx = 0, ty = 0, s = getConnections().size();
		for (LinkConnection f : getConnections()) {
			tx += f.getSource().getRootLayout().x;
			ty += f.getSource().getRootLayout().y;
		}
		setLayout(new Rectangle(tx / s, ty / s, getLayout().width, getLayout().height));
	}
	
	public void addPoint(Point point) {
		super.addPoint(point);
		
		if (!getBigraph().hasChild(this))
			getBigraph().addChild(this);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>If this Edge has no points left after this operation, then it'll be
	 * removed from the Bigraph.
	 */
	public void removePoint(Point point) {
		super.removePoint(point);
		
		if (getConnections().size() == 0)
			getBigraph().removeChild(this);
	}
	
	@Override
	public Edge clone() {
		return new Edge();
	}

}
