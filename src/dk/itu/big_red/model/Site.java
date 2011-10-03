package dk.itu.big_red.model;

import dk.itu.big_red.model.assistants.CloneMap;
import dk.itu.big_red.model.interfaces.IParent;
import dk.itu.big_red.model.interfaces.ISite;

/**
 * @author alec
 * @see ISite
 */
public class Site extends Layoutable implements ISite {
	@Override
	public Site clone(CloneMap m) {
		return (Site)super.clone(m);
	}

	@Override
	public IParent getIParent() {
		return (IParent)getParent();
	}
}
