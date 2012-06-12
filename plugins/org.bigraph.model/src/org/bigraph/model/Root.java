package org.bigraph.model;

import java.util.List;

import org.bigraph.model.interfaces.IChild;
import org.bigraph.model.interfaces.IRoot;


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
	public List<Node> getNodes() {
		return only(null, Node.class);
	}

	@Override
	public List<Site> getSites() {
		return only(null, Site.class);
	}

	@Override
	public List<IChild> getIChildren() {
		return only(null, IChild.class);
	}
}
