package dk.itu.big_red.model;

import dk.itu.big_red.model.interfaces.IChild;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IRoot;
import dk.itu.big_red.model.interfaces.ISite;
import dk.itu.big_red.util.Lists;

/**
 * 
 * @author alec
 * @see IRoot
 */
public class Root extends Container implements IRoot {
	
	@Override
	public boolean canContain(Layoutable child) {
		Class<? extends Layoutable> c = child.getClass();
		return (c == Node.class || c == Site.class);
	}

	@Override
	public Iterable<INode> getINodes() {
		return Lists.only(children, INode.class);
	}

	@Override
	public Iterable<ISite> getISites() {
		return Lists.only(children, ISite.class);
	}

	@Override
	public Iterable<IChild> getIChildren() {
		return Lists.only(children, IChild.class);
	}
}
