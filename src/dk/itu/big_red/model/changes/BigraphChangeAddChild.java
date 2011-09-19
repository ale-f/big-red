package dk.itu.big_red.model.changes;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;

public class BigraphChangeAddChild extends Change {
	public Container parent;
	public LayoutableModelObject child;
	
	public BigraphChangeAddChild(Container parent, LayoutableModelObject child) {
		this.parent = parent;
		this.child = child;
	}

	@Override
	public Change inverse() {
		return new BigraphChangeRemoveChild(parent, child);
	}
}
