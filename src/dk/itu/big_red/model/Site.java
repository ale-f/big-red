package dk.itu.big_red.model;

import dk.itu.big_red.model.interfaces.IParent;
import dk.itu.big_red.model.interfaces.ISite;

/**
 * @author alec
 * @see ISite
 */
public class Site extends Layoutable implements ISite {
	@Override
	public IParent getIParent() {
		return (IParent)getParent();
	}
}
