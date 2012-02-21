package dk.itu.big_red.model;

import java.util.Map;

import dk.itu.big_red.model.interfaces.IEdge;
import dk.itu.big_red.utilities.geometry.Rectangle;

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
	public class ChangeReposition extends LayoutableChange {
		@Override
		public Edge getCreator() {
			return Edge.this;
		}
		
		protected ChangeReposition() {
		}

		private Rectangle oldLayout;
		@Override
		public void beforeApply() {
			oldLayout = getCreator().getLayout().getCopy();
		}
		
		@Override
		public LayoutableChange inverse() {
			return getCreator().changeLayout(oldLayout);
		}
		
		@Override
		public boolean canInvert() {
			return (oldLayout != null);
		}
		
		@Override
		public boolean isReady() {
			return true;
		}
		
		@Override
		public String toString() {
			return "Change(recalculate position of " + getCreator() + ")";
		}
	}
	
	/**
	 * Moves this EdgeTarget to the average position of all the
	 * {@link Point}s connected to it.
	 */
	protected void averagePosition() {
		int tx = 0, ty = 0, s = getPoints().size();
		for (Point p : getPoints()) {
			tx += p.getRootLayout().getX();
			ty += p.getRootLayout().getY();
		}
		setLayout(new Rectangle(tx / s, ty / s, getLayout().getWidth(), getLayout().getHeight()));
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
	public Edge clone(Map<ModelObject, ModelObject> m) {
		return (Edge)super.clone(m);
	}

	public LayoutableChange changeReposition() {
		return new ChangeReposition();
	}
}
