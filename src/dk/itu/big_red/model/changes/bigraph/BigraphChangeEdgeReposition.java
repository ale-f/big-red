package dk.itu.big_red.model.changes.bigraph;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.changes.Change;

public class BigraphChangeEdgeReposition extends Change {
	public Edge edge;
	
	public BigraphChangeEdgeReposition(Edge edge) {
		this.edge = edge;
	}

	private Rectangle oldLayout;
	@Override
	public void beforeApply() {
		oldLayout = edge.getLayout();
	}
	
	@Override
	public Change inverse() {
		return new BigraphChangeLayout(edge, oldLayout);
	}
	
	@Override
	public boolean canInvert() {
		return (oldLayout != null);
	}
	
	@Override
	public boolean isReady() {
		return (edge != null);
	}
	
	@Override
	public String toString() {
		return "Change(recalculate position of " + edge + ")";
	}
}
