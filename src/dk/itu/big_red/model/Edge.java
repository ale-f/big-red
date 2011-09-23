package dk.itu.big_red.model;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.interfaces.IEdge;

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
  * @see IEdge
  *
  */
public class Edge extends Link implements IEdge {
	/**
	 * Moves this EdgeTarget to the average position of all the
	 * {@link Point}s connected to it.
	 */
	public void averagePosition() {
		int tx = 0, ty = 0, s = getPoints().size();
		for (Point p : getPoints()) {
			tx += p.getRootLayout().x;
			ty += p.getRootLayout().y;
		}
		setLayout(new Rectangle(tx / s, ty / s, getLayout().width, getLayout().height));
	}
	
	public Edge() {
		super.setLayout(new Rectangle(5, 5, 14, 14));
	}
	
	@Override
	public void setLayout(Rectangle newLayout) {
		if (newLayout != null)
			newLayout.setSize(14, 14);
		super.setLayout(newLayout);
	}
	
	@Override
	public Edge clone() {
		return new Edge();
	}

	@Override
	public NameType getNameType() {
		return NameType.NAME_ALPHABETIC;
	}

}
