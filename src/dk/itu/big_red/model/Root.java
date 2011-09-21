package dk.itu.big_red.model;

import dk.itu.big_red.model.interfaces.IChild;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IRoot;
import dk.itu.big_red.model.interfaces.ISite;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.util.HomogeneousIterable;

/**
 * 
 * @author alec
 * @see IRoot
 */
public class Root extends NameableContainer implements IRoot {
	
	@Override
	public boolean canContain(ILayoutable child) {
		Class<? extends ILayoutable> c = child.getClass();
		return (c == Node.class || c == Site.class);
	}

	@Override
	public Iterable<INode> getINodes() {
		return new HomogeneousIterable<INode>(children, INode.class);
	}

	@Override
	public Iterable<ISite> getISites() {
		return new HomogeneousIterable<ISite>(children, ISite.class);
	}

	@Override
	public Iterable<IChild> getIChildren() {
		return new HomogeneousIterable<IChild>(children, IChild.class);
	}

	@Override
	public NameType getNameType() {
		return NameType.NAME_NUMERIC;
	}
}
