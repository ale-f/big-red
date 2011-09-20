package dk.itu.big_red.model.changes.bigraph;

import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.changes.Change;

public class BigraphChangeConnect extends Change {
	public Point point;
	public Link link;
	
	public BigraphChangeConnect(Point point, Link link) {
		this.point = point;
		this.link = link;
	}

	@Override
	public Change inverse() {
		return new BigraphChangeDisconnect(point, link);
	}
}
