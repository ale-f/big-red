package org.bigraph.model;

import org.bigraph.model.interfaces.IParent;
import org.bigraph.model.interfaces.ISite;

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
