package dk.itu.big_red.model.changes;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Edge;

public class BigraphChangeEdgeReposition extends Change {
	public Edge edge;
	
	public BigraphChangeEdgeReposition(Edge edge) {
		this.edge = edge;
	}

	private Rectangle oldLayout;
	public void setOldLayout(Rectangle oldLayout) {
		this.oldLayout = oldLayout;
	}
	
	@Override
	public Change inverse() {
		return new BigraphChangeLayout(edge, oldLayout);
	}
	
	@Override
	public boolean canInvert() {
		return (oldLayout != null);
	}
}
