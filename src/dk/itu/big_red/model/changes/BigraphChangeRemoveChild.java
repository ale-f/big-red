package dk.itu.big_red.model.changes;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;

public class BigraphChangeRemoveChild extends Change {
	public Container parent;
	public LayoutableModelObject child;
	
	public BigraphChangeRemoveChild(Container parent, LayoutableModelObject child) {
		this.parent = parent;
		this.child = child;
	}

	@Override
	public Change inverse() {
		return new BigraphChangeAddChild(parent, child);
	}
}
