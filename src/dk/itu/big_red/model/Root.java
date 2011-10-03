package dk.itu.big_red.model;

import dk.itu.big_red.model.interfaces.IChild;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IRoot;
import dk.itu.big_red.model.interfaces.ISite;
import dk.itu.big_red.util.Utility;

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
		return Utility.only(children, INode.class);
	}

	@Override
	public Iterable<ISite> getISites() {
		return Utility.only(children, ISite.class);
	}

	@Override
	public Iterable<IChild> getIChildren() {
		return Utility.only(children, IChild.class);
	}
}
