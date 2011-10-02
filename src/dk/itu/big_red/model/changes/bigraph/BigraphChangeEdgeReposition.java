package dk.itu.big_red.model.changes.bigraph;

import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.util.geometry.Rectangle;

public class BigraphChangeEdgeReposition extends Change {
	public Edge edge;
	
	public BigraphChangeEdgeReposition(Edge edge) {
		this.edge = edge;
	}

	private Rectangle oldLayout;
	@Override
	public void beforeApply() {
		oldLayout = edge.getLayout().getCopy();
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
